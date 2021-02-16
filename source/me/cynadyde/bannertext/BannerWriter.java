package me.cynadyde.bannertext;

import me.cynadyde.bannertext.BannerTextPlugin.DataKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents an item that the player can use as a wand
 * to spawn down banners for a stored string of text.
 */
public class BannerWriter {

    public static final String WRITER_NAME_TEMPLATE = "&8< &3&lBannerText Writer &8| &3%d of %d &8>";

    private final ItemStack item;

    public BannerWriter(@NotNull List<ParsedSlot> slots) {
        this.item = new ItemStack(Material.WHITE_BANNER, 1);

        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof BannerMeta)) {
            throw new IllegalStateException("writer item does not have BannerMeta");
        }
        DataKey.WRITER_TEXT.setData(itemMeta, slots.stream().map(ParsedSlot::toString).collect(Collectors.joining()));
        DataKey.WRITER_POS.setData(itemMeta, 0);

        item.setItemMeta(itemMeta);
        updateItem();
    }

    public BannerWriter(@NotNull ItemStack writer) throws IllegalArgumentException {
        if (!(writer.getItemMeta() instanceof BannerMeta)) {
            throw new IllegalArgumentException("that writer item does not have a BannerMeta");
        }
        if (!DataKey.WRITER_POS.hasData(writer.getItemMeta())) {
            throw new IllegalArgumentException("that writer item is missing pos data");
        }
        if (!DataKey.WRITER_TEXT.hasData(writer.getItemMeta())) {
            throw new IllegalArgumentException("that writer item is missing slots data");
        }
        this.item = writer.clone();
    }

    public @NotNull List<ParsedSlot> getSlots() {
        List<ParsedSlot> slots = new ArrayList<>();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            String text = DataKey.WRITER_TEXT.getData(itemMeta);
            if (text != null) {
                for (int i = 0; i < text.length(); i += 4) {
                    try {
                        slots.add(ParsedSlot.fromString(text.substring(i, i + 4)));
                    }
                    catch (IllegalArgumentException ex) {
                        slots.add(ParsedSlot.DEFAULT);
                    }
                }
            }
        }
        if (slots.isEmpty()) {
            slots.add(ParsedSlot.DEFAULT);
        }
        return Collections.unmodifiableList(slots);
    }

    public int getPos() {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            Integer pos = DataKey.WRITER_POS.getData(itemMeta);
            if (pos != null && pos >= 0) {
                return pos;
            }
        }
        return 0;
    }

    public @NotNull ItemStack getCurBanner() {
        ParsedSlot slot = getSlots().get(getPos());
        ItemStack banner = BannerFactory.getBanner(slot.getChar(), slot.getTs(), slot.getFg(), slot.getBg());
        ItemMeta meta = banner.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(null);
            meta.setLore(null);
            DataKey.WRITER_TEXT.removeData(meta);
            banner.setItemMeta(meta);
        }
        return banner;
    }

    public void setPos(int newPos) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            DataKey.WRITER_POS.setData(itemMeta, Math.floorMod(newPos, getSlots().size()));
            item.setItemMeta(itemMeta);
        }
        updateItem();
    }

    public void scrollNext() {
        setPos(getPos() + 1);
    }

    public void scrollPrev() {
        setPos(getPos() - 1);
    }

    public @NotNull ItemStack toItem() {
        return item.clone();
    }

    public void updateItem() {
        if (!(item.getItemMeta() instanceof BannerMeta)) {
            throw new IllegalStateException("writer item does not have BannerMeta");
        }
        ItemStack modelBanner = getCurBanner();
        item.setType(modelBanner.getType());

        BannerMeta itemMeta = (BannerMeta) item.getItemMeta();
        BannerMeta modelMeta;
        {
            if (!(modelBanner.getItemMeta() instanceof BannerMeta)) {
                throw new IllegalStateException("writer's model item did not have BannerMeta!");
            }
            modelMeta = (BannerMeta) modelBanner.getItemMeta();
        }

        List<ParsedSlot> slots = getSlots();
        int pos = getPos();

        String displayName = Utils.chatFormat(WRITER_NAME_TEMPLATE, pos + 1, slots.size());
        String description = Utils.chatFormat(BannerTextPlugin.Msg.WRITER_USAGE.getTemplate());

        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(Utils.loreFormat(description));
        itemMeta.setPatterns(modelMeta.getPatterns());

        item.setItemMeta(itemMeta);
    }

    public static @Nullable BannerWriter from(ItemStack writer) {
        try {
            return new BannerWriter(writer);
        }
        catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
