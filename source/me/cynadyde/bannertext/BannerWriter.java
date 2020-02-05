package me.cynadyde.bannertext;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a held banner that is used by a player
 * to as a writer tool for some text.
 */
public class BannerWriter {

    public static final String WRITER_NAME_TEMPLATE = "&8< &3&lBannerText Writer &8| &3%d of %d &8>";
    public static final String LORE_SEPARATOR = ChatColor.DARK_GRAY + "--------------------------------";
    public static final String DATA_COLOR = "";  // ChatColor.BLACK + "" + ChatColor.MAGIC + "" + ChatColor.BOLD;
    public static final String DATA_PREFIX = "BannerText-Writer";

    private ItemStack item;
    private String[] chars;
    private int pos;

    private BannerWriter(ItemStack writerItem, String[] writerChars, int writerPos) {
        this.item = writerItem;
        this.chars = writerChars;
        this.pos = writerPos;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public int getPos() {
        return this.pos;
    }

    public int getMaxPos() {
        return this.chars.length - 1;
    }

    public void setPos(int newPos) {

        if (newPos < 0) {
            newPos += this.chars.length;
        }
        while (newPos > getMaxPos()) {
            newPos -= this.chars.length;
        }
        if (!((0 <= newPos) && (newPos <= getMaxPos()))) {
            return;
        }
        this.pos = newPos;
        updateItem();
    }

    public FormattedChar getChar(int pos) {

        String data = this.chars[pos];
        return new FormattedChar(
                PatternStyle.getByChar(data.charAt(0)),
                PatternColor.getByChar(data.charAt(1)),
                PatternColor.getByChar(data.charAt(2)),
                data.charAt(3));
    }

    public ItemStack getBanner(int pos) {

        FormattedChar c = getChar(pos);
        return BannerFactory.getBanner(c.getChar(), c.getTs(), c.getFg(), c.getBg());
    }

    public void updateItem() {

        if (!(item.getItemMeta() instanceof BannerMeta)) {
            throw new IllegalStateException("Writer's item did not have BannerMeta item meta.");
        }
        BannerMeta bannerMeta = (BannerMeta) item.getItemMeta();
        bannerMeta.setDisplayName(String.format(WRITER_NAME_TEMPLATE, getPos(), getMaxPos()));

        String[] dataTokens = new String[this.chars.length + 2];
        dataTokens[0] = DATA_PREFIX;
        dataTokens[1] = String.valueOf(this.pos);

        System.arraycopy(this.chars, 0, dataTokens, 2, this.chars.length);
        bannerMeta.setLore(mergeIntoLore(bannerMeta.getLore(), dataTokens));

        ItemStack modelBanner = getBanner(this.pos);
        BannerMeta modelMeta = (BannerMeta) modelBanner.getItemMeta();

        assert modelMeta != null;
        item.setType(modelBanner.getType());
        bannerMeta.setPatterns(modelMeta.getPatterns());

        this.item.setItemMeta(bannerMeta);
    }

    /**
     * Parse the given lore for data tokens.
     */
    private static String[] getLoreData(List<String> lore) {

        if ((lore == null) || (lore.size() < 2)) {
            return new String[0];
        }
        int start = lore.lastIndexOf(LORE_SEPARATOR) + 1;
        if (start >= lore.size()) {
            return new String[0];
        }
        return ChatColor.stripColor(String.join(
                ";", lore.subList(start, lore.size()))).split(";");
    }

    /**
     * Merge the specified data tokens with the given lore.
     */
    private static List<String> mergeIntoLore(List<String> lore, String[] dataTokens) {

        if (lore == null) {
            lore = new ArrayList<>();
        }
        if (lore.contains(LORE_SEPARATOR)) {
            lore = new ArrayList<>(lore.subList(0, lore.lastIndexOf(LORE_SEPARATOR)));
        }

        ArrayList<String> dataLines;
        {
            ArrayList<ArrayList<String>> wrappedLines = new ArrayList<>();
            ArrayList<String> currentLine = new ArrayList<>();
            int maxSize = 3;
            int size = 0;
            for (String dc : dataTokens) {
                if (size + 1 > maxSize) {
                    if (maxSize == 3) { maxSize = 10; }
                    wrappedLines.add(currentLine);
                    currentLine = new ArrayList<>();
                    size = 0;
                }
                currentLine.add(dc);
                size += 1;
            }
            wrappedLines.add(currentLine);

            dataLines = new ArrayList<>();

            for (ArrayList<String> wrappedLine : wrappedLines) {

                String dataLine = String.join(";", wrappedLine);
                dataLines.add(DATA_COLOR + dataLine);
            }
        }
        lore.add(LORE_SEPARATOR);
        lore.addAll(dataLines);

        return lore;
    }

    /**
     * Create a new writer object for the given chat text.
     */
    public static @NotNull BannerWriter createNew(String text) {

        int pos = 0;
        String[] dataTokens;
        {
            List<FormattedChar> fmtdChars = BannerFactory.parseText(text);

            dataTokens = new String[fmtdChars.size() + 2];
            dataTokens[0] = DATA_PREFIX;
            dataTokens[1] = String.valueOf(pos);

            for (int i = 2; i < dataTokens.length; i++) {
                FormattedChar fc = fmtdChars.get(i - 2);
                dataTokens[i] = "" +
                        fc.getTs().getChar() +
                        fc.getFg().getChar() +
                        fc.getBg().getChar() +
                        fc.getChar();
            }
        }
        String[] dataChars = Arrays.copyOfRange(dataTokens, 2, dataTokens.length);

        ItemStack banner = new ItemStack(Material.WHITE_BANNER, 1);
        ItemMeta itemMeta = banner.getItemMeta();
        assert itemMeta != null;

        itemMeta.setLore(mergeIntoLore(itemMeta.getLore(), dataTokens));
        banner.setItemMeta(itemMeta);

        BannerWriter bannerWriter = new BannerWriter(banner, dataChars, pos);
        bannerWriter.updateItem();

        return bannerWriter;
    }

    /**
     * Create a writer object using the given banner item.
     */
    public static BannerWriter createFrom(ItemStack writer) {

        if ((writer == null) || (!BannerFactory.BANNER_MATERIALS.contains(writer.getType()))) {
            return null;
        }
        if (writer.getItemMeta() == null) {
            return null;
        }
        String[] dataTokens = getLoreData(writer.getItemMeta().getLore());
        if (!Utils.arrayGet(dataTokens, 0, "").equals(DATA_PREFIX)) {
            return null;
        }
        int writerPos = Utils.intValueOf(Utils.arrayGet(dataTokens, 1, "-1"), -1);
        if (writerPos == -1) {
            return null;
        }
        String writerStyle = Utils.arrayGet(dataTokens, 2, "");
        if (writerStyle.isEmpty()) {
            return null;
        }
        String[] writerChars = Utils.arraySlice(dataTokens, 3, null);
        if (writerChars.length < 1) {
            return null;
        }
        return new BannerWriter(writer, writerChars, writerPos);
    }
}
