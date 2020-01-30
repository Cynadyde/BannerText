package me.cynadyde.bannertext;

public class FormattedChar {
	
	public static final FormattedChar defaultChar = new FormattedChar(PatternColor.BLACK, PatternColor.WHITE, ' ');
	
	private char c;
	private PatternColor fg, bg;
	
	public FormattedChar(PatternColor fg, PatternColor bg, char c) {
		this.c = c;
		this.fg = fg;
		this.bg = bg;
	}
	
	public char getChar() {
		return this.c;
	}
	
	public PatternColor getFgColor() {
		return this.fg;
	}
	
	public PatternColor getBgColor() {
		return this.bg;
	}
	
	@Override
	public String toString() {
		return String.format("&%c%c%c", fg.getChar(), bg.getChar(), c);
	}
}