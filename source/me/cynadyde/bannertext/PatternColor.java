package me.cynadyde.bannertext;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of different text colors a banner
 * can be created with.
 */
public enum PatternColor {

    BLACK(Material.BLACK_BANNER, DyeColor.BLACK, ChatColor.BLACK),
    BROWN(Material.BROWN_BANNER, DyeColor.BROWN, ChatColor.DARK_BLUE),
    GREEN(Material.GREEN_BANNER, DyeColor.GREEN, ChatColor.DARK_GREEN),
    CYAN(Material.CYAN_BANNER, DyeColor.CYAN, ChatColor.DARK_AQUA),
    RED(Material.RED_BANNER, DyeColor.RED, ChatColor.DARK_RED),
    PURPLE(Material.PURPLE_BANNER, DyeColor.PURPLE, ChatColor.DARK_PURPLE),
    ORANGE(Material.ORANGE_BANNER, DyeColor.ORANGE, ChatColor.GOLD),
    LIGHT_GRAY(Material.LIGHT_GRAY_BANNER, DyeColor.LIGHT_GRAY, ChatColor.GRAY),
    GRAY(Material.GRAY_BANNER, DyeColor.GRAY, ChatColor.DARK_GRAY),
    BLUE(Material.BLUE_BANNER, DyeColor.BLUE, ChatColor.BLUE),
    LIME(Material.LIME_BANNER, DyeColor.LIME, ChatColor.GREEN),
    LIGHT_BLUE(Material.LIGHT_BLUE_BANNER, DyeColor.LIGHT_BLUE, ChatColor.AQUA),
    MAGENTA(Material.MAGENTA_BANNER, DyeColor.MAGENTA, ChatColor.RED),
    PINK(Material.PINK_BANNER, DyeColor.PINK, ChatColor.LIGHT_PURPLE),
    YELLOW(Material.YELLOW_BANNER, DyeColor.YELLOW, ChatColor.YELLOW),
    WHITE(Material.WHITE_BANNER, DyeColor.WHITE, ChatColor.WHITE);

    private final Material banner;
    private final DyeColor dye;
    private final ChatColor chat;

    PatternColor(Material banner, DyeColor dye, ChatColor chat) {
        this.banner = banner;
        this.dye = dye;
        this.chat = chat;
    }

    public char getChar() {
        return chat.getChar();
    }

    public Material getMat() {
        return banner;
    }

    public DyeColor getDyeColor() {
        return dye;
    }

    public static @NotNull PatternColor getByChar(char c) throws IllegalArgumentException {
        for (PatternColor color : values()) {
            if (color.getChar() == Character.toLowerCase(c)) {
                return color;
            }
        }
        throw new IllegalArgumentException("invalid color char: " + c);
    }
}
