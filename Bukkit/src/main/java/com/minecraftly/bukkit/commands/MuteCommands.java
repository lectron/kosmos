/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.worlddata.PunishEntry;
import com.minecraftly.bukkit.world.data.local.worlddata.WorldData;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.util.Callback;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class MuteCommands implements CommandExecutor, TabCompleter {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can mute and unmute people!" );
			return true;
		}

		Player player = ((Player) sender);

		if ( !Objects.equals( player.getUniqueId(), WorldDimension.getUUIDOfWorld( player.getWorld() ) ) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can mute and unmute people!" );
			return true;
		}

		WorldData worldData = core.getPlayerHandler().getWorldData( player.getUniqueId() );
		if ( worldData == null ) {
			sender.sendMessage( ChatColor.RED + "We were unable to load the world data!" );
			return true;
		}

		switch ( command.getName().toLowerCase() ) {

			case "mute":
				muteCommand( player, args, false, worldData );
				break;
			case "unmute":
				muteCommand( player, args, true, worldData );
				break;
			case "muted":
				mutedCommand( player, args, worldData );
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
		if ( !(sender instanceof Player) ) return Collections.emptyList();

		// Send the list of home names.
		String commandName = command.getName();
		if ( commandName.equalsIgnoreCase( "mute" ) || commandName.equalsIgnoreCase( "unmute" ) ) {

			if ( args.length == 1 ) {

				String search = args[0].toLowerCase();
				return Bukkit.getOnlinePlayers()
						.stream()
						.filter( player1 -> player1.getName().toLowerCase().startsWith( search ) )
						.map( Player::getName )
						.collect( Collectors.toList() );

			}

		}

		return Collections.emptyList();

	}

	private void muteCommand( Player player, String[] args, boolean remove, WorldData worldData ) {

		// TODO tempmute.

		if ( args.length != 1 ) {
			player.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			String un = remove ? "un" : "";
			player.sendMessage( ChatColor.YELLOW + " /" + un + "mute [player]" );
			return;
		}

		Callback<Callable<Void>, UUID> uuidCallback = param -> () -> {
			if ( param == null ) {
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
			} else {

				Player punishee = Bukkit.getPlayer( param );

				if ( remove ) {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" is no longer muted!" );
					if ( worldData.getMutedUsers().remove( param ) != null && punishee != null ) {
						punishee.sendMessage( ChatColor.GREEN + "You are no longer muted!" );
					}

				} else {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" is now muted!" );
					worldData.getMutedUsers().put( param, new PunishEntry( -1, "You are muted." ) );
					if ( punishee != null ) punishee.sendMessage( ChatColor.DARK_RED + "You have been muted!" );
				}

				core.getPlayerHandler().save( worldData );

			}
			return null;
		};

		player.sendMessage( ChatColor.YELLOW + "Processing..." );
		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {

			try ( Jedis jedis = core.getJedis() ) {

				UUID uuid = core.getUUIDManager().getUuid( jedis, args[0] );
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), uuidCallback.call( uuid ) );

			} catch ( ProcessingException | NoJedisException ex ) {
				ex.printStackTrace();
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
				player.sendMessage( ChatColor.RED + "Error:  " + ex.getMessage() );
			}

		} );

	}

	private void mutedCommand( Player player, String[] args, WorldData worldData ) {

		player.sendMessage( ChatColor.BLUE + "Currently online muted users:" );

		WorldDimension.getPlayersAllDimensions( player.getWorld() )
				.stream()
				.filter( player1 -> worldData.getTrustedUsers().contains( player1.getUniqueId() ) )
				.map( Player::getName )
				.map( s -> ChatColor.YELLOW + " - " + s )
				.forEach( player::sendMessage );

	}

	private void sendHelp( CommandSender sender ) {

		String cmd = ChatColor.BOLD.toString() + ChatColor.YELLOW;
		String inf = ChatColor.BLUE.toString();

		sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.BLUE + "The mute commands are used to prevent annoying people from chatting." );
		sender.sendMessage( cmd + "/mute <player>" );
		sender.sendMessage( inf + " - Mute the specified player." );
		sender.sendMessage( cmd + "/unmute <player>" );
		sender.sendMessage( inf + " - Unmute the specified player." );
		sender.sendMessage( cmd + "/muteed" );
		sender.sendMessage( inf + " - Get a list of all online and muted players." );

	}

}
