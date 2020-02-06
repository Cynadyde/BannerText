package me.cynadyde.bannertext;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a held banner that is used by a player
 * to as a writer tool for some text.
 */
public class BannerWriter {

    public static final String WRITER_NAME_TEMPLATE = "&8< &3&lBannerText Writer &8| &3%d of %d &8>";

    private final ItemStack item;
    private final List<FormattedChar> chars;
    private int pos;

    public BannerWriter(@NotNull List<FormattedChar> chars) {
        this.item = new ItemStack(Material.WHITE_BANNER, 1);
        this.chars = Collections.unmodifiableList(new ArrayList<>(chars));
        this.pos = 0;
        updateItem();
    }

    public BannerWriter(@NotNull ItemStack writer) throws IllegalArgumentException {
        this.item = Objects.requireNonNull(writer);

        List<FormattedChar> chars = new ArrayList<>();
        String text = Utils.getLoreData(writer, "text");
        for (int i = 0; i < text.length(); i += 4) {
            chars.add(FormattedChar.fromString(text.substring(i, i + 4))); // throws illegal arg
        }
        Integer pos = Utils.getInt(Utils.getLoreData(writer, "pos"));
        if (pos == null) {
            throw new IllegalArgumentException("writer item had no pos data");
        }
        this.chars = Collections.unmodifiableList(chars);
        this.pos = pos;
    }

    public List<FormattedChar> getChars() {
        return chars;
    }

    public int getPos() {
        return pos;
    }

    public ItemStack getCurBanner() {
        FormattedChar c = chars.get(pos);
        return BannerFactory.getBanner(c.getChar(), c.getTs(), c.getFg(), c.getBg());
    }

    public void setPos(int newPos) {
        this.pos = Math.floorMod(newPos, chars.size());
        updateItem();
    }

    public void augPos(int amount) {
        setPos(pos + amount);
    }

    public ItemStack toItem() {
        return item.clone();
    }

    public void updateItem() {

        Utils.setDisplayName(item, Utils.chatFormat(WRITER_NAME_TEMPLATE, getPos() + 1, chars.size()));
        Utils.setLoreData(item, "pos", String.valueOf(pos));
        Utils.setLoreData(item, "text", chars.stream().map(FormattedChar::toString).collect(Collectors.joining()));

        if (!(item.getItemMeta() instanceof BannerMeta)) {
            throw new IllegalStateException("writer item does not have BannerMeta");
        }
        BannerMeta meta = (BannerMeta) item.getItemMeta();

        ItemStack modelBanner = getCurBanner();
        item.setType(modelBanner.getType());
        meta.setPatterns(Objects.requireNonNull((BannerMeta) modelBanner.getItemMeta()).getPatterns());

        item.setItemMeta(meta);
    }

    /**
     * Creates a writer object using the given valid item,
     * returning null if there was an illegal argument exception.
     */
    public static BannerWriter from(ItemStack writer) {
        try {
            return new BannerWriter(writer);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
