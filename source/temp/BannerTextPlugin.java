package temp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import temp.exceptions.BadFormatterException;
import temp.listeners.BannerWriterListener;

public class BannerTextPlugin extends JavaPlugin {
	
	public static final String chatTag = ChatColor.DARK_GRAY + "[" + ChatColor.DARK_AQUA
			+ "BTxt" + ChatColor.DARK_GRAY + "]" + ChatColor.RESET + " ";
	
	public static final String permissionGet = "bannertext.get";
	public static final String permissionWrite = "bannertext.write";
	public static final String permissionReload = "bannertext.reload";
	
	public static final String command = "bannertext";
	public static final String commandGet = "get";
	public static final String commandWrite = "write";
	public static final String commandColors = "colors";
	public static final String commandStyles = "styles";
	public static final String commandReload = "reload";
	
	public static boolean debugMode = false;
	
	private BannerWriterListener writerListener;
	
	@Override
	public void onEnable() {

		this.getCommand(command).setExecutor(this);
		this.getCommand("bt").setExecutor(this);
		
		this.loadConfigValues();
		
		this.writerListener = new BannerWriterListener(this);
		this.getServer().getPluginManager().registerEvents(writerListener, this);
		
		this.getLogger().info("Successfully enabled " + this.getName() + "!");
	}

	@Override
	public void onDisable() {
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		// Command: "/bannertext"
		if (args.length == 0) {
			return this.showPluginHelp(sender, 0);
		}
		
		// Command: "/bannertext get <text...> [-style: <style>]"
		// command: "/bannertext write <text...> [-style: <style>]"
		boolean hasGetArg = args[0].equals(commandGet);
		boolean hasWriteArg = args[0].equals(commandWrite);
		
		if (hasGetArg || hasWriteArg) {
			
			if ((hasGetArg && !(sender.hasPermission(permissionGet))) 
					|| (hasWriteArg && !(sender.hasPermission(permissionWrite)))) {
				sender.sendMessage(chatTag + ChatColor.RED + "Insufficient permissions...");
				return false;
			}
			if (!(sender instanceof Player)) {
				sender.sendMessage(chatTag + ChatColor.RED + "You must be in-game to use this command!");
				return false;
			}
			if (args.length < 2) {
				sender.sendMessage(chatTag + ChatColor.RED + "Invalid usage: missing argument <text...>");
				return false;
			}
			Player player = (Player) sender;
			int keyIndex = Utilities.arrayIndexOf(args, "-style:");
			String textArg = String.join(" ", Utilities.arraySlice(args, 1, (keyIndex != -1)? keyIndex : null));
			String styleArg = (keyIndex != -1)? String.join(" ", Utilities.arraySlice(args, keyIndex + 1, null)) : "default";
			if (!BannerData.isStyle(styleArg)) {
				player.sendMessage(chatTag + ChatColor.RED + String.format("Invalid style: '%s'... ", styleArg) 
				+ "Use \"/bannertext styles [<page>]\" to see available styles.");
				return false;
			}
			if (hasGetArg) {
				return this.giveTextBanners(player, textArg, styleArg);
			}
			else {
				return this.giveTextBannerWriter(player, textArg, styleArg);
			}
		}
		
		// Command: "/bannertext colors"
		else if (args[0].equals(commandColors)) {
			if (!(sender.hasPermission(permissionGet))) {
				sender.sendMessage(chatTag + ChatColor.RED + "Insufficient permissions...");
				return false;
			}
			return this.showColorFormatHelp(sender);
		}
		
		// Command: "/bannertext styles [<page>]"
		else if (args[0].equals(commandStyles)) {
			if (!(sender.hasPermission(permissionGet))) {
				sender.sendMessage(chatTag + ChatColor.RED + "Insufficient permissions...");
				return false;
			}
			int page = Utilities.intValueOf(Utilities.arrayGet(args, 1, "0"), 0);
			return this.showStyleList(sender, page);
		}
		
		// Command: "/bannertext reload"
		else if (args[0].equals(commandReload)) {
			if (!sender.hasPermission(permissionReload)) {
				sender.sendMessage(chatTag + ChatColor.RED + "Insufficient permissions...");
			}
			return this.reloadConfig(sender);
		}
		int page = Utilities.intValueOf(Utilities.arrayGet(args, 1, "1"), 1);
		this.showPluginHelp(sender, page);
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		
		List<String> options = new ArrayList<String>();

		if (args.length == 0) {
			
			boolean canGet = sender.hasPermission(permissionGet);
			boolean canWrite = sender.hasPermission(permissionWrite);
			
			if (canGet) { options.add(commandGet); }
			if (canWrite) { options.add(commandWrite); }
			
			if (canGet || canWrite) {
				options.add(commandStyles);
				options.add(commandColors);
			}
		}
		return options;
	}
	
