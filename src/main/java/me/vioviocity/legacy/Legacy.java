package me.vioviocity.legacy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Legacy extends JavaPlugin implements Listener {
    
    static public Logger log = Logger.getLogger("Legacy");
    
    static public FileConfiguration config = null;
    static File configFile = null;
    
    static public Map<Player,Long> playerTime = new HashMap<Player,Long>(100);
    
    @Override
    public void onDisable() {
        log.info(this + " is now disabled.");
    }

    @Override
    public void onEnable() {
	// register events
        getServer().getPluginManager().registerEvents(new LegacyListener(), this);
	
	// setup config file
	loadLegacyConfig();
	saveLegacyConfig();
	
	// register commands
	getCommand("legacy").setExecutor(new LegacyCommand(this));
	
	log.info(this + " is now enabled.");
    }
    
    public FileConfiguration loadLegacyConfig() {
	if (config == null) {
	    if (configFile == null)
		configFile = new File(this.getDataFolder(), "log.yml");
	    if (configFile.exists()) {
		config = YamlConfiguration.loadConfiguration(configFile);
	    } else {
		InputStream configStream = getResource("log.yml");
		config = YamlConfiguration.loadConfiguration(configStream);
	    }
	}
	return config;
    }
    
    static public void saveLegacyConfig() {
	if (config == null || configFile == null)
	    return;
	try {
	    config.save(configFile);
	} catch (IOException e) {
	    log.severe("[Legacy] Unable to save log to " + configFile + '.');
	}
    }
    
    // check permission node
    static public boolean checkPermission(String permission, Player player) {
	if(!player.hasPermission(permission)) {
	    player.sendMessage(ChatColor.RED + "You do not have permission.");
	    log.info("[Legacy] " + player.getName() + " was denied permission to " + permission + '.');
	    return false;
	} else {
	    return true;
	}
    }
}