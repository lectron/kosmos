/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.data.local.userdata.UserData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class HomeCommands implements CommandExecutor, TabCompleter {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		switch ( command.getName().toLowerCase() ) {

			case "sethome":
				setHomeCommand( sender, args, false );
				break;
			case "delhome":
				setHomeCommand( sender, args, true );
				break;
			case "home":
				homeCommand( sender, args );
				break;
			default:
				sendHelp( sender );
				break;

		}

		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args ) {

		// Don't accept non players.. Lazy.
		if( !(sender instanceof Player) ) return Collections.emptyList();
		Player player = ((Player) sender);

		// Load the userdata and the player's homes.
		UserData userData = core.getPlayerHandler().getUserData( player.getUniqueId() );
		if( userData == null ) return Collections.emptyList();

		HashMap<String, Location> homes = userData.getHomes();

		// Send the list of home names.
		String commandName = command.getName();
		if( commandName.equalsIgnoreCase( "home" ) || commandName.equalsIgnoreCase( "sethome" ) || commandName.equalsIgnoreCase( "delhome" ) ) {

			if( args.length == 0 ) {

					return new ArrayList<>( homes.keySet() );

			} else if( args.length == 1 ) {

				String search = args[0].toLowerCase();
				return homes.keySet().stream().filter( home -> home.toLowerCase().startsWith( search ) ).collect( Collectors.toList() );

			}

		}

		return Collections.emptyList();

	}

	private void setHomeCommand( CommandSender sender, String[] args, boolean remove ) {

		if( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.YELLOW + "Only players can set/remove homes." );
			return;
		}

		Player player = ((Player) sender);

		if ( args.length > 1 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			String un = remove ? "del" : "";
			player.sendMessage( ChatColor.YELLOW + " /" + un + "sethome <homename>" );
			return;
		}

		UserData userData = core.getPlayerHandler().getUserData( player.getUniqueId() );
		if( userData == null ) {
			sender.sendMessage( ChatColor.RED + "We were unable to load your data!" );
			return;
		}

		HashMap<String, Location> homes = userData.getHomes();
		String homeName = args.length == 1 ? args[0].toLowerCase() : "home";

		if( remove ) {
			homes.remove( homeName.trim() );
		} else {
			homes.put( homeName.trim(), player.getLocation() );
		}

		player.sendMessage( ChatColor.GREEN + "Your home has been updated!" );

		core.getPlayerHandler().save( userData );

	}

	private void homeCommand( CommandSender sender, String[] args ) {

		Player player;
		String homeName;

		if( !(sender instanceof Player) ) {

			if ( args.length != 1 && args.length != 2 ) {
				sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
				sender.sendMessage( ChatColor.RED + "You can only send users to their home via console." );
				sender.sendMessage( ChatColor.YELLOW + " /home [user] <homename>" );
				return;
			} else {

				player = Bukkit.getPlayer( args[0] );
				homeName = args.length == 2 ? args[1] : null;

				if( player == null ) {
					sender.sendMessage( ChatColor.YELLOW + "The specified player couldn't be found! :(" );
					return;
				}

				sender.sendMessage( ChatColor.GREEN + "Player teleported to the relevant home.." );

			}

		} else {

			if ( args.length > 1 ) {
				sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
				sender.sendMessage( ChatColor.YELLOW + " /home <homename>" );
				return;
			}

			player = ((Player) sender);
			homeName = args.length == 1 ? args[0] : null;

		}

		teleportPlayerToHome( player, homeName );

	}

	private void sendHelp( CommandSender sender ) {

		String cmd = ChatColor.BOLD.toString() + ChatColor.YELLOW;
		String inf = ChatColor.BLUE.toString();

		sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.BLUE + "The home commands are a useful bunch of commands that allow you to set, teleport to, and delete homes" );
		sender.sendMessage( cmd + "/home <home name>" );
		sender.sendMessage( inf + " - Will teleport you to a home." );
		sender.sendMessage( cmd + "/sethome <home name>" );
		sender.sendMessage( inf + " - Set the home, with the specified name." );
		sender.sendMessage( cmd + "/delhome <home name>" );
		sender.sendMessage( inf + " - Remove the home with the specified name." );

	}

	private void teleportPlayerToHome( @NonNull Player player, String homeName ) {

		String ending = homeName == null ? "." : " \"" + homeName + "\".";

		player.teleport( getHomeLocation( player, homeName ), PlayerTeleportEvent.TeleportCause.COMMAND );
		player.sendMessage( ChatColor.BLUE + "You've been teleported to your home" + ending );

	}

	private Location getHomeLocation( @NonNull Player player, String homeName ) {

		UserData userData = core.getPlayerHandler().getUserData( player.getUniqueId() );
		if( userData == null ) return player.getWorld().getSpawnLocation();

		HashMap<String, Location> homes = userData.getHomes();

		if( homeName == null ) {

			if( homes.size() == 1 ) {
				return homes.values().iterator().next();
			} else if( homes.containsKey( "home" ) ) {
				return homes.get( "home" );
			}

		} else {
			if( homes.containsKey( homeName.toLowerCase() ) ) return homes.get( homeName.toLowerCase() );
		}

		return userData.getBedLocation() == null ? player.getWorld().getSpawnLocation() : userData.getBedLocation();

	}

}
