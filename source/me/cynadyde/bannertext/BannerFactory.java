package me.cynadyde.bannertext;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Facilitates the creation of banners and banner text writers.
 */
public class BannerFactory {

    private static final Map<Character, PatternChar> patternChars = new HashMap<>();

    public static final Pattern FORMATTER_PATTERN = Pattern.compile("&(?:([klmnor])|([0-9a-f])([0-9a-f]))");
    public static final String RESULT_NAME_TEMPLATE = Utils.chatFormat("&8< &3'%c&%c%c' &b&l'%c' &8| &7%d of %d &8>");
    public static final String PACKAGE_NAME_TEMPLATE = Utils.chatFormat("&8< &b&l%s &8| &7%d of %d &8>");

    public static final Set<Material> BANNER_MATERIALS;

    static {
        Set<Material> mats = new HashSet<>();
        for (PatternColor c : PatternColor.values()) {
            mats.add(c.getMat());
        }
        BANNER_MATERIALS = Collections.unmodifiableSet(mats);
    }

    /**
     * Strips all formatting codes in the given text.
     */
    public static String stripFormat(String chatText) {
        return FORMATTER_PATTERN.matcher(chatText.replace("&&", "&")).replaceAll("");
    }

    /**
     * Looks through the given text and specifies the text style, color, and background of each character.
     */
    public static List<FormattedChar> parseText(String chatText) {

        String text = chatText.replace("&&", "&");
        Matcher matcher = FORMATTER_PATTERN.matcher(text);
        List<FormattedChar> chars = new ArrayList<>();

        PatternStyle ts = PatternStyle.DEFAULT;
        PatternColor fg = PatternColor.WHITE;
        PatternColor bg = PatternColor.BLACK;

        String tsStr, fgStr, bgStr;
        int i = 0, stop, pickup;

        while (i < text.length()) {

            if (matcher.find()) {
                tsStr = matcher.group(1);
                fgStr = matcher.group(2);
                bgStr = matcher.group(3);

                stop = matcher.start();
                pickup = matcher.end();
            }
            else {
                tsStr = fgStr = bgStr = null;
                stop = text.length();
                pickup = -1;
            }
            while (i < stop) {
                chars.add(new FormattedChar(ts, fg, bg, text.charAt(i)));
                i++;
            }
            if (tsStr != null) { ts = PatternStyle.getByChar(tsStr.charAt(0)); }
            if (fgStr != null) { fg = PatternColor.getByChar(fgStr.charAt(0)); }
            if (bgStr != null) { bg = PatternColor.getByChar(bgStr.charAt(0)); }
            if (pickup != -1) { i = pickup; }

            if (ts == PatternStyle.DEFAULT) {
                fg = PatternColor.WHITE;
                bg = PatternColor.BLACK;
            }
        }
        return chars;
    }

    /**
     * Gets a banner to represent the given character in the given text style,
     * with the given foreground and background colors.
     */
    public static @NotNull ItemStack getBanner(char character, PatternStyle ts, PatternColor fg, PatternColor bg) {

        return patternChars.getOrDefault(character, PatternChar.DEFAULT).toBanner(ts, fg, bg);
    }

    /**
     * Converts the given text into a list of text banners.
     */
    public static List<ItemStack> buildBanners(String text) {

        List<FormattedChar> fText = BannerFactory.parseText(text);
        List<ItemStack> banners = new ArrayList<>();

        for (int i = 0; i < fText.size(); i++) {

            FormattedChar c = fText.get(i);
            ItemStack banner = getBanner(c.getChar(), c.getTs(), c.getFg(), c.getBg());

            ItemMeta bannerMeta = banner.getItemMeta();
            if (bannerMeta != null) {
                bannerMeta.setDisplayName(String.format(RESULT_NAME_TEMPLATE,
                        c.getTs().getChar(), c.getFg().getChar(), c.getBg().getChar(),
                        c.getChar(), i, fText.size() - 1));

                banner.setItemMeta(bannerMeta);
            }
            banners.add(banner);
        }
        return banners;
    }

