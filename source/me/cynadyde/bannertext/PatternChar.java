package me.cynadyde.bannertext;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of banner designs that can be
 * used to represent a text character.
 */
public class PatternChar {

    public static final PatternChar DEFAULT = new PatternChar(new HashMap<>());

    private final Map<PatternStyle, PatternDesign> designs;

    public PatternChar(Map<PatternStyle, PatternDesign> designs) {
        this.designs = Collections.unmodifiableMap(designs);
    }

    /**
     * Gets a mapping of banner styles to banner designs
     * that can represent this text character.
     */
    public Map<PatternStyle, PatternDesign> getDesigns() {
        return designs;
    }

    /**
     * Converts the given pattern character into a banner, given
     * a text style, foreground color, and background color.
     */
    public ItemStack toBanner(PatternStyle ts, PatternColor fg, PatternColor bg) {

        if (!designs.containsKey(ts)) {
            if (designs.containsKey(PatternStyle.DEFAULT)) {
                ts = PatternStyle.DEFAULT;
            }
            else {
                return PatternDesign.DEFAULT.toBanner(fg, bg);
            }
        }
        return designs.getOrDefault(ts, PatternDesign.DEFAULT).toBanner(fg, bg);
    }
}
