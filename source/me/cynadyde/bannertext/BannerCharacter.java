package me.cynadyde.bannertext;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of banner design templates that represent a text symbol.
 */
public class BannerCharacter {

    public static final BannerCharacter DEFAULT = new BannerCharacter(new HashMap<>());

    private final Map<PatternStyle, BannerDesign> designs;

    public BannerCharacter(Map<PatternStyle, BannerDesign> designs) {
        this.designs = Collections.unmodifiableMap(designs);
    }

    /**
     * Gets a mapping of banner styles to banner designs
     * that can represent this text character.
     */
    public Map<PatternStyle, BannerDesign> getDesigns() {
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
                return BannerDesign.DEFAULT.toBanner(fg, bg);
            }
        }
        return designs.getOrDefault(ts, BannerDesign.DEFAULT).toBanner(fg, bg);
    }
}
