package net.omnivr.ocd;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import net.omnivr.olib.Constants;
import net.omnivr.olib.ItemDB;
import net.omnivr.olib.Trace;
import net.omnivr.olib.Util;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;

/**
 * OChestDump for Bukkit
 *
 * @author Nayruden
 */
public class OChestDump extends JavaPlugin {

    private final OCDBlockListener blockListener = new OCDBlockListener(this);

    public OChestDump(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onEnable() {
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.BLOCK_INTERACT, blockListener, Priority.Monitor, this);

        // Setup configs
        getDataFolder().mkdirs(); // Make sure dir exists
        File config_file = new File(getDataFolder(), "config.yml");
        if (!config_file.isFile()) {
            Util.extractResourceTo("/config.yml", config_file.getPath());
        }

        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " has been loaded.");
    }

    public void onDisable() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (command.getName().equalsIgnoreCase("ocd")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only players can use this command");
                return false;
            }

            Player player = (Player) sender;
            if (args.length < 1 || args.length > 2 || !(args[0].equalsIgnoreCase("stash") || args[0].equalsIgnoreCase("loot") || args[0].equalsIgnoreCase("swap") || args[0].equalsIgnoreCase("sort"))) {
                player.sendMessage(ChatColor.RED + "/ocd <stash|loot|swap> [item]");
                player.sendMessage(ChatColor.RED + "OR /ocd sort [<name|amount|id>]");
                player.sendMessage(ChatColor.RED + "stash - Put all items in inventory in the chest you're looking at");
                player.sendMessage(ChatColor.RED + "loot - Put all items in the chest into your inventory");
                player.sendMessage(ChatColor.RED + "swap - Exhange items between your inventory and chest");
                player.sendMessage(ChatColor.RED + "sort - Sort items by name, amount, or id, defaults to name");
                player.sendMessage(ChatColor.RED + "Adding item ID/name will exchange only that item for stash/loot");
                return false;
            }

            Block block = Trace.Simple(player, 4.0); // 4m max
            if (block == null || block.getType() != Material.CHEST) {
                player.sendMessage(ChatColor.RED + "You need to look at a chest to use this command");
                return false;
            }

            if (!player.isOp() && getConfiguration().getBoolean("require-chest-open", false) && !OCDProtectionInfo.isOwner(block.getLocation().toVector(), player.getName())) {
                player.sendMessage(ChatColor.RED + "You must prove you own this chest by opening it first");
                return false;
            }

            ContainerBlock chest1 = (ContainerBlock) block.getState();
            ContainerBlock chest2 = null;
            ItemStack[] chest1_contents;
            ItemStack[] chest2_contents = null;
            try {
                chest1_contents = chest1.getInventory().getContents();
                for (BlockFace neighbor : Constants.NEIGHBORS) {
                    if (block.getRelative(neighbor).getType() == Material.CHEST) {
                        ContainerBlock neighbor_chest = (ContainerBlock) block.getRelative(neighbor).getState();
                        ItemStack[] neighbor_chest_contents = neighbor_chest.getInventory().getContents();
                        if (neighbor == BlockFace.NORTH || neighbor == BlockFace.EAST) {
                            chest2 = chest1;
                            chest2_contents = chest1_contents;
                            chest1 = neighbor_chest;
                            chest1_contents = neighbor_chest_contents;
                        } else {
                            chest2 = neighbor_chest;
                            chest2_contents = neighbor_chest_contents;
                        }
                        break;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                player.sendMessage(ChatColor.RED + "Sorry, but due to a bug in Bukkit, you can't use OCD on newly");
                player.sendMessage(ChatColor.RED + "placed chests. See http://bit.ly/BukkitChest for more info");
                return false;
            }

            ItemStack[] chest_contents = chest1_contents;
            if (chest2 != null) {
                chest_contents = Util.concat(chest1_contents, chest2_contents);
            }

            ItemStack[] player_contents = player.getInventory().getContents();

            if (args[0].equalsIgnoreCase("sort")) {                
                Comparator<ItemStack> comparator;
                if (args.length == 1 || args[1].equalsIgnoreCase("name")) {
                    comparator = new orderByName();
                } else if (args[1].equalsIgnoreCase("amount")) {
                    comparator = new orderByAmount(chest_contents);
                } else if (args[1].equalsIgnoreCase("id")) {
                    comparator = new orderByID();
                } else {
                    player.sendMessage(ChatColor.RED + "Unknown sort: " + args[1]);
                    return false;
                }
                compactInventory(chest_contents);
                Arrays.sort(chest_contents, comparator);
            } else { // loot, stash, swap

                int item_id = 0;
                if (args.length == 2) {
                    item_id = ItemDB.nameOrIDToID(args[1]);
                    if (item_id == -1) {
                        player.sendMessage(ChatColor.RED + "Unknown item name/id: " + args[1]);
                        return false;
                    }
                }

                if (args[0].equalsIgnoreCase("stash")) {
                    tryFill(player_contents, chest_contents, item_id);
                } else if (args[0].equalsIgnoreCase("loot")) {
                    tryFill(chest_contents, player_contents, item_id);
                } else {
                    ItemStack[] new_chest_contents = new ItemStack[chest_contents.length];
                    tryFill(player_contents, new_chest_contents, 0);
                    tryFill(chest_contents, player_contents, 0);
                    tryFill(chest_contents, new_chest_contents, 0);
                    chest_contents = new_chest_contents;
                }
            }

            if (chest2 == null) {
                chest1_contents = chest_contents;
            } else {
                System.arraycopy(chest_contents, 0, chest1_contents, 0, chest1_contents.length);
                System.arraycopy(chest_contents, chest1_contents.length, chest2_contents, 0, chest2_contents.length);
            }

            chest1.getInventory().setContents(chest1_contents);
            if (chest2 != null) {
                chest2.getInventory().setContents(chest2_contents);
            }
            player.getInventory().setContents(player_contents);

        }

        return true;
    }

    private void tryFill(ItemStack[] inventory_from, ItemStack[] inventory_to, int restricted_item_id) {
        int from_size = inventory_from.length;
        int to_size = inventory_to.length;
        for (int from_slot = 0; from_slot < from_size; from_slot++) {
            if (inventory_from[from_slot] == null) {
                continue;
            }

            ItemStack from_stack = inventory_from[from_slot];
            if (from_stack.getAmount() == 0 || (restricted_item_id != 0 && from_stack.getTypeId() != restricted_item_id)) {
                continue;
            }

            for (int to_slot = 0; to_slot < to_size; to_slot++) {
                ItemStack to_stack = inventory_to[to_slot];
                if (to_stack == null || to_stack.getAmount() == 0) {
                    inventory_to[to_slot] = from_stack;
                    inventory_from[from_slot] = null;
                    break;
                }
                compactStack(from_stack, to_stack); // Compact if possible
            }
        }
    }

    private void compactStack(ItemStack from_stack, ItemStack to_stack) {
        if (from_stack.getType() != Material.AIR && from_stack.getType() == to_stack.getType()) {
            int max = from_stack.getType().getMaxStackSize();
            int diff = Math.min(from_stack.getAmount() + to_stack.getAmount(), max) - to_stack.getAmount();
            to_stack.setAmount(to_stack.getAmount() + diff);
            from_stack.setAmount(from_stack.getAmount() - diff);
        }
    }

    private void compactInventory(ItemStack[] stacks) {
        for (int from = 1; from < stacks.length; from++) {
            for (int to = 0; to < from; to++) {
                compactStack(stacks[from], stacks[to]);
            }
        }
    }

    private class orderByName implements Comparator<ItemStack> {

        public int compare(ItemStack a, ItemStack b) {
            if (a == null || a.getAmount() == 0) {
                return 1;
            }
            if (b == null || b.getAmount() == 0) {
                return -1;
            }
            return a.getType().toString().compareToIgnoreCase(b.getType().toString());
        }
    }

    private class orderByAmount implements Comparator<ItemStack> {

        Map<Integer, Integer> amounts = new TreeMap<Integer, Integer>();

        public orderByAmount(ItemStack[] stacks) {
            for (ItemStack stack : stacks) {
                Integer amount = amounts.get(stack.getTypeId());
                if (amount == null) {
                    amount = Integer.valueOf(0);
                }
                amounts.put(stack.getTypeId(), amount + stack.getAmount());
            }
        }

        public int compare(ItemStack a, ItemStack b) {
            if (a == null || a.getAmount() == 0) {
                return 1;
            }
            if (b == null || b.getAmount() == 0) {
                return -1;
            }
            return amounts.get(b.getTypeId()) - amounts.get(a.getTypeId());
        }
    }

    private class orderByID implements Comparator<ItemStack> {

        public int compare(ItemStack a, ItemStack b) {
            if (a == null || a.getAmount() == 0) {
                return 1;
            }
            if (b == null || b.getAmount() == 0) {
                return -1;
            }
            return a.getTypeId() - b.getTypeId();
        }
    }
}
