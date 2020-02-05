package me.cynadyde.bannertext;

/**
 * A character, including its text style,
 * foreground color, and background color.
 */
public class FormattedChar {

    public static final FormattedChar DEFAULT = new FormattedChar(
            PatternStyle.DEFAULT, PatternColor.BLACK, PatternColor.WHITE, ' ');

    private final PatternStyle TS;
    private final PatternColor FG, BG;
    private final char CH;

    public FormattedChar(PatternStyle ts, PatternColor fg, PatternColor bg, char ch) {
        this.FG = fg;
        this.BG = bg;
        this.TS = ts;
        this.CH = ch;
    }

    public char getChar() {
        return this.CH;
    }

    public PatternColor getFg() {
        return FG;
    }

    public PatternColor getBg() {
        return BG;
    }

    public PatternStyle getTs() {
        return TS;
    }

    @Override
    public String toString() {
        if (TS == PatternStyle.DEFAULT) {
            return String.format("&%c&%c%c%c", TS.getChar(), FG.getChar(), BG.getChar(), CH);
        }
        else {
            return String.format("&%c%c&%c%c", FG.getChar(), BG.getChar(), TS.getChar(), CH);
        }
    }
}
