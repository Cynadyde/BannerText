package me.Cynadyde;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;

public enum PatternColor {
	
	BLACK('0', DyeColor.BLACK, ChatColor.BLACK),
	
	BROWN('1', DyeColor.BROWN, ChatColor.DARK_BLUE),
	
	GREEN('2', DyeColor.GREEN, ChatColor.DARK_GREEN),
	
	CYAN('3', DyeColor.CYAN, ChatColor.DARK_AQUA),
	
	RED('4', DyeColor.RED, ChatColor.DARK_RED),
	
	PURPLE('5', DyeColor.PURPLE, ChatColor.DARK_PURPLE),
	
	ORANGE('6', DyeColor.ORANGE, ChatColor.GOLD),
	
	LIGHT_GRAY('7', DyeColor.SILVER, ChatColor.GRAY),
	
	GRAY('8', DyeColor.GRAY, ChatColor.DARK_GRAY),
	
	BLUE('9', DyeColor.BLUE, ChatColor.BLUE),
	
	LIME('a', DyeColor.LIME, ChatColor.GREEN),
	
	LIGHT_BLUE('b', DyeColor.LIGHT_BLUE, ChatColor.AQUA),
	
	MAGENTA('c', DyeColor.MAGENTA, ChatColor.RED),
	
	PINK('d', DyeColor.PINK, ChatColor.LIGHT_PURPLE),
	
	YELLOW('e', DyeColor.YELLOW, ChatColor.YELLOW),
	
	WHITE('f', DyeColor.WHITE, ChatColor.WHITE);
	
	private char charID;
	private DyeColor dyeID;
	private ChatColor chatID;
	
	private PatternColor(char charID, DyeColor dyeID, ChatColor chatID) {
		this.charID = charID;
		this.dyeID = dyeID;
		this.chatID = chatID;
	}
	public char getChar() {
		return this.charID;
	}
	public String getDisplay() {
		String[] tokens = this.name().split("_");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			tokens[i] = token.substring(0, 1).toUpperCase() + token.substring(1);
		}
		return String.join(" ", tokens);
	}
	public DyeColor getDyeColor() {
		return this.dyeID;
	}
	public ChatColor getChatColor() {
		return this.chatID;
	}
	public static PatternColor getByValue(String value) {
		for (PatternColor color : PatternColor.values()) {
			if (color.name().equals(value)) {
				return color;
			}
		}
		return null;
	}
	public static PatternColor getByChar(char c) {
		for (PatternColor color: PatternColor.values()) {
			if (color.getChar() == Character.toLowerCase(c)) {
				return color;
			}
		}
		return null;
	}
	public static PatternColor getByDyeColor(DyeColor dyeID) {
		for (PatternColor color : PatternColor.values()) {
			if (color.getDyeColor().equals(dyeID)) {
				return color;
			}
		}
		return null;
	}
	public static PatternColor getByChatColor(ChatColor chatColor) {
		for (PatternColor color : PatternColor.values()) {
			if (color.getChatColor().equals(chatColor)) {
				return color;
			}
		}
		return null;
	}
}
