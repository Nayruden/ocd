package net.omnivr.ocd;

import net.omnivr.olib.Trace;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

/**
 * Handle events for all Player related events
 * @author Nayruden
 */
public class OCDPlayerListener extends PlayerListener {

    private final OChestDump plugin;

    public OCDPlayerListener(OChestDump instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerCommand(PlayerChatEvent event) {
        String[] pieces = event.getMessage().split(" ");
        Player player = event.getPlayer();

        if (pieces[0].equalsIgnoreCase("/ocd")) {
            if (pieces.length != 2 || !(pieces[1].equalsIgnoreCase("stash") || pieces[1].equalsIgnoreCase("loot") || pieces[1].equalsIgnoreCase("swap"))) {
                player.sendMessage(ChatColor.RED + "/ocd stash|loot|swap");
                player.sendMessage(ChatColor.RED + "stash - Put all items in inventory in the chest you're looking at");
                player.sendMessage(ChatColor.RED + "loot - Put all items in the chest into your inventory");
                player.sendMessage(ChatColor.RED + "swap - Exhange items between your inventory and chest");
                return;
            }

            Block block = Trace.Simple(player, 4.0); // 4m max
            if (block == null || block.getType() != Material.CHEST) {
                player.sendMessage(ChatColor.RED + "You need to look at a chest to use this command");
                return;
            }

            ContainerBlock chest = (ContainerBlock) block.getState();
            ItemStack[] chest_contents = chest.getInventory().getContents();
            ItemStack[] player_contents = player.getInventory().getContents();

            if (pieces[1].equalsIgnoreCase("stash")) {
                tryFill(player_contents, chest_contents);
            } else if (pieces[1].equalsIgnoreCase("loot")) {
                tryFill(chest_contents, player_contents);
            } else {
                final int chest_size = chest_contents.length;
                final int player_size = player_contents.length;
                if (chest_size < player_size) {
                    ItemStack[] new_chest_contents = new ItemStack[chest_size];
                    tryFill(player_contents, new_chest_contents);
                    tryFill(chest_contents, player_contents);
                    chest_contents = new_chest_contents;
                } else {
                    ItemStack[] new_player_contents = new ItemStack[player_size];
                    tryFill(chest_contents, new_player_contents);
                    tryFill(player_contents, chest_contents);
                    player_contents = new_player_contents;
                }
            }
            chest.getInventory().setContents(chest_contents);
            player.getInventory().setContents(player_contents);

            event.setCancelled(true);

        }
    }

    private void tryFill(ItemStack[] inventory_from, ItemStack[] inventory_to) {
        int from_size = inventory_from.length;
        int to_size = inventory_to.length;
        int from_slot = 0;
        for (int to_slot = 0; to_slot < to_size; to_slot++) {
            if (inventory_to[to_slot] != null && inventory_to[to_slot].getAmount() != 0) {
                continue;
            }

            for (; from_slot < from_size && inventory_from[from_slot].getAmount() == 0; from_slot++);
            if (from_slot >= from_size) {
                break;
            }

            inventory_to[to_slot] = inventory_from[from_slot];
            inventory_from[from_slot] = null;
            from_slot++;
        }
    }
}
