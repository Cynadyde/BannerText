package me.cynadyde.bannertext;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides utility functions for the BannerText plugin.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Formats the given message's ampersands into chat color, then formats any given objects into it.
     */
    public static @NotNull String chatFormat(@NotNull String message, Object... objects) {
        try {
            return String.format(ChatColor.translateAlternateColorCodes('&', message), objects);
        }
        catch (IllegalFormatException ex) {
            return String.format("%s %% (%s)", message, Arrays.stream(objects)
                    .map(Objects::toString).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Formats a given text to be word wrapped for an item's lore,
     * while keeping the correct, previously applied chat formatting.
     */
    public static List<String> loreFormat(@NotNull String text) {
        int maxLines = 32;  // magic value
        int maxWidth = 32;  // magic value
        int maxChars = 2048;  // magic value
        List<String> lore = new ArrayList<>();
        String linePrefix = "";

        text = ChatColor.LIGHT_PURPLE + text;

        parseLines:
        for (String line : text.substring(0, Math.min(maxChars, text.length())).split("\n")) {
            // if the number of lines overflows, add an ellipses
            if (lore.size() >= maxLines) {
                String lastLine = lore.remove(lore.size() - 1);
                lore.add(lastLine.substring(0, Math.min(maxWidth - 3, lastLine.length())) + "...");
                break;
            }
            else if (line.length() <= maxWidth) {
                line = linePrefix + line;
                lore.add(line);
                linePrefix = ChatColor.getLastColors(line);
            }
            else {
                String remainder = line;
                while (remainder.length() > 0) {
                    // if the number of lines overflows, add an ellipses
                    if (lore.size() >= maxLines) {
                        String lastLine = lore.remove(lore.size() - 1);
                        lore.add(lastLine.substring(0, Math.min(maxWidth - 3, lastLine.length())) + "...");
                        break parseLines;
                    }
                    // finding the right spot to break the line is a little complex due to the formatter chars...
                    String parcel = remainder.substring(0, Math.min(maxWidth, remainder.length()));
                    int parcelFormattersLength = parcel.length() - ChatColor.stripColor(parcel).length();
                    parcel = remainder.substring(0, Math.min(maxWidth + parcelFormattersLength, remainder.length()));

                    // as long as the line's still too long, try to split between words and not within formatters
                    if (parcel.length() < remainder.length()) {

                        int wordBoundary = parcel.lastIndexOf(" ");
                        if (wordBoundary > 0) {
                            parcel = parcel.substring(0, wordBoundary + 1);
                        }
                        if (parcel.charAt(parcel.length() - 1) == ChatColor.COLOR_CHAR) {
                            ChatColor code = ChatColor.getByChar(remainder.charAt(parcel.length()));
                            if (code != null) {
                                parcel = parcel.substring(0, parcel.length() - 1);
                            }
                        }
                    }
                    remainder = remainder.substring(/*from*/ parcel.length() /*to end*/);
                    parcel = linePrefix + parcel;
                    lore.add(parcel);
                    linePrefix = ChatColor.getLastColors(parcel);
                }
            }
        }
        return lore;
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
     * Safely returns a slice of the given array.
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
