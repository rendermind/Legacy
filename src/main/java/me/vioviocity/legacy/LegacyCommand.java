package me.vioviocity.legacy;

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
	
	// initialize core variables
	Player player = (Player) sender;
	
	// command handler
	String cmd = command.getName().toLowerCase();
	if (cmd.equals("legacy")) {
	    //invalid args
	    if (args.length > 1)
		return false;
	    
	    // <command>
	    if (args.length == 0) {
		// check permission
		if (!Legacy.checkPermission("legacy.check", player))
		    return true;
		
	    }
	    
	    // <command> (player)
	    if (args.length == 1) {
		// check permission
		if (!Legacy.checkPermission("legacy.check.others", player))
		    return true;
		
	    }
	}
	
	// end of command
	return false;
    }
}
