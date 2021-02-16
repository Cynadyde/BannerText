package me.cynadyde.bannertext;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A slot containing four chars: a text style, foreground color, background color, and text character.
 */
public class ParsedSlot {

    public static final ParsedSlot DEFAULT = new ParsedSlot(
            PatternStyle.DEFAULT, PatternColor.WHITE, PatternColor.BLACK, ' ');

    private final PatternStyle TS;
    private final PatternColor FG, BG;
    private final char CH;

    public ParsedSlot(PatternStyle ts, PatternColor fg, PatternColor bg, char ch) {
        this.FG = Objects.requireNonNull(fg);
        this.BG = Objects.requireNonNull(bg);
        this.TS = Objects.requireNonNull(ts);
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
        return "" + TS.getChar() + FG.getChar() + BG.getChar() + CH;
    }

    public static @NotNull ParsedSlot fromString(String string) throws IllegalArgumentException {
        if (string.length() != 4) {
            throw new IllegalArgumentException("string length must be 4");
        }
        return new ParsedSlot(
                PatternStyle.getByChar(string.charAt(0)),
                PatternColor.getByChar(string.charAt(1)),
                PatternColor.getByChar(string.charAt(2)),
                string.charAt(3)
        );
    }
}
