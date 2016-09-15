/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.google.common.base.Joiner;
import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class KickCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only players can do this.." );
			return true;
		}

		if ( args.length == 0 ) {
			sender.sendMessage( ChatColor.RED + "That's not how you use this command..." );
			sender.sendMessage( ChatColor.YELLOW + "  /kick [player] <reason>" );
			return true;
		}

		Player player = ((Player) sender);

		String compare = args[0];
		if ( compare.length() > 16 ) {
			try {
				compare = UUID.fromString( compare ).toString();
			} catch ( IllegalArgumentException ex ) {
				player.sendMessage( ChatColor.YELLOW + "The player \"" + args[0] + "\" could not be found.." );
				return true;
			}
		}

		String reason = "You have been kicked!";

		if ( args.length > 1 ) {
			reason = Joiner.on( " " ).join( Arrays.copyOfRange( args, 1, args.length ) );
		}

		for ( Player kicker : WorldDimension.getPlayersAllDimensions( player.getWorld() ) ) {

			if ( kicker.getName().equalsIgnoreCase( compare ) || kicker.getUniqueId().toString().equalsIgnoreCase( compare ) ) {
				kicker.kickPlayer( ChatColor.YELLOW + "$$$" + reason );
				sender.sendMessage( ChatColor.GREEN + kicker.getName() + " has been kicked." );
				return true;
			}

		}

		return true;

	}

	private String getNiceWorldName( Player player ) {

		WorldDimension wd = WorldDimension.fromEnvironment( player.getWorld().getEnvironment() );
		return wd == null ? "The Overworld" : wd.getNiceName();

	}

	private String colour( String string ) {
		return ChatColor.translateAlternateColorCodes( '&', string );
	}

}
