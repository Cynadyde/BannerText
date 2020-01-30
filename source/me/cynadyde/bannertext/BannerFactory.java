package me.cynadyde.bannertext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.cynadyde.bannertext.exceptions.BadFormatterException;
import net.md_5.bungee.api.ChatColor;

public class BannerFactory {
	
	public static final char escapeChar = '&';
	
	public static final Pattern formatterPattern = Pattern.compile("&[&0-9a-f][0-9a-f]");
	
	public static final String resultNameTemplate = ChatColor.DARK_GRAY + "< " + ChatColor.DARK_AQUA 
			+ "%s '&%c%c' " + ChatColor.BOLD + "'%c'" + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA 
			+ "%d of %s" + ChatColor.DARK_GRAY + " >";
	
	public static final String packageNameTemplate = ChatColor.DARK_GRAY + "< " + ChatColor.DARK_AQUA + "" 
			+ ChatColor.BOLD + "%s" + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA + "%d of %d" 
			+ ChatColor.DARK_GRAY + " >";

	/** Looks through the given text and specifies the text and bg color of each character */
	public static List<FormattedChar> parseText(String text) throws BadFormatterException {
		
		List<FormattedChar> parsed = new ArrayList<FormattedChar>();

		PatternColor fgColor = PatternColor.WHITE;
		PatternColor bgColor = PatternColor.BLACK;
		
		char[] textChars = text.toCharArray();
		int escapeLevel = 0;
		
		for (int i = 0; i < textChars.length; i++) {
			
			char character = Character.toUpperCase(textChars[i]);
			
			if (character == escapeChar) {
				if (escapeLevel == 2) {
					escapeLevel = 0;
				} 
				else if (escapeLevel == 0) {
					escapeLevel = 2;
					continue;
				}
				else {
					String fmtr = text.substring(i - 1, (i + ((i + 2 >= text.length())? 1 : 2)));
					throw new BadFormatterException(text, fmtr, i - 1);
				}
			}
			if (escapeLevel > 0) {
				if (escapeLevel == 2) {
					
					fgColor = PatternColor.getByChar(character);
					if (fgColor == null) {
						String fmtr = text.substring(i - 1, (i + ((i + 2 >= text.length())? 1 : 2)));
						throw new BadFormatterException(text, fmtr, i - 1);
					}
					escapeLevel--;
					continue;
				}
				else if (escapeLevel == 1) {
					
					bgColor = PatternColor.getByChar(character);
					if (bgColor == null) {
						String fmtr = text.substring(i - 1, (i + ((i + 2 >= text.length())? 1 : 2)));
						throw new BadFormatterException(text, fmtr, i - 1);
					}
					escapeLevel--;
					continue;
				}
			}
			parsed.add(new FormattedChar(fgColor, bgColor, character));
		}
		parsed.add(new FormattedChar(fgColor, bgColor, ' '));
		return parsed;
	}
	
	/** Converts the given text into a list of text banners of the specified style */
	public static List<ItemStack> buildBanners(BannerTextPlugin plugin, String text, String style) throws BadFormatterException {
		
		List<FormattedChar> formattedText = BannerFactory.parseText(text);
		List<ItemStack> banners = new ArrayList<ItemStack>();
		
		for (int i = 0; i < formattedText.size(); i++) {
			
			FormattedChar c = formattedText.get(i);
			
			BannerData bannerPattern = BannerData.get(c.getChar());	
			
			ItemStack banner = bannerPattern.getBanner(style, c.getFgColor(), c.getBgColor());
			
			ItemMeta bannerMeta = banner.getItemMeta();
			bannerMeta.setDisplayName(String.format(resultNameTemplate, 
					style, c.getFgColor().getChar(), c.getBgColor().getChar(), 
					Character.toUpperCase(c.getChar()), i, formattedText.size() - 1));
			
			banner.setItemMeta(bannerMeta);
			banners.add(banner);
			
			plugin.debugMsg("Built Banner: %s\n", banner.toString());
		}
		
//		plugin.debugMsg("Built Banner: %s\n", banners.stream()
//				.map(Object::toString).collect(Collectors.joining("\n")));
		
		return banners;
	}
	
	/** Packages the given banners into a list of chests */
	public static List<ItemStack> packageBanners(BannerTextPlugin plugin, List<ItemStack> banners, String name) {
		
		if (name.length() > 16) {
			name = name.substring(0, 13) + "...";
		}
		
		List<List<ItemStack>> stacks = new ArrayList<List<ItemStack>>();
		
		{
			List<ItemStack> stack = new ArrayList<ItemStack>();
			int size = 0;

			for (ItemStack banner : banners) {
				
				if (size >= 27) {
					stacks.add(stack);
					stack = new ArrayList<ItemStack>();
					size = 0;
				}
				stack.add(banner);
				size++;
			}
			stacks.add(stack);
		}

		List<ItemStack> chests = new ArrayList<ItemStack>();
		
		for (int i = 0; i < stacks.size(); i++) {
			List<ItemStack> stack = stacks.get(i);
			
			ItemStack chest = new ItemStack(Material.CHEST, 1);
			BlockStateMeta chestMeta = (BlockStateMeta) chest.getItemMeta();
			Chest chestState = (Chest) chestMeta.getBlockState();
			
			chestMeta.setDisplayName(String.format(packageNameTemplate, name, i, stacks.size() - 1));
			chestState.getBlockInventory().addItem(stack.toArray(new ItemStack[stack.size()]));
			
			chestMeta.setBlockState(chestState);
			chest.setItemMeta(chestMeta);
			
			chests.add(chest);
		}
		return chests;
	}
	
	/** Creates a banner writer item from the given text and style */
	public static ItemStack buildBannerWriter(BannerTextPlugin plugin, String text, String style) throws BadFormatterException {
		BannerWriter writer = BannerWriter.createNew(plugin, text, style);
		return (writer == null)? null : writer.getSkin();
	}
}
