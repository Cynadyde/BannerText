package me.cynadyde.bannertext;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Provides utility functions for the BannerText plugin.
 */
public class Utils {

    /**
     * Formats the given message's ampersands into chat color, then formats the given objects into it.
     */
    public static String chatFormat(String message, Object... objects) {
        if (message == null) { return ""; }
        try {
            return String.format(ChatColor.translateAlternateColorCodes('&', message), objects);
        }
        catch (IllegalFormatException ex) {
            return ex.getMessage();
        }
    }

    /**
     * Attempts to parse the given string into an integer,
     * returning null if there was a number format exception.
     */
    public static @Nullable Integer getInt(@NotNull String integer) {
        try {
            return Integer.parseInt(integer);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Put the given items into the player's inv, spilling out anything that doesn't fit.
     */
    public static void giveItems(Player player, ItemStack... items) {

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(items);
        if (!leftovers.isEmpty()) {
            for (ItemStack leftover : leftovers.values()) {
                player.getWorld().dropItem(player.getLocation(), leftover);
            }
        }
    }

    /**
     * Sets the display name of a given item stack.
     */
    public static void setDisplayName(@Nullable ItemStack item, @Nullable String displayName) {

        if (item == null) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return;
        }
        itemMeta.setDisplayName(displayName);
        item.setItemMeta(itemMeta);
    }

    /**
     * Adds the specified lore tag to the given item's lore.
     */
    public static void addLoreTag(@Nullable ItemStack item, String tag) {

        if (item == null) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.add(tag);

        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
    }

    /**
     * Checks if the given item has the specified tag in its lore.
     */
    public static boolean hasLoreTag(@Nullable ItemStack item, @NotNull String tag) {

        String rawTag = ChatColor.stripColor(tag).trim();

        if (item == null) {
            return false;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return false;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.isEmpty()) {
            return false;
        }

        for (String line : lore) {

            if (ChatColor.stripColor(line).trim().equalsIgnoreCase(rawTag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets data under the specified lore key from the given item.
     * If it doesn't exist, an empty string is returned.
     */
    public static @NotNull String getLoreData(@Nullable ItemStack item, @NotNull String key) {

        String rawKey = ChatColor.stripColor(key).trim().toLowerCase();

        if (item == null) {
            return "";
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return "";
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.isEmpty()) {
            return "";
        }
        for (String line : lore) {

            String rawLine = ChatColor.stripColor(line).trim().toLowerCase();
            if (rawLine.startsWith(rawKey)) {

                return line.substring(line.indexOf(": ") + 2);
            }
        }
        return "";
    }

    /**
     * Sets the specified key in the given item's lore. If the value is null, the key will be removed.
     */
    public static void setLoreData(@Nullable ItemStack item, @NotNull String key, @Nullable String value) {

        if (key.contains(": ")) {
            throw new IllegalArgumentException("lore key cannot contain ': '");
        }

        String rawKey = ChatColor.stripColor(key).trim().toLowerCase();

        if (item == null) {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) {
            return;
        }

        List<String> lore = itemMeta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }

        Integer index = null;
        for (int i = 0; i < lore.size(); i++) {

            String line = ChatColor.stripColor(lore.get(i)).trim().toLowerCase();
            if (line.startsWith(rawKey)) {
                index = i;
                break;
            }
        }
        if (index == null) {

            if (value != null) {
                lore.add(key + ": " + value);
            }
        }
        else {
            if (value == null) {
                lore.remove((int) index);
            }
            else {
                lore.set(index, key + ": " + value);
            }
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
    }

//    /** Safely attempts to convert the given string into an integer */
//    public static int intValueOf(final String string, final int defaultVal) {
//        try { return (int) Math.floor(Double.parseDouble(string)); }
//        catch (NumberFormatException e) { return defaultVal; }
//    }
//
//    /** Safely attempts to retrieve an object from the given array */
//    public static <T> T arrayGet(final T[] array, int index, final T defaultVal) {
//        if (array.length == 0) {
//            return defaultVal;
//        }
//        if (index < 0) {
//            index += array.length;
//        }
//        if (!((0 <= index) && (index < array.length))) {
//            return defaultVal;
//        }
//        return array[index];
//    }
//
//    /** Finds the index of an object or -1 within the given array */
//    public static <T> int arrayIndexOf(final T[] array, final T v) {
//
//        if (array == null || array.length == 0) {
//            return -1;
//        }
//
//        for (int i = 0; i < array.length; i++) {
//            T e = array[i];
//            if (Objects.equals(e, v)) {
//                return i;
//            }
//        }
//        return -1;
//    }
//

    /**
     * Safely returns a slice of the given array
     */
    public static <T> T[] arraySlice(final T[] array, Integer from, Integer to) {

        if (array.length == 0) {
            return array;
        }
        if (from == null) {
            from = 0;
        }
        if (to == null) {
            to = array.length;
        }
        if (from < 0) {
            from = Math.max(0, from + array.length);
        }
        if (to < 0) {
            to += array.length;
        }
        if (to > array.length) {
            to = (array.length);
        }
        if ((array.length <= from) | (to <= from)) {
            //noinspection unchecked
            return (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
        }
        return Arrays.copyOfRange(array, from, to);
    }
}
