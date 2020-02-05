package me.cynadyde.bannertext;

import org.bukkit.block.banner.PatternType;

/**
 * Represents a pattern shape that can be
 * added to a banner.
 */
public enum PatternShape {

    BASE("Banner", PatternType.BASE),
    STRIPE_BOTTOM("Base", PatternType.STRIPE_BOTTOM),
    STRIPE_TOP("Chief", PatternType.STRIPE_TOP),
    STRIPE_LEFT("Pale Dexter", PatternType.STRIPE_LEFT),
    STRIPE_RIGHT("Pale Sinister", PatternType.STRIPE_RIGHT),
    STRIPE_CENTER("Pale", PatternType.STRIPE_CENTER),
    STRIPE_MIDDLE("Fess", PatternType.STRIPE_MIDDLE),
    STRIPE_DOWNRIGHT("Bend", PatternType.STRIPE_DOWNRIGHT),
    STRIPE_DOWNLEFT("Bend Sinister", PatternType.STRIPE_DOWNLEFT),
    STRIPE_SMALL("Paly", PatternType.STRIPE_SMALL),
    CROSS("Saltire", PatternType.CROSS),
    STRAIGHT_CROSS("Cross", PatternType.STRAIGHT_CROSS),
    DIAGONAL_LEFT("Per Bend Sinister", PatternType.DIAGONAL_LEFT),
    DIAGONAL_RIGHT_MIRROR("Per Bend", PatternType.DIAGONAL_RIGHT_MIRROR),
    DIAGONAL_LEFT_MIRROR("Per Bend Inverted", PatternType.DIAGONAL_LEFT_MIRROR),
    DIAGONAL_RIGHT("Per Bend Sinister Inverted", PatternType.DIAGONAL_RIGHT),
    HALF_VERTICAL("Per Pale", PatternType.HALF_VERTICAL),
    HALF_VERTICAL_MIRROR("Per Pale Inverted", PatternType.HALF_VERTICAL_MIRROR),
    HALF_HORIZONTAL("Per Fess", PatternType.HALF_HORIZONTAL),
    HALF_HORIZONTAL_MIRROR("Per Fess Inverted", PatternType.HALF_HORIZONTAL_MIRROR),
    SQUARE_BOTTOM_LEFT("Base Dexter Canton", PatternType.SQUARE_BOTTOM_LEFT),
    SQUARE_BOTTOM_RIGHT("Base Sinister Canton", PatternType.SQUARE_BOTTOM_RIGHT),
    SQUARE_TOP_LEFT("Chief Dexter Canton", PatternType.SQUARE_TOP_LEFT),
    SQUARE_TOP_RIGHT("Chief Sinister Canton", PatternType.SQUARE_TOP_RIGHT),
    TRIANGLE_BOTTOM("Chevron", PatternType.TRIANGLE_BOTTOM),
    TRIANGLE_TOP("Inverted Chevron", PatternType.TRIANGLE_TOP),
    TRIANGLES_BOTTOM("Base Indented", PatternType.TRIANGLES_BOTTOM),
    TRIANGLES_TOP("Chief Indented", PatternType.TRIANGLES_TOP),
    CIRCLE_MIDDLE("Roundel", PatternType.CIRCLE_MIDDLE),
    RHOMBUS_MIDDLE("Lozenge", PatternType.RHOMBUS_MIDDLE),
    BORDER("Bordure", PatternType.BORDER),
    CURLY_BORDER("Bordure Indented", PatternType.CURLY_BORDER),
    BRICKS("Field Masoned", PatternType.BRICKS),
    GRADIENT("Gradient", PatternType.GRADIENT),
    GRADIENT_UP("Base Gradient", PatternType.GRADIENT_UP),
    CREEPER("Creeper Charge", PatternType.CREEPER),
    SKULL("Skull Charge", PatternType.SKULL),
    FLOWER("Flower Charge", PatternType.FLOWER),
    MOJANG("Mojang Charge", PatternType.MOJANG);

    private final String display;
    private final PatternType type;

    PatternShape(String display, PatternType type) {
        this.display = display;
        this.type = type;
    }

    public String getDisplay() {
        return this.display;
    }

    public PatternType getType() {
        return this.type;
    }

    public static PatternShape getByDisplay(String display) {
        for (PatternShape pattern : values()) {
            if (pattern.getDisplay().equalsIgnoreCase(display)) {
                return pattern;
            }
        }
        return null;
    }

    public static PatternShape getByType(PatternType type) {
        for (PatternShape pattern : values()) {
            if (pattern.getType().equals(type)) {
                return pattern;
            }
        }
        return null;
    }
}
