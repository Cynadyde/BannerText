package me.cynadyde.bannertext;

import org.bukkit.block.banner.Pattern;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A banner design template for a character of a specific style (i.e. default, bold, underline, etc.)
 */
public class BannerDesign {

    public static final BannerDesign DEFAULT = new BannerDesign(new LinkedHashMap<>());

    private final Map<PatternShape, Boolean> shapes;

    public BannerDesign(LinkedHashMap<PatternShape, Boolean> shapes) {
        this.shapes = Collections.unmodifiableMap(shapes);
    }

    /**
     * Gets the pattern shapes that make up this design, each mapped to a boolean
     * that represents its layer (true = foreground; false = background).
     */
    public @NotNull Map<PatternShape, Boolean> getShapes() {
        return shapes;
    }

    /**
     * Creates a banner for this generic design, given a
     * foreground and background color.
     */
    public @NotNull ItemStack toBanner(PatternColor fg, PatternColor bg) {

        ItemStack banner = new ItemStack(bg.getMat(), 1);
        BannerMeta bannerMeta = Objects.requireNonNull((BannerMeta) banner.getItemMeta());

        for (PatternShape shape : shapes.keySet()) {

            PatternColor color = shapes.get(shape) ? fg : bg;
            if (shape.equals(PatternShape.BASE)) {
                banner.setType(color.getMat());
            }
            else {
                bannerMeta.addPattern(new Pattern(color.getDyeColor(), shape.getType()));
            }
        }
        banner.setItemMeta(bannerMeta);
        return banner;
    }
}
