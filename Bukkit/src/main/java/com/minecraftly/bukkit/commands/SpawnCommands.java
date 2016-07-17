/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.worlddata.WorldData;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class SpawnCommands implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		switch ( command.getName().toLowerCase() ) {

			case "setspawn":
				setSpawnCommand( sender, args );
				break;
			case "spawn":
				spawnCommand( sender, args );
				break;
			default:
				sendHelp( sender );
				break;

		}

		return true;
	}

	private void setSpawnCommand( CommandSender sender, String[] args ) {

		if( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can set the spawn point!" );
			return;
		}

		Player player = ((Player) sender);

		if( !Objects.equals( player.getUniqueId(), WorldDimension.getUUIDOfWorld( player.getWorld() ) ) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can set the spawn point!" );
			return;
		}

		if ( args.length > 1 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			sender.sendMessage( ChatColor.YELLOW + " /setspawn" );
			return;
		}

		WorldData worldData = core.getPlayerHandler().getWorldData( player.getUniqueId() );
		if( worldData == null ) {
			sender.sendMessage( ChatColor.RED + "We were unable to load the world data!" );
			return;
		}

		Location loc = player.getLocation();
		player.getWorld().setSpawnLocation( loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() );

		player.sendMessage( ChatColor.GREEN + "Your spawn point has been updated!" );

	}

	private void spawnCommand( CommandSender sender, String[] args ) {

		Player player;

		if( !(sender instanceof Player) ) {
				sender.sendMessage( ChatColor.RED + "Only players can go to spawn!" );
		} else {

			if ( args.length != 0 ) {
				sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
				sender.sendMessage( ChatColor.YELLOW + " /spawn" );
				return;
			}

			player = ((Player) sender);
			player.teleport( player.getWorld().getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND );

		}

	}

	private void sendHelp( CommandSender sender ) {

		String cmd = ChatColor.BOLD.toString() + ChatColor.YELLOW;
		String inf = ChatColor.BLUE.toString();

		sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.BLUE + "The spawn commands are a useful bunch of commands that allow you to set, and teleport to the world spawn." );
		sender.sendMessage( cmd + "/spawn" );
		sender.sendMessage( inf + " - Will teleport you to the spawn." );
		sender.sendMessage( cmd + "/setspawn" );
		sender.sendMessage( inf + " - Set the spawn location." );

	}

}
