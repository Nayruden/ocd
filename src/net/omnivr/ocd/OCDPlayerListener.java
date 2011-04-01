package net.omnivr.ocd;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author Nayruden
 */
public class OCDPlayerListener extends PlayerListener {

    private final OChestDump plugin;

    public OCDPlayerListener(OChestDump instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfiguration().getBoolean("require-chest-open", false) || event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        OCDProtectionInfo.setKnownOwner(event.getClickedBlock().getLocation().toVector(), event.getPlayer().getName());
    }
}