	/** Loads the config file's values into the plugin */
	public void loadConfigValues() {
		
		long timeStart = System.currentTimeMillis();
		
		if (!(new File(getDataFolder(), "config.yml")).exists()) { saveDefaultConfig(); }
		
		this.reloadConfig();
		
		debugMode = getConfig().getBoolean("debug-mode", false);
		
		BannerData.loadStyles(this, getConfig());
		BannerData.loadPatterns(this, getConfig());
		
		this.getLogger().info(String.format("Successfully loaded config! Took... %dms", 
				System.currentTimeMillis() - timeStart));
	}
	
	/** Prints a formtted debugging message if in debugging mode */
	public void debugMsg(String fMsg, Object...objs) {
		if (debugMode) { this.getLogger().info("\u001B[36m[Debug] " + String.format(fMsg, objs) + "\u001B[0m"); }
	}
	
	/** Get a reference to the bannertext-writer listener */
	public BannerWriterListener getWriterListener() {
		return this.writerListener;
	}
	
	/** Shows the sender general information about the plugin and its commands */
	public boolean showPluginHelp(CommandSender sender, int page) {
		if (page == 0) {
			sender.sendMessage(new String[] {
					chatTag + ChatColor.GOLD + "--{ " + ChatColor.YELLOW + "Plugin help" + ChatColor.GOLD + " | page " + 
							ChatColor.RED + "0" + ChatColor.GOLD + " / " + ChatColor.RED + "1" + ChatColor.GOLD + " }--",
					chatTag + ChatColor.AQUA + "Name: " + ChatColor.GRAY + getDescription().getName(),
					chatTag + ChatColor.AQUA + "Version: " + ChatColor.GRAY + getDescription().getVersion(),
					chatTag + ChatColor.AQUA + "Desc: " + ChatColor.GRAY + getDescription().getDescription(),
					chatTag + ChatColor.AQUA + "Authors: " + ChatColor.GRAY + String.join(", ", getDescription().getAuthors()),
					chatTag + ChatColor.AQUA + "Website: " + ChatColor.WHITE + getDescription().getWebsite(),
					chatTag + ChatColor.YELLOW + "--------------------------------",
					chatTag + ChatColor.GRAY + "Use " + ChatColor.GREEN + "/bt help 1" 
							+ ChatColor.GRAY + " to view command help on the next page.",
					""
			});
		}
		else {
			sender.sendMessage(new String[] {
					chatTag + ChatColor.GOLD + "--{ " + ChatColor.YELLOW + "Plugin help" + ChatColor.GOLD + " | page " + 
							ChatColor.RED + "1" + ChatColor.GOLD + " / " + ChatColor.RED + "1" + ChatColor.GOLD + " }--",
					chatTag + ChatColor.GREEN + "/bannertext get <text...> [-style: <style>]",
					chatTag + ChatColor.GRAY + "  Creates banners for the given text and style.",
					chatTag + ChatColor.GRAY + "  Use the formatter &[0-f][0-f] to change the text",
					chatTag + ChatColor.GRAY + "  and background color. && becomes a literal '&'.",
					chatTag + ChatColor.GREEN + "/bannertext write <text...> [-style: <style>]",
					chatTag + ChatColor.GRAY + "  Creates a writer for the given text and style.",
					chatTag + ChatColor.GRAY + "  Use the formatter &[0-f][0-f] to change the text",
					chatTag + ChatColor.GRAY + "  and background color. && becomes a literal '&'.",
					chatTag + ChatColor.GREEN + "/bannertext colors",
					chatTag + ChatColor.GRAY + "  Display each color formatter code value",
					chatTag + ChatColor.GREEN + "/bannertext styles [<page>]",
					chatTag + ChatColor.GRAY + "  Get a list of all available styles.",
					chatTag + ChatColor.GREEN + "/bannertext reload",
					chatTag + ChatColor.GRAY + "  Reloads the plugin's configuration file.",
			});
		}
		return true;
	}
	
	/** Shows the sender information on using the plugin's color codes */
	public boolean showColorFormatHelp(CommandSender sender) {
		sender.sendMessage(new String[] {
				chatTag + ChatColor.GOLD + "--{ " + ChatColor.YELLOW + "Color Formatting Codes" + ChatColor.GOLD + " }--",
				chatTag + ChatColor.GREEN + "0" + ChatColor.GRAY + ": Black______" + ChatColor.GREEN + "8" + ChatColor.GRAY + ": Dark Gray",
				chatTag + ChatColor.GREEN + "1" + ChatColor.GRAY + ": Brown______" + ChatColor.GREEN + "9" + ChatColor.GRAY + ": Blue",
				chatTag + ChatColor.GREEN + "2" + ChatColor.GRAY + ": Green______" + ChatColor.GREEN + "a" + ChatColor.GRAY + ": Lime",
				chatTag + ChatColor.GREEN + "3" + ChatColor.GRAY + ": Cyan_______" + ChatColor.GREEN + "b" + ChatColor.GRAY + ": Light Blue",
				chatTag + ChatColor.GREEN + "4" + ChatColor.GRAY + ": Red________" + ChatColor.GREEN + "c" + ChatColor.GRAY + ": Magenta",
				chatTag + ChatColor.GREEN + "5" + ChatColor.GRAY + ": Purple_____" + ChatColor.GREEN + "d" + ChatColor.GRAY + ": Pink",
				chatTag + ChatColor.GREEN + "6" + ChatColor.GRAY + ": Orange_____" + ChatColor.GREEN + "e" + ChatColor.GRAY + ": Yellow",
				chatTag + ChatColor.GREEN + "7" + ChatColor.GRAY + ": Gray_______" + ChatColor.GREEN + "f" + ChatColor.GRAY + ": White",
				""
		});
		return true;
	}
	
