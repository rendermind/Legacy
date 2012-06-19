package co.viocode.legacy;

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
    
    static public Map<Player,Long> timeTracker = new HashMap<Player,Long>(128);
    
    @Override
    public void onDisable() {
	
	// initialize variables
	Date now = new Date();
	long playerSession;
	
	// cycle players
	for (Player each : getServer().getOnlinePlayers()) {
	    playerSession = (now.getTime() - timeTracker.get(each)) / 1000;
	    
	    // save in config
	    if (config.contains(each.getName()))
		config.set(each.getName(), config.getLong(each.getName()) + playerSession);
	    else
		config.set(each.getName(), playerSession);
	    saveLegacyConfig();
	    
	    // remove from map
	    timeTracker.remove(each);
	}
        
        // display to console
        log.info("[Legacy] Auto-saved player time");
	
	// console
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
	
	// add players to timeTracker
	Date now = new Date();
	for (Player each : getServer().getOnlinePlayers())
	    timeTracker.put(each, now.getTime());
	
	// metrics
	try {
	    Metrics metrics = new Metrics(this);
	    metrics.start();
	} catch (IOException e) {
	    log.warning("Legacy] Failed to submit metrics.");
	}
	
        // schedule auto-save
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() { savePlayerTime(); }
	}, 18000, 18000);
        
	// console
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
    
    // save player time to config
    public void savePlayerTime() {
        
        // initialize variables
	Date now = new Date();
	long playerSession;
	
	// cycle players
	for (Player each : getServer().getOnlinePlayers()) {
            if (timeTracker.containsKey(each)) {
                playerSession = (now.getTime() - timeTracker.get(each)) / 1000;
	    
                // save in config
                if (config.contains(each.getName()))
                    config.set(each.getName(), config.getLong(each.getName()) + playerSession);
                else
                    config.set(each.getName(), playerSession);
                saveLegacyConfig();
	    
                // remove from map
                timeTracker.remove(each);
            }
	}
        
        // add players to timeTracker
	now = new Date();
	for (Player each : getServer().getOnlinePlayers())
	    timeTracker.put(each, now.getTime());
    }
}