package me.cynadyde.bannertext;

import org.bukkit.ChatColor;

/**
 * An enumeration of different text styles a banner
 * can be created for.
 */
public enum PatternStyle {

    DEFAULT(ChatColor.RESET),
    OBFUSCATED(ChatColor.MAGIC),
    BOLD(ChatColor.BOLD),
    STRIKE(ChatColor.STRIKETHROUGH),
    UNDERLINE(ChatColor.UNDERLINE),
    ITALIC(ChatColor.ITALIC);

    private final ChatColor chat;

    PatternStyle(ChatColor chat) {
        this.chat = chat;
    }

    public char getChar() {
        return chat.getChar();
    }

    public static PatternStyle getByChar(char c) {
        for (PatternStyle style : values()) {
            if (style.getChar() == Character.toLowerCase(c)) {
                return style;
            }
        }
        return null;
    }
}
