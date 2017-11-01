package me.Cynadyde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

public class BannerData {

	private static LinkedHashMap<String, String> characterStyles = new LinkedHashMap<String, String>();
	private static HashMap<Character, BannerData> characterPatterns = new HashMap<Character, BannerData>();
	
	private static final BannerData defaultCharPattern = new BannerData((char) 0, 
			new HashMap<String, LinkedHashMap<PatternShape, Boolean>>());
	
	public static void loadStyles(BannerTextPlugin plugin, FileConfiguration yaml) {
		
		if (!yaml.contains("styles")) {
			plugin.getLogger().severe("Malformed config: missing 'styles' node.");
			plugin.getLogger().severe("Remove the broken config to regenerate defaults!");
			return;
		}
		characterStyles.clear();
		
		ConfigurationSection ymlCharStyles = yaml.getConfigurationSection("styles");
		for (String styleKey : ymlCharStyles.getKeys(false)) {
			characterStyles.put(styleKey, ymlCharStyles.getString(styleKey, ""));
		}
	}
	
	public static void loadPatterns(BannerTextPlugin plugin, FileConfiguration yaml) {
		
		if (!yaml.contains("patterns")) {
			plugin.getLogger().severe("Malformed config: missing 'patterns' node.");
			plugin.getLogger().severe("Remove the broken config to regenerate defaults!");
			return;
		}
		characterPatterns.clear();
		
		ConfigurationSection ymlCharGroups = yaml.getConfigurationSection("patterns");
		for (String charKey : ymlCharGroups.getKeys(false)) {
			
			if (charKey.length() == 0) { continue; }
			
			Character character;
			
			if (charKey.equals("dot")) {
				character = '.';
			}
			
			else if (charKey.length() != 1) {
				plugin.getLogger().warning("Malformed config node-- key isn't a single character: "
						+ "'patterns." + charKey + "'.");
				continue;
			}
			else {
				character = charKey.charAt(0);
			}
			
			HashMap<String, LinkedHashMap<PatternShape, Boolean>> patterns = new HashMap<String, LinkedHashMap<PatternShape, Boolean>>();
			
			ConfigurationSection ymlCharGroup = ymlCharGroups.getConfigurationSection(charKey);
			for (String styleKey : ymlCharGroup.getKeys(false)) {
				
				LinkedHashMap<PatternShape, Boolean> shapes = new LinkedHashMap<PatternShape, Boolean>();
				
				List<Map<?, ?>> ymlCharPattern = ymlCharGroup.getMapList(styleKey);
				
				for (int i = 0; i < ymlCharPattern.size(); i++) {
					Map<?, ?> entry = ymlCharPattern.get(i);
					
					String shapeKey = (String) entry.keySet().toArray()[0];
					Boolean layer = (Boolean) entry.values().toArray()[0];
					PatternShape shape = PatternShape.getByDisplay(shapeKey);
					if (shape == null) {
						plugin.getLogger().warning("Malformed config node-- invalid banner shape in "
								+ "'patterns." + character + "." + styleKey + "': '"
								+ String.valueOf(shapeKey) + "'");
						continue;
					}
					if (layer == null) {
						plugin.getLogger().warning("Malformed config node-- invalid shape layer in "
								+ "'patterns." + character + "." + styleKey + "': '"
								+ String.valueOf(layer) + "'");
						continue;
					}
					
					plugin.debugMsg("Loaded: %s: %s", String.join(".", new String[] {"patterns", charKey, 
							styleKey, String.valueOf(i), shapeKey}), String.valueOf(layer));
					
					shapes.put(shape, layer);
				}
				patterns.put(styleKey, shapes);
			}
			characterPatterns.put(character, new BannerData(character, patterns));
		}
		
		for (Character c : characterPatterns.keySet()) {
			plugin.debugMsg("Recognized: %s\n", characterPatterns.get(c).toString());
		}
	}
	public static boolean isStyle(String style) {
		return characterStyles.containsKey(style);
	}
	
	public static boolean isChar(char character) {
		return characterPatterns.containsKey(character);
	}
	
	public static String[] getAllStyles() {
		return characterStyles.keySet().toArray(new String[characterStyles.size()]);
	}
	
	public static String getStyleDesc(String style) {
		String styleDesc = characterStyles.get(style);
		return (styleDesc == null)? "" : styleDesc;
	}
	
	public static BannerData get(char character) {
		BannerData charPattern = characterPatterns.get(character);
		return (charPattern == null)? defaultCharPattern : charPattern;
	}
	
	private char character;
	private HashMap<String, LinkedHashMap<PatternShape, Boolean>> patterns;
	
	private BannerData(char character, HashMap<String, LinkedHashMap<PatternShape, Boolean>> patterns) {
		this.character = character;
		this.patterns = patterns;
	}
	
	public char getCharacter() {
		return this.character;
	}
	
	public String[] getStyles() {
		return patterns.keySet().toArray(new String[patterns.size()]);
	}
	
	public boolean hasStyle(String style) {
		return this.patterns.containsKey(style);
	}

	@SuppressWarnings("deprecation")
	public ItemStack getBanner(String style, PatternColor txtColor, PatternColor bgColor) {
		
		if (!patterns.containsKey(style)) {
			if (patterns.containsKey("default")) {
				style = "default";
			}
			else {
				ItemStack banner = new ItemStack(Material.BANNER, 1);
				BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
				bannerMeta.setBaseColor(bgColor.getDyeColor());
				banner.setItemMeta(bannerMeta);
				return banner;
			}
		}
		
		ItemStack banner = new ItemStack(Material.BANNER, 1);
		BannerMeta bannerMeta = (BannerMeta) banner.getItemMeta();
		
		bannerMeta.setBaseColor(bgColor.getDyeColor());
		
		for (PatternShape shape : patterns.get(style).keySet()) {
			PatternColor color = (patterns.get(style).get(shape))? txtColor : bgColor;
			if (shape.equals(PatternShape.BASE)) { bannerMeta.setBaseColor(color.getDyeColor()); }
			else { bannerMeta.addPattern(new Pattern(color.getDyeColor(), shape.getType())); }
		}
		banner.setItemMeta(bannerMeta);
		return banner;
	}
	
	@Override
	public String toString() {
		
		List<String> strPatterns = new ArrayList<String>();

		for (String styleKey : this.patterns.keySet()) {
			
			List<String> strStylePatterns = new ArrayList<String>();
			LinkedHashMap<PatternShape, Boolean> stylePatterns = patterns.get(styleKey);
			
			for (PatternShape shapeKey : stylePatterns.keySet()) {
				strStylePatterns.add(String.format("{%s: %b}", shapeKey.getDisplay(), stylePatterns.get(shapeKey)));
			}
			strPatterns.add( String.format("{'%s': [%s]}", styleKey, String.join(", ", strStylePatterns)));
		}
		return String.format("BannerData('%c', [%s])", character, String.join(", ", strPatterns));
	}
}
