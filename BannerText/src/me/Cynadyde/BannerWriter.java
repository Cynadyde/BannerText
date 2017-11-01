package me.Cynadyde;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;

import me.Cynadyde.exceptions.BadFormatterException;

public class BannerWriter {
	
	public static final Pattern writerNamePattern = Pattern.compile("^\\< BannerText Writer \\| \\d*? \\>$");
	
	public static final String writerNameTemplate = ChatColor.DARK_GRAY + "< " + ChatColor.DARK_AQUA + "" + ChatColor.BOLD 
			+ "BannerText Writer" + ChatColor.DARK_GRAY + " | " + ChatColor.DARK_AQUA + "%d of %d" + ChatColor.DARK_GRAY + " >";
	
	public static final String loreSeparator = ChatColor.DARK_GRAY + "--------------------------------";
	
	public static final String dataColor = "";  // ChatColor.BLACK + "" + ChatColor.MAGIC + "" + ChatColor.BOLD;
	
	public static final String dataPrefix = "BannerText-Writer";
	public static final String dataDelimeter = ";";
	
	/** Parse the given lore for data tokens */
	public static String[] getLoreData(List<String> lore) {
		
		if ((lore == null) || (lore.size() < 2)) {
			return new String[0];
		}
		int start = lore.lastIndexOf(loreSeparator) + 1;
		if (start >= lore.size()) {
			return new String[0];
		}
		return ChatColor.stripColor(String.join(
				";", lore.subList(start, lore.size()))).split(";");
	}
	
	/** Merge the given data tokens with the given lore */
	public static List<String> setLoreData(List<String> lore, String[] dataTokens) {
		
		if (lore == null) {
			lore = new ArrayList<String>();
		}
		if (lore.contains(loreSeparator)) {
			lore = new ArrayList<String>(lore.subList(0, lore.lastIndexOf(loreSeparator)));
		}
		
		ArrayList<String> dataLines;
		
		{
			ArrayList<ArrayList<String>> wrappedLines = new ArrayList<ArrayList<String>>();
			ArrayList<String> currentLine = new ArrayList<String>();
			int maxSize = 3;
			int size = 0;
			for (String dc : dataTokens) {
				if (size + 1 > maxSize) {
					if (maxSize == 3) { maxSize = 10; }
					wrappedLines.add(currentLine);
					currentLine = new ArrayList<String>();
					size = 0;
				}
				currentLine.add(dc);
				size += 1;
			}
			wrappedLines.add(currentLine);
			
			dataLines = new ArrayList<String>();
			
			for (int i = 0; i < wrappedLines.size(); i++) {
				
				ArrayList<String> wrappedLine = wrappedLines.get(i);
				String dataLine = String.join(";", wrappedLine);
				dataLines.add(dataColor + dataLine);
			}
		}
		
		lore.add(loreSeparator);
		lore.addAll(dataLines);
		
		return lore;
	}
	
	/** Create a brand new writer object */
	public static BannerWriter createNew(BannerTextPlugin plugin, String text, String style) throws BadFormatterException {
		
		int pos = 0;
		
		String[] dataTokens;
		
		{
			List<FormattedChar> fmtdChars = BannerFactory.parseText(text);
			
			dataTokens = new String[fmtdChars.size() + 3];
			dataTokens[0] = dataPrefix;
			dataTokens[1] = String.valueOf(pos);
			dataTokens[2] = style;
			
			for (int i = 3; i < dataTokens.length; i++) {
				FormattedChar fc = fmtdChars.get(i - 3);
				String dc = "";
				dc += fc.getFgColor().getChar();
				dc += fc.getBgColor().getChar();
				dc += fc.getChar();
				dataTokens[i] = dc;
			}
		}
		
		String[] dataChars = Arrays.copyOfRange(dataTokens, 3, dataTokens.length);
		
		ItemStack banner = new ItemStack(Material.BANNER, 1);
		ItemMeta itemMeta = banner.getItemMeta();
		
		itemMeta.setLore(setLoreData(itemMeta.getLore(), dataTokens));
		banner.setItemMeta(itemMeta);
		
		BannerWriter bannerWriter = new BannerWriter(banner, pos, style, dataChars);
		bannerWriter.updateSkin();
		
		return bannerWriter;
	}
	