    /**
     * Packages the given banners into a list of chests.
     */
    public static List<ItemStack> packageBanners(List<ItemStack> banners, String name) {

        if (name.length() > 16) {
            name = name.substring(0, 13) + "...";
        }

        List<List<ItemStack>> stacks = new ArrayList<>();
        {
            List<ItemStack> stack = new ArrayList<>();
            int size = 0;

            for (ItemStack banner : banners) {
                if (size >= 27) {
                    stacks.add(stack);
                    stack = new ArrayList<>();
                    size = 0;
                }
                stack.add(banner);
                size++;
            }
            stacks.add(stack);
        }
        List<ItemStack> chests = new ArrayList<>();

        for (int i = 0; i < stacks.size(); i++) {
            List<ItemStack> stack = stacks.get(i);

            ItemStack chest = new ItemStack(Material.CHEST, 1);
            BlockStateMeta chestMeta = Objects.requireNonNull((BlockStateMeta) chest.getItemMeta());
            Chest chestState = (Chest) chestMeta.getBlockState();

            chestMeta.setDisplayName(String.format(PACKAGE_NAME_TEMPLATE, name, i, stacks.size() - 1));
            chestState.getBlockInventory().addItem(stack.toArray(new ItemStack[0]));

            chestMeta.setBlockState(chestState);
            chest.setItemMeta(chestMeta);

            chests.add(chest);
        }
        return chests;
    }

    /**
     * Creates a banner writer for the given text.
     */
    public static ItemStack buildBannerWriter(String text) {
        return BannerWriter.createNew(text).getItem();
    }

    /**
     * Parses the plugin's config to reload a collection of valid pattern characters.
     */
    public static void reloadPatternChars(BannerTextPlugin plugin) {

        final String KEY = "banners";

        patternChars.clear();

        ConfigurationSection yaml = plugin.getConfig().getConfigurationSection(KEY);
        if (yaml == null) {
            plugin.getLogger().warning("Malformed config: missing '" + KEY + "' node. " +
                    "Remove the broken config to regenerate defaults!");
            return;
        }

        for (String charKey : yaml.getKeys(false)) {
            if (charKey.length() != 1) {
                plugin.getLogger().warning("Malformed config node: " +
                        "key isn't a single character: '" + KEY + "." + charKey + "'.");
                continue;
            }
            char character;
            if (!plugin.getConfig().getBoolean("case-sensitive")) {
                character = charKey.toUpperCase().charAt(0);
            }
            else {
                character = charKey.charAt(0);
            }

            Map<PatternStyle, PatternDesign> designs = new HashMap<>();

            ConfigurationSection ymlDesigns = Objects.requireNonNull(yaml.getConfigurationSection(charKey));
            for (String styleKey : ymlDesigns.getKeys(false)) {

                Map<PatternShape, Boolean> shapes = new HashMap<>();

                List<Map<?, ?>> ymlShapes = ymlDesigns.getMapList(styleKey);

                for (int i = 0; i < ymlShapes.size(); i++) {
                    Map<?, ?> entry = ymlShapes.get(i);

                    if (entry.isEmpty()) {
                        plugin.getLogger().warning("Malformed config node: " +
                                "wrong structure at '" + KEY + "." + character + "." + styleKey + "'");
                        continue;
                    }

                    Object ymlShapeKey = entry.keySet().toArray()[0];
                    Object ymlLayer = entry.values().toArray()[0];

                    if (!(ymlShapeKey instanceof String) || !(ymlLayer instanceof String)) {
                        plugin.getLogger().warning("Malformed config node: " +
                                "wrong structure at '" + KEY + "." + character + "." + styleKey + "[" + i + "]'");
                        continue;
                    }
                    String shapeKey = (String) entry.keySet().toArray()[0];
                    Boolean layer = (Boolean) entry.values().toArray()[0];

                    PatternShape shape = PatternShape.getByDisplay(shapeKey);
                    if (shape == null) {
                        plugin.getLogger().warning("Malformed config node: invalid banner shape in "
                                + "'" + KEY + "." + character + "" + styleKey + "': '" + shapeKey + "'");
                        continue;
                    }
                    if (layer == null) {
                        plugin.getLogger().warning("Malformed config node: invalid shape layer in "
                                + "'" + KEY + "." + character + "" + styleKey + "': '" + layer + "'");
                        continue;
                    }
                    shapes.put(shape, layer);
                }
                designs.put(PatternStyle.valueOf(styleKey.toUpperCase()), new PatternDesign(shapes));
            }
            patternChars.put(character, new PatternChar(designs));
        }
    }
}
