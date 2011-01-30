package net.omnivr.ocd;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockRightClickEvent;

/**
 *
 * @author Nayruden
 */
public class OCDBlockListener extends BlockListener {

    private final OChestDump plugin;

    public OCDBlockListener(OChestDump instance) {
        plugin = instance;
    }

    @Override
    public void onBlockInteract(BlockInteractEvent event) {
        if (!plugin.getConfiguration().getBoolean("require-chest-open", false) || event.isCancelled() || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        OCDProtectionInfo.setKnownOwner(event.getBlock().getLocation().toVector(), player.getName());
    }
}