	/** Create a writer object using the given banner item */
	public static BannerWriter createFrom(BannerTextPlugin plugin, ItemStack writer) {
		if ((writer == null) || (writer.getType() != Material.BANNER)) {
			plugin.debugMsg("Cannot create writer from: %s", writer);
			
			return null;
		}
		String[] dataTokens = getLoreData(writer.getItemMeta().getLore());
		
		plugin.debugMsg("Lore data: %s", String.join(";", dataTokens));
		
		if (!Utilities.arrayGet(dataTokens, 0, "").equals(dataPrefix)) {
			plugin.debugMsg("Item is not a bannertext writer");

			return null;
		}
		
		int writerPos = Utilities.intValueOf(Utilities.arrayGet(dataTokens, 1, "-1"), -1);
		if (writerPos == -1) {
			plugin.debugMsg("Writer missing valid pos token");

			return null;
		}
		
		String writerStyle = Utilities.arrayGet(dataTokens, 2, "");
		if (writerStyle.isEmpty()) {
			plugin.debugMsg("Writer missing style token");

			return null;
		}
		
		String[] writerChars = Utilities.arraySlice(dataTokens, 3, null);
		if (writerChars.length < 1) {
			plugin.debugMsg("Writer missing char tokens");

			return null;
		}
		return new BannerWriter(writer, writerPos, writerStyle, writerChars);
	}
	
	private ItemStack skin;
	private int pos;
	private String style;
	private String[] chars;
	
	private BannerWriter(ItemStack writerItem, int writerPos, String writerStyle, String[] writerChars) {
		this.skin = writerItem;
		this.pos = writerPos;
		this.style = writerStyle;
		this.chars = writerChars;
	}
	
	public ItemStack getSkin() {
		return this.skin;
	}
	
	public int getPos() {
		return this.pos;
	}
	
	public int getMaxPos() {
		return this.chars.length - 1;
	}
	
	public void setPos(int newPos) {

		if (newPos < 0) { newPos += this.chars.length; }
		if (newPos > this.getMaxPos()) { newPos -= this.chars.length; }
		
		if (!((0 <= newPos) && (newPos <= this.getMaxPos()))) { return; }
		
		this.pos = newPos;
		this.updateSkin();
	}
	
	public String getStyle() {
		return this.style;
	}
	
	public FormattedChar getChar(int pos) {
		try { 
			String data = this.chars[pos]; 
			return new FormattedChar(PatternColor.getByChar(data.charAt(0)), 
					PatternColor.getByChar(data.charAt(1)), data.charAt(2));
		}
		catch (IndexOutOfBoundsException e) {
			return FormattedChar.defaultChar;
		}
	}
	
	public ItemStack getBanner(int pos) {
		FormattedChar c = this.getChar(pos);
		return BannerData.get(c.getChar()).getBanner(this.style, c.getFgColor(), c.getBgColor());
	}
	
	@SuppressWarnings("deprecation")
	public void updateSkin() {
		BannerMeta bannerMeta = (BannerMeta) this.skin.getItemMeta();
		
		bannerMeta.setDisplayName(String.format(writerNameTemplate, this.getPos(), this.getMaxPos()));
		
		String[] dataTokens = new String[this.chars.length + 3];
		dataTokens[0] = dataPrefix;
		dataTokens[1] = String.valueOf(this.pos);
		dataTokens[2] = this.style;
		
		for (int i = 0; i < this.chars.length; i++) {
			dataTokens[i + 3] = this.chars[i];
		}
		
		bannerMeta.setLore(setLoreData(bannerMeta.getLore(), dataTokens));
		
		ItemStack modelBanner = this.getBanner(this.pos);
		BannerMeta modelMeta = (BannerMeta) modelBanner.getItemMeta();
		
		bannerMeta.setBaseColor(modelMeta.getBaseColor());
		bannerMeta.setPatterns(modelMeta.getPatterns());
		
		this.skin.setItemMeta(bannerMeta);
	}
}
