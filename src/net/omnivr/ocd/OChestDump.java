package net.omnivr.ocd;

import java.io.File;
import net.omnivr.olib.Util;
import org.bukkit.Server;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
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

    private final OCDPlayerListener playerListener = new OCDPlayerListener(this);
    private final OCDBlockListener blockListener = new OCDBlockListener(this);

    public OChestDump(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
        super(pluginLoader, instance, desc, folder, plugin, cLoader);
    }

    public void onEnable() {
        // Register our events
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_COMMAND, playerListener, Priority.Normal, this);
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
}
