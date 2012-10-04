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

	// init global vars
	public static final Logger log = Logger.getLogger("Legacy");
	public static Map<Player,Long> timeTracker = new HashMap<Player,Long>(100);
	public static Map<Player,Long> timeAway = new HashMap<Player,Long>(100);

	// init config
	public static FileConfiguration pluginConfig = null;
	static File pluginConfigFile = null;
	public static FileConfiguration logConfig = null;
	static File logConfigFile = null;

	@Override
	public void onDisable() {

		// initialize variables
		Date now = new Date();
		long playerSession;

		// cycle players
		for (Player each : getServer().getOnlinePlayers()) {
			playerSession = (now.getTime() - timeTracker.get(each)) / 1000;

			// save in config
			if (logConfig.contains(each.getName()))
				logConfig.set(each.getName(), logConfig.getLong(each.getName()) + playerSession);
			else
				logConfig.set(each.getName(), playerSession);
			saveLogConfig();

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
		getServer().getPluginManager().registerEvents(new EventListener(), this);

		// setup config file
		loadPluginConfig();
		savePluginConfig();
		loadLogConfig();
		saveLogConfig();

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
		int delay = pluginConfig.getInt("auto-save.frequency") * 20 * 60;
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			public void run() { savePlayerTime(); }
		}, delay, delay);

		// schedule idler
		delay = pluginConfig.getInt("idle.freqency") * 20 * 60;
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			public void run() { idleTime(); }
		}, delay, delay);

		// console
		log.info(this + " is now enabled.");
	}

	public void idleTime() {

		// init vars
		Date date = new Date();

		// scan through players for idle
		for (Map.Entry<Player,Long> entry : timeAway.entrySet())
			if (entry.getValue() + (pluginConfig.getInt("idle.timeout") * 1000 * 60) < date.getTime()) {
				entry.getKey().sendMessage("[Legacy] Paused due to idle.");
				pausePlayerLegacy(entry.getKey());
			}
	}

	public FileConfiguration loadPluginConfig() {
		if (pluginConfig == null) {
			if (pluginConfigFile == null)
				pluginConfigFile = new File(this.getDataFolder(), "config.yml");
			if (pluginConfigFile.exists()) {
				pluginConfig = YamlConfiguration.loadConfiguration(pluginConfigFile);
			} else {
				InputStream configStream = getResource("config.yml");
				pluginConfig = YamlConfiguration.loadConfiguration(configStream);
			}
		}
		return pluginConfig;
	}

	public static void savePluginConfig() {
		if (pluginConfig == null || pluginConfigFile == null)
			return;
		try {
			pluginConfig.save(pluginConfigFile);
		} catch (IOException e) {
			log.severe("[Legacy] Unable to save config to " + pluginConfigFile + '.');
		}
	}

	public FileConfiguration loadLogConfig() {
		if (logConfig == null) {
			if (logConfigFile == null)
				logConfigFile = new File(this.getDataFolder(), "log.yml");
			if (logConfigFile.exists()) {
				logConfig = YamlConfiguration.loadConfiguration(logConfigFile);
			} else {
				InputStream configStream = getResource("log.yml");
				logConfig = YamlConfiguration.loadConfiguration(configStream);
			}
		}
		return logConfig;
	}

	public static void saveLogConfig() {
		if (logConfig == null || logConfigFile == null)
			return;
		try {
			logConfig.save(logConfigFile);
		} catch (IOException e) {
			log.severe("[Legacy] Unable to save log to " + logConfigFile + '.');
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
				if (logConfig.contains(each.getName()))
					logConfig.set(each.getName(), logConfig.getLong(each.getName()) + playerSession);
				else
					logConfig.set(each.getName(), playerSession);
				saveLogConfig();

				// remove from map
				timeTracker.remove(each);
			}
		}

		// add players to timeTracker
		now = new Date();
		for (Player each : getServer().getOnlinePlayers())
			timeTracker.put(each, now.getTime());
	}

	// remove player time from map
	public static void pausePlayerLegacy(Player player) {

		if (!timeTracker.containsKey(player))
			return;

		// initialize variables
		Date now = new Date();
		long playerSession = (now.getTime() - timeTracker.get(player)) / 1000;

		// save in config
		if (logConfig.contains(player.getName()))
			logConfig.set(player.getName(), logConfig.getLong(player.getName()) + playerSession);
		else
			logConfig.set(player.getName(), playerSession);
		saveLogConfig();

		// remove from map
		timeTracker.remove(player);
	}

	public static void resumePlayerLegacy(Player player) {

		if (timeTracker.containsKey(player))
			return;

		// add player to timeTracker
		timeTracker.put(player, new Date().getTime());
	}
}