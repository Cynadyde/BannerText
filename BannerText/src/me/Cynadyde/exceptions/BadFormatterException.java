package me.Cynadyde.exceptions;

public class BadFormatterException extends Exception {
	
	private static final long serialVersionUID = 3537825049239402548L;
	
	private String text;
	private String fmtr;
	private int i;
	
	public BadFormatterException(String text, String fmtr, int i) { 
		super("Invalid color formatter: " + String.format("'%s' at pos %d... ", fmtr, i) 
		+ "Should match '&[0-f][0-f]' or '&&'... See \"/bannertext colors\" for [0-f] values."); 
		this.text = text;
		this.fmtr = fmtr;
		this.i = i;
	}
	public String getText() {
		return this.text;
	}
	public String getFormatter() {
		return this.fmtr;
	}
	public int getPos() {
		return this.i;
	}
}
