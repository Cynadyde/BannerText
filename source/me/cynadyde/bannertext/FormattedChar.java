package me.cynadyde.bannertext;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

    public static @NotNull FormattedChar fromString(String string) throws IllegalArgumentException {
        if (string.length() != 4) {
            throw new IllegalArgumentException("string length must be 4");
        }
        return new FormattedChar(
                PatternStyle.getByChar(string.charAt(0)),
                PatternColor.getByChar(string.charAt(1)),
                PatternColor.getByChar(string.charAt(2)),
                string.charAt(3)
        );
    }
}
