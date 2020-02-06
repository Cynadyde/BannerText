package me.cynadyde.bannertext;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The main class of the BannerText Spigot MC Plugin.
 */
public class BannerTextPlugin extends JavaPlugin implements Listener {

    private static final Supplier<IllegalStateException> NOT_ENABLED = () ->
            new IllegalStateException("plugin has not been fully enabled yet");

    private PluginCommand rootCmd;

    /**
     * Reloads the plugin's configuration, registers its
     * event listeners, and sets up its root command.
     */
    @Override
    public void onEnable() {

        reloadConfig();

        getServer().getPluginManager().registerEvents(this, this);

        rootCmd = Objects.requireNonNull(getCommand(Cmd.ROOT.getLabel()));
        rootCmd.setExecutor(this);
        rootCmd.setTabCompleter(this);
    }

    /**
     * Executes the plugin's root command and sub commands.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (command == getRootCmd()) {
            if (args.length > 0) {
                boolean hasGetArg = args[0].equalsIgnoreCase(Cmd.GET.getLabel());
                boolean hasWriteArg = args[0].equalsIgnoreCase(Cmd.WRITE.getLabel());

                if (hasGetArg || hasWriteArg) {

                    if ((hasGetArg && !Cmd.GET.checkPerms(sender))
                            || (hasWriteArg && !Cmd.WRITE.checkPerms(sender))) {
                        Msg.NO_PERMS.to(sender);
                        return false;
                    }
                    if (!(sender instanceof Player)) {
                        Msg.NOT_PLAYER.to(sender);
                        return false;
                    }
                    if (args.length < 2) {
                        Msg.BAD_USAGE.to(sender, (hasGetArg ? Cmd.GET : Cmd.WRITE).getUsage());
                        return false;
                    }
                    Player player = (Player) sender;
                    String textArg = String.join(" ", Utils.arraySlice(args, 1, null));

                    if (hasGetArg) {
                        doGiveTextBanners(player, textArg);
                        Msg.GOT_BANNERS.to(player);
                        return true;
                    }
                    else {
                        doGiveBannerWriter(player, textArg);
                        Msg.GOT_WRITER.to(player);
                        return true;
                    }
                }
                else if (args[0].equalsIgnoreCase(Cmd.RELOAD.getLabel())) {

                    if (!Cmd.RELOAD.checkPerms(sender)) {
                        Msg.NO_PERMS.to(sender);
                        return false;
                    }
                    reloadConfig();
                    Msg.DID_CONFIG.toAll(Cmd.RELOAD.getPerm(), sender.getName());
                    return true;
                }
            }
            sender.sendMessage(Msg.getInfo());
            return true;
        }
        return false;
    }

    /**
     * Tab completes the plugin's sub commands command.
     */
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
            @NotNull Command command, @NotNull String alias, @NotNull String[] args) {

        if (command == rootCmd) {
            List<String> options = new ArrayList<>();

            if (args.length == 1) {
                for (Cmd cmd : Cmd.ROOT.getChildren()) {
                    if (cmd.getLabel().startsWith(args[0].toLowerCase())) {
                        if (cmd.checkPerms(sender)) {
                            options.add(cmd.getLabel());
                        }
                    }
                }
            }
            return options;
        }
        return null;
    }

    /**
     * Handles a player's usage of the banner writer.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (event.getItem() == null) {
            return;
        }
        BannerWriter writer = BannerWriter.from(event.getItem());
        if (writer == null) {
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_AIR
                || event.getAction() == Action.LEFT_CLICK_BLOCK) {

            if (event.getPlayer().isSneaking()) {
                writer.augPos(-1);
            }
        }
        else if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (event.getPlayer().getGameMode() != GameMode.CREATIVE
                    && event.useInteractedBlock() != Event.Result.DENY
                    && event.useItemInHand() != Event.Result.DENY) {

                event.getItem().setAmount(2);  // keeps item stack size at 1
            }

            if (event.getPlayer().isSneaking()) {
                writer.augPos(1);
//                getServer().getScheduler().runTaskLater(this,  // why is this is executed next tick..?
//                        () -> writer.augPos(1), 1L);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * The plugin's commands, messages, and banner pattern chars are refreshed.
     */
    @Override
    public void reloadConfig() {

        long timeStart = System.currentTimeMillis();

        saveDefaultConfig();
        super.reloadConfig();

        Cmd.reload(this);
        Msg.reload(this);

        BannerFactory.reloadPatternChars(this);

        this.getLogger().info(String.format("reloaded the config in %dms",
                System.currentTimeMillis() - timeStart));
    }

    /**
     * Gets the plugin's root command.
     */
    public @NotNull PluginCommand getRootCmd() {
        if (rootCmd == null) {
            throw NOT_ENABLED.get();
        }
        return rootCmd;
    }

    /**
     * Attempts to give the specified player banners for the requested text.
     */
    public void doGiveTextBanners(Player player, String text) {

        long timeStart = System.currentTimeMillis();

        if (!getConfig().getBoolean("case-sensitive")) {
            text = text.toUpperCase();
        }

        String name = BannerFactory.stripFormat(text);
        List<ItemStack> banners = BannerFactory.buildBanners(text);
        List<ItemStack> chests = BannerFactory.packageBanners(banners, name);

        Utils.giveItems(player, chests.toArray(new ItemStack[0]));

        debugMsg("took %dms to run: doGiveTextBanners(%s, %s);",
                System.currentTimeMillis() - timeStart, player, text);
    }

    /**
     * Attempts to give the specified player a banner writer for the requested text.
     */
    public void doGiveBannerWriter(Player player, String text) {

        long timeStart = System.currentTimeMillis();

        if (!getConfig().getBoolean("case-sensitive")) {
            text = text.toUpperCase();
        }
        ItemStack writer = BannerFactory.buildBannerWriter(text);
        Utils.giveItems(player, writer);

        this.debugMsg("took %dms to run: giveTextBannerWriter(%s, %s);",
                System.currentTimeMillis() - timeStart, player, text);
    }

    /**
     * Prints a formatted debugging message if in debugging mode.
     */
    void debugMsg(String message, Object... objects) {
        if (getConfig().getBoolean("debug-mode", false)) {
            this.getLogger().info("\u001B[36m[Debug] " + String.format(message, objects) + "\u001B[0m");
        }
    }

    /**
     * The plugin's permissions.
     */
    public enum Perm {

        GET("bannertext.get"),
        WRITE("bannertext.write"),
        CONFIG("bannertext.config");

        private final String node;

        Perm(String node) {
            this.node = node;
        }

        public @NotNull String getNode() {
            return node;
        }
    }

    /**
     * The plugin's commands.
     */
    public enum Cmd {

        ROOT("bannertext", null),
        GET("get", Perm.GET),
        WRITE("write", Perm.WRITE),
        RELOAD("reloadconfig", Perm.CONFIG);

        static {
            ROOT.children = Collections.unmodifiableList(
                    Arrays.asList(Cmd.GET, Cmd.WRITE, Cmd.RELOAD));
        }

        private final String label;
        private final Perm permission;
        private List<Cmd> children;

        private String usage;
        private String description;

        Cmd(String label, Perm permission) {
            this.label = label;
            this.permission = permission;
            this.children = Collections.unmodifiableList(new ArrayList<>());
        }

        public @NotNull String getLabel() {
            return label;
        }

        public @NotNull Perm getPerm() {
            return permission;
        }

        public @NotNull List<Cmd> getChildren() {
            return children;
        }

        public @NotNull String getUsage() {
            if (usage == null) {
                throw NOT_ENABLED.get();
            }
            return usage;
        }

        public @NotNull String getDescr() {
            if (description == null) {
                throw NOT_ENABLED.get();
            }
            return description;
        }

        public boolean checkPerms(CommandSender sender) {
            return permission == null || sender.hasPermission(permission.getNode());
        }

        public static void reload(BannerTextPlugin plugin) {

            ConfigurationSection commands = Objects.requireNonNull(
                    plugin.getConfig().getConfigurationSection("chat.commands"));

            for (Cmd cmd : values()) {
                ConfigurationSection command = Objects.requireNonNull(
                        commands.getConfigurationSection(cmd.name()));

                cmd.usage = command.getString("usage");
                cmd.description = command.getString("description");
            }
        }
    }

    /**
     * The plugin's messages.
     */
    public enum Msg {

        NO_PERMS,
        NOT_PLAYER,
        BAD_CMD,
        BAD_USAGE,
        BAD_ARG,
        ERR_WRITER,
        ERR_BANNERS,
        GOT_WRITER,
        GOT_BANNERS,
        DID_CONFIG;

        private static String tag;
        private static String info;

        private String template;

        public @NotNull String getTemplate() {
            if (template == null) {
                throw NOT_ENABLED.get();
            }
            return template;
        }

        public void to(CommandSender sender, Object... objects) {
            Msg.chat(sender, getTemplate(), objects);
        }

        public void toAll(Perm permission, Object... objects) {
            Msg.broadcast(permission, getTemplate(), objects);
        }

        public static void chat(CommandSender sender, String message, Object... objects) {
            sender.sendMessage(getTag() + Utils.chatFormat(message, objects));
        }

        public static void broadcast(Perm permission, String message, Object... objects) {
            Bukkit.broadcast(getTag() + Utils.chatFormat(message, objects), permission.getNode());
        }

        public static void reload(BannerTextPlugin plugin) {

            ConfigurationSection chat = Objects.requireNonNull(plugin.getConfig().getConfigurationSection("chat"));
            ConfigurationSection chatMsgs = Objects.requireNonNull(chat.getConfigurationSection("messages"));

            tag = Utils.chatFormat(chat.getString("tag"));

            String helpPage = chat.getString("help-page");
            String cmdFormat = chat.getString("cmd-format");

            info = Utils.chatFormat(helpPage,
                    plugin.getDescription().getFullName(),
                    plugin.getDescription().getDescription(),
                    plugin.getDescription().getWebsite(),
                    Arrays.stream(Cmd.values())
                            .map((cmd -> Utils.chatFormat(cmdFormat, cmd.getUsage(), cmd.getDescr())))
                            .collect(Collectors.joining("\n")));

            for (Msg msg : values()) {
                msg.template = Objects.requireNonNull(chatMsgs.getString(msg.name()));
            }
        }

        public static @NotNull String getTag() {
            if (tag == null) {
                throw NOT_ENABLED.get();
            }
            return tag;
        }

        public static @NotNull String getInfo() {
            if (info == null) {
                throw NOT_ENABLED.get();
            }
            return info;
        }
    }
}
