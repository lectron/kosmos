/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.worlddata.WorldData;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.util.Callback;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
public class TrustCommands implements CommandExecutor, TabCompleter {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can trust and untrust people!" );
			return true;
		}

		Player player = ((Player) sender);

		if( !Objects.equals( player.getUniqueId(), WorldDimension.getUUIDOfWorld( player.getWorld() ) ) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can trust and untrust people!" );
			return true;
		}

		WorldData worldData = core.getPlayerHandler().getWorldData( player.getUniqueId() );
		if( worldData == null ) {
			sender.sendMessage( ChatColor.RED + "We were unable to load the world data!" );
			return true;
		}

		switch ( command.getName().toLowerCase() ) {

			case "trust":
				trustCommand( player, args, false, worldData );
				break;
			case "untrust":
				trustCommand( player, args, true, worldData );
				break;
			case "trusted":
				trustedCommand( player, args, worldData );
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

		// Send the list of home names.
		String commandName = command.getName();
		if( commandName.equalsIgnoreCase( "trust" ) || commandName.equalsIgnoreCase( "untrust" ) ) {

			if( args.length == 1 ) {

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

	private void trustCommand( Player player, String[] args, boolean remove, WorldData worldData ) {

		if( args.length != 1 ) {
			player.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			String un = remove ? "un" : "";
			player.sendMessage( ChatColor.YELLOW + " /" + un + "trust [player]" );
			return;
		}

		Callback<Callable<Void>, UUID> uuidCallback = param -> () -> {
			if( param == null ) {
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
			} else {

				Player trustee = Bukkit.getPlayer( param );
				if ( trustee == player ) {
					player.sendMessage( ChatColor.RED + "You may not trust/untrust yourself." );
					return null;
				}

				if( remove ) {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" is no longer trusted!" );
					worldData.getTrustedUsers().remove( param );
					if( trustee != null ) {
						trustee.setGameMode( GameMode.ADVENTURE );
						trustee.sendMessage( ChatColor.DARK_RED + "You are no longer trusted!" );
					}
				} else {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" is now trusted!" );
					worldData.getTrustedUsers().add( param );
					if( trustee != null ) {
						trustee.setGameMode( GameMode.SURVIVAL );
						trustee.sendMessage( ChatColor.GREEN + "You have been trusted!" );
					}
				}

				core.getPlayerHandler().save( worldData );

			}
			return null;
		};

		player.sendMessage( ChatColor.YELLOW + "Processing..." );
		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {

			try ( Jedis jedis = core.getJedis() ){

				UUID uuid = core.getUUIDManager().getUuid( jedis, args[0] );
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), uuidCallback.call( uuid ) );

			} catch ( ProcessingException | NoJedisException ex ) {
				ex.printStackTrace();
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
				player.sendMessage( ChatColor.RED + "Error:  " + ex.getMessage() );
			}

		} );

	}

	private void trustedCommand( Player player, String[] args, WorldData worldData ) {

		player.sendMessage( ChatColor.BLUE + "Currently online trusted users:" );

		WorldDimension.getPlayersAllDimensions( player.getWorld() )
				.stream()
				.filter( player1 -> worldData.getTrustedUsers().contains( player1.getUniqueId() ) )
				.map( Player::getName )
				.map( s -> ChatColor.GREEN + " - " + s )
				.forEach( player::sendMessage );

	}

	private void sendHelp( CommandSender sender ) {

		String cmd = ChatColor.BOLD.toString() + ChatColor.YELLOW;
		String inf = ChatColor.BLUE.toString();

		sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.BLUE + "The trust commands allow certain people to build on your world." );
		sender.sendMessage( cmd + "/trust <player>" );
		sender.sendMessage( inf + " - Trust the specified player." );
		sender.sendMessage( cmd + "/untrust <player>" );
		sender.sendMessage( inf + " - Untrust the specified player." );
		sender.sendMessage( cmd + "/trusted" );
		sender.sendMessage( inf + " - Get a list of all online and trusted players." );

	}

}
