package me.vioviocity.legacy;

import java.util.Date;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LegacyCommand implements CommandExecutor {
    
    private Legacy plugin;
    public LegacyCommand(Legacy plugin) {
	this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
	if (!(sender instanceof Player)) {
	    sender.sendMessage("[Legacy] Command not yet supported in console.");
	    return true;
	}
	
	// initialize variables
	Player player = (Player) sender;
	Date now = new Date();
	
	// command handler
	String cmd = command.getName().toLowerCase();
	if (cmd.equals("legacy")) {
	    //invalid args
	    if (args.length > 1)
		return false;
	    
	    // <command>
	    if (args.length == 0) {
		// check permission
		if (!Legacy.checkPermission("legacy.check", player) || !Legacy.checkPermission("legacy.check.others", player))
		    return true;
		
		// initialize variables
		long totalTime = 0;
		
		// search current session
		for (Map.Entry<Player,Long> entry : Legacy.timeTracker.entrySet())
		    if (entry.getKey().equals(player))
			totalTime += (now.getTime() - entry.getValue()) / 1000;
		
		// search prior sessions
		if (Legacy.config.contains(player.getName()))
		    totalTime += Legacy.config.getLong(player.getName());
		
		// display to player
		player.sendMessage(ChatColor.GREEN + "Time played:  " + timePlayed(totalTime) + '.');
		return true;
	    }
	    
	    // <command> (player)
	    if (args.length == 1) {
		// check permission
		if (!Legacy.checkPermission("legacy.check.others", player))
		    return true;
		
		// initialize variables
		String playerName = args[0];
		long totalTime = 0;
		
		// find case-sensitive player name
		for (Player each : plugin.getServer().getOnlinePlayers())
		    if (each.getName().toLowerCase().contains(playerName.toLowerCase()))
			playerName = each.getName();
		for (OfflinePlayer each : plugin.getServer().getOfflinePlayers())
		    if (each.getName().toLowerCase().contains(playerName.toLowerCase()))
			playerName = each.getName();
		
		// search current session
		for (Map.Entry<Player,Long> entry : Legacy.timeTracker.entrySet())
		    if (entry.getKey().getName().contains(playerName))
			totalTime += (now.getTime() - entry.getValue()) / 1000;
		
		// search prior sessions
		if (Legacy.config.contains(playerName))
		    totalTime += Legacy.config.getLong(playerName);
		
		// player not found
		if (totalTime == 0) {
		    player.sendMessage(ChatColor.RED + "Player not found.");
		    return true;
		}
		
		// display to player
		player.sendMessage(ChatColor.GREEN + "Time played:  " + timePlayed(totalTime) + '.');
		return true;
	    }
	}
	
	// end of command
	return false;
    }
    
    public String timePlayed(Long totalTime) {
	
	// initialize variables
	long days; long hours; long minutes;
	String _days; String _hours; String _minutes;
	
	// calculate days
	days = totalTime / 86400;
	if (days == 1)
	    _days = "day";
	else
	    _days = "days";
	
	// calculate hours
	hours = (totalTime / 3600) - (days * 24);
	if (hours == 1)
	    _hours = "hour";
	else
	    _hours = "hours";
	
	// calculate minutes
	minutes = (totalTime / 60) - (hours * 60);
	if (minutes == 1)
	    _minutes = "minute";
	else
	    _minutes = "minutes";
	
	// return formatted time played
	return days + " " + _days + ", " + hours + " " + _hours + ", " + minutes + " " + _minutes;
    }
}