	/** Shows the sender a list of styles with their description */
	public boolean showStyleList(CommandSender sender, int page) {
		
		String[] message;
		
		{
			ArrayList<String> lines = new ArrayList<String>();
			String[] styles = BannerData.getAllStyles();
			
			int itemsPerPage = 8;
			int maxPage = (int) (Math.ceil((double) styles.length / (double) itemsPerPage)) - 1;
			page = Math.max(0, Math.min(page, maxPage));
			
			lines.add(chatTag + ChatColor.GOLD + "--{" + ChatColor.YELLOW 
					+ " Character Styles " + ChatColor.GOLD 
					+ "| page " + ChatColor.RED + String.valueOf(page) + ChatColor.GOLD + " / " 
					+ ChatColor.RED + String.valueOf(maxPage) + ChatColor.GOLD + " }--");
			
			int startI = Math.max(0, Math.min(page * itemsPerPage, styles.length - 1));
			int endI = Math.max(1, Math.min(startI + itemsPerPage, styles.length));
			if (styles.length == 0) { 
				lines.add(chatTag + ChatColor.GRAY + "" + ChatColor.ITALIC + "No results to display..."); 
				message = lines.toArray(new String[lines.size()]);
			}
			else {
				for (int i = startI; i < endI; i++) {
					lines.add(chatTag + ChatColor.GRAY + String.valueOf(i + 1) + " " + ChatColor.GREEN + styles[i]
							+ ChatColor.GRAY + " - " + BannerData.getStyleDesc(styles[i]));
				}
				lines.add("");
				message = lines.toArray(new String[lines.size()]);
			}
		}
		sender.sendMessage(message);
		return true;
	}
	
	/** Reads the config values back into the plugin */
	public boolean reloadConfig(CommandSender sender) {
		Bukkit.broadcast(chatTag + ChatColor.GOLD + sender.getName() + " has reloaded the config", permissionReload);
		this.loadConfigValues();
		return true;
	}
	
	/** Attempts to give the player banners or a ticket for the requested text */
	public boolean giveTextBanners(Player player, String text, String style) {

		long timeStart = System.currentTimeMillis();
		
		List<ItemStack> banners;
		try { 
			banners = BannerFactory.buildBanners(this, text, style); 
		} 
		catch (BadFormatterException e) {
			player.sendMessage(chatTag + ChatColor.RED + e.getMessage());
			return false;
		}
		String name = BannerFactory.formatterPattern.matcher(text).replaceAll("");
		List<ItemStack> chests = BannerFactory.packageBanners(this, banners, name);
		
		HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(chests.toArray(new ItemStack[chests.size()]));
		
		if (!leftovers.isEmpty()) {
			for (ItemStack leftover : leftovers.values()) {
				player.getWorld().dropItem(player.getLocation(), leftover);
			}
		}
		
		this.debugMsg("took %dms to run: giveTextBanners(%s, %s, %s);", 
				System.currentTimeMillis() - timeStart, player, text, style);
		
		return true;
	}
	
	/** Attempts to give the player a banner writer for the requested text */
	public boolean giveTextBannerWriter(Player player, String text, String style) {
		
		long timeStart = System.currentTimeMillis();
		
		ItemStack writer;
		try {
			writer = BannerFactory.buildBannerWriter(this, text, style);
		}
		catch (BadFormatterException e) {
			player.sendMessage(chatTag + ChatColor.RED + e.getMessage());
			return false;
		}
		if (writer == null) {
			player.sendMessage(chatTag + ChatColor.RED + "Sorry; none of those characters were recognized! "
					+ "Try adding them to the plugin's config?");
			return false;
		}
		HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(writer);
		if (!leftovers.isEmpty()) {
			for (ItemStack leftover : leftovers.values()) {
				player.getWorld().dropItem(player.getLocation(), leftover);
			}
		}
		
		this.debugMsg("took %dms to run: giveTextBannerWriter(%s, %s, %s);", 
				System.currentTimeMillis() - timeStart, player, text, style);
		
		return true;
	}
}
