package me.Cynadyde.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Cynadyde.BannerTextPlugin;
import me.Cynadyde.BannerWriter;

public class BannerWriterListener implements Listener {

	private BannerTextPlugin plugin;
	
	public BannerWriterListener(BannerTextPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {

		plugin.debugMsg("Caught BlockPlaceEvent... \n[CLICKED]: %s\n[ITEM]: %s\n[PERMS]: %b\n[CANCELLED]: %b\n\n",
				e.getBlock(), e.getItemInHand().toString(), e.canBuild(), e.isCancelled());
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent e) {
		
		plugin.debugMsg("Caught PlayerInteractEvent... \n[CLICKED]: %s\n[ITEM]: %s\n[SNEAKING]: %b\n[ACTION]: %s\n\n",
				e.getClickedBlock(), e.getItem(), e.getPlayer().isSneaking(), e.getAction());
		
		BannerWriter writer = BannerWriter.createFrom(plugin, e.getItem());
		
		if (writer == null) { return; }

		if (e.getAction() == Action.LEFT_CLICK_AIR 
				|| e.getAction() == Action.LEFT_CLICK_BLOCK) {
		
			if (e.getPlayer().isSneaking()) {
				writer.setPos(writer.getPos() - 1);
			}
		}
		else if (e.getAction() == Action.RIGHT_CLICK_AIR 
				|| e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			if (!e.isCancelled() && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
					e.getItem().setAmount(2);
			}
			
			if (e.getPlayer().isSneaking()) {
				
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask((Plugin) plugin, new BukkitRunnable() {
					
					@Override
					public void run() {
						writer.setPos(writer.getPos() + 1);
					}
					
				}, 1L);
			}
		}
	}
}
