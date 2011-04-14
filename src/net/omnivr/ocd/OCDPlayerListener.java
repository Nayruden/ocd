package net.omnivr.ocd;

import net.omnivr.olib.Util;
import org.bukkit.block.Block;
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

        Util.DoubleChest double_chest = Util.getDoubleChestIfExists(event.getClickedBlock());
        Block block = double_chest != null ? double_chest.primary_chest : event.getClickedBlock();
        OCDProtectionInfo.setKnownOwner(block.getLocation(), event.getPlayer().getName());
    }
}
