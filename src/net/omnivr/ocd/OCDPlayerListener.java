package net.omnivr.ocd;

import net.omnivr.olib.Constants;
import net.omnivr.olib.ItemDB;
import net.omnivr.olib.Trace;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
            if (pieces.length < 2 || pieces.length > 3 || !(pieces[1].equalsIgnoreCase("stash") || pieces[1].equalsIgnoreCase("loot") || pieces[1].equalsIgnoreCase("swap"))) {
                player.sendMessage(ChatColor.RED + "/ocd <stash|loot|swap> [item]");
                player.sendMessage(ChatColor.RED + "stash - Put all items in inventory in the chest you're looking at");
                player.sendMessage(ChatColor.RED + "loot - Put all items in the chest into your inventory");
                player.sendMessage(ChatColor.RED + "swap - Exhange items between your inventory and chest");
                player.sendMessage(ChatColor.RED + "Adding item ID/name will exchange only that item for stash/loot");
                return;
            }

            int item_id = 0;
            if (pieces.length == 3) {
                item_id = ItemDB.nameOrIDToID(pieces[2]);
                if (item_id == -1) {
                    player.sendMessage(ChatColor.RED + "Unknown item name/id: " + pieces[2]);
                    return;
                }
            }

            Block block = Trace.Simple(player, 4.0); // 4m max
            if (block == null || block.getType() != Material.CHEST) {
                player.sendMessage(ChatColor.RED + "You need to look at a chest to use this command");
                return;
            }

            ContainerBlock chest1 = (ContainerBlock) block.getState();
            ContainerBlock chest2 = null;
            ItemStack[] chest1_contents = chest1.getInventory().getContents();
            ItemStack[] chest2_contents = null;
            for (BlockFace neighbor : Constants.NEIGHBORS) {
                if (block.getRelative(neighbor).getType() == Material.CHEST) {
                    chest2 = (ContainerBlock) block.getRelative(neighbor).getState();
                    chest2_contents = chest2.getInventory().getContents();
                    break;
                }
            }

            ItemStack[] player_contents = player.getInventory().getContents();

            if (pieces[1].equalsIgnoreCase("stash")) {
                tryFill(player_contents, chest1_contents, item_id);
                tryFill(player_contents, chest2_contents, item_id);
            } else if (pieces[1].equalsIgnoreCase("loot")) {
                tryFill(chest1_contents, player_contents, item_id);
                tryFill(chest2_contents, player_contents, item_id);
            } else if (chest2 == null) {
                ItemStack[] new_chest1_contents = new ItemStack[chest1_contents.length];
                tryFill(player_contents, new_chest1_contents, 0);
                tryFill(chest1_contents, player_contents, 0);
                chest1_contents = new_chest1_contents;
            } else {
                ItemStack[] new_player_contents = new ItemStack[player_contents.length];
                tryFill(chest1_contents, new_player_contents, 0);
                tryFill(chest2_contents, new_player_contents, 0);
                tryFill(player_contents, chest1_contents, 0);
                tryFill(player_contents, chest2_contents, 0);
                player_contents = new_player_contents;
            }
            chest1.getInventory().setContents(chest1_contents);
            if (chest2 != null) {
                chest2.getInventory().setContents(chest2_contents);
            }
            player.getInventory().setContents(player_contents);

            event.setCancelled(true);

        }
    }

    private void tryFill(ItemStack[] inventory_from, ItemStack[] inventory_to, int restricted_item_id) {
        if (inventory_from == null || inventory_to == null) {
            return;
        }

        int from_size = inventory_from.length;
        int to_size = inventory_to.length;
        int from_slot = 0;
        for (int to_slot = 0; to_slot < to_size; to_slot++) {
            if (inventory_to[to_slot] != null && inventory_to[to_slot].getAmount() != 0) {
                continue;
            }

            for (; from_slot < from_size && (inventory_from[from_slot] == null || inventory_from[from_slot].getAmount() == 0
                    || (restricted_item_id != 0 && inventory_from[from_slot].getTypeId() != restricted_item_id)); from_slot++);
            if (from_slot >= from_size) {
                break;
            }

            inventory_to[to_slot] = inventory_from[from_slot];
            inventory_from[from_slot] = null;
            from_slot++;
        }
    }
}
