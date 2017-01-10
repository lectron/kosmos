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

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class BanCommands implements CommandExecutor, TabCompleter {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can ban and unban people!" );
			return true;
		}

		Player player = ((Player) sender);

		if ( !Objects.equals( player.getUniqueId(), WorldDimension.getUUIDOfWorld( player.getWorld() ) ) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can ban and unban people!" );
			return true;
		}

		WorldData worldData = core.getPlayerHandler().getWorldData( player.getUniqueId() );
		if ( worldData == null ) {
			sender.sendMessage( ChatColor.RED + "We were unable to load the world data!" );
			return true;
		}

		switch ( command.getName().toLowerCase() ) {

			case "ban":
				banCommand( player, args, false, worldData );
				break;
			case "unban":
				banCommand( player, args, true, worldData );
				break;
			case "list":
				listBans( player, args, worldData );
			default:
				sendHelp( sender );
				break;

		}

		return true;
	}

	private void listBans( Player player, String[] args, WorldData worldData ) {

		int page = 1;
		if( args.length == 1 ) {
			try {
				page = Integer.parseInt( args[0] );
			} catch ( Exception ex ) {
				player.sendMessage( ChatColor.RED + "That page number is invalid!" );
				return;
			}
		}

		page--;
		if( page < 1 ) {
			player.sendMessage( ChatColor.RED + "That page number is invalid!" );
			return;
		}
		int offset = (page - 1) * 8;

		ArrayList<HashMap.Entry<UUID, PunishEntry>> punishments = new ArrayList<>( worldData.getBannedUsers().entrySet() );
		ArrayList<String> messages = new ArrayList<>();

		for( int i = offset; i<punishments.size(); i++ ) {
			Map.Entry<UUID, PunishEntry> entry = punishments.get( i );
			if( entry.getValue().isBanned() ) continue;

			String name = worldData.getUsername( entry.getKey() );

			messages.add( ChatColor.RED + "Banned user: " + ChatColor.DARK_RED + name );
			messages.add( ChatColor.RED + "  Reason: " + ChatColor.YELLOW + entry.getValue().getReason() );

		}

		player.sendMessage( messages.toArray( new String[ messages.size() ] ) );

	}

	@Override
	public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args ) {

		// Don't accept non players.. Lazy.
		if ( !(sender instanceof Player) ) return Collections.emptyList();

		// Send the list of home names.
		String commandName = command.getName();
		if ( commandName.equalsIgnoreCase( "ban" ) || commandName.equalsIgnoreCase( "unban" ) ) {

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

	private void banCommand( Player player, String[] args, boolean remove, WorldData worldData ) {

		// TODO tempban.

		if ( args.length != 1 ) {
			player.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			String un = remove ? "un" : "";
			player.sendMessage( ChatColor.YELLOW + " /" + un + "ban [player]" );
			return;
		}

		Callback<Callable<Void>, UUID> uuidCallback = param -> () -> {
			if ( param == null ) {
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
			} else {

				if ( remove ) {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" is no longer banned!" );
					worldData.getBannedUsers().remove( param );
				} else {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" is now banned!" );
					worldData.getBannedUsers().put( param, new PunishEntry( -1, "You are banned." ) );


					Player offender = Bukkit.getPlayer( param );
					if ( offender != null && WorldDimension.getPlayersAllDimensions( player.getWorld() ).contains( offender ) ) {
						offender.kickPlayer( ChatColor.RED + "You're banned from this server." );
					}
				}

				// TODO kick player.

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

	private void sendHelp( CommandSender sender ) {

		String cmd = ChatColor.BOLD.toString() + ChatColor.YELLOW;
		String inf = ChatColor.BLUE.toString();

		sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.BLUE + "The ban commands are used to prevent players from accessing your server." );
		sender.sendMessage( cmd + "/ban <player>" );
		sender.sendMessage( inf + " - Ban the specified player." );
		sender.sendMessage( cmd + "/unban <player>" );
		sender.sendMessage( inf + " - Unban the specified player." );

	}

}
