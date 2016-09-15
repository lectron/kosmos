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
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class WhiteListCommands implements CommandExecutor, TabCompleter {

	private final MinecraftlyBukkitCore core;

	@Override
	public boolean onCommand( CommandSender sender, Command command, String label, String[] args ) {

		if ( !command.getName().toLowerCase().equalsIgnoreCase( "whitelist" ) || args.length == 0 ) {
			sendHelp( sender );
			return true;
		}

		if ( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.RED + "Only players can manipulate white lists." );
			return true;
		}

		Player player = ((Player) sender);

		if ( !Objects.equals( player.getUniqueId(), WorldDimension.getUUIDOfWorld( player.getWorld() ) ) ) {
			sender.sendMessage( ChatColor.RED + "Only world owners can manipulate white lists!" );
			return true;
		}

		WorldData worldData = core.getPlayerHandler().getWorldData( player.getUniqueId() );
		if ( worldData == null ) {
			sender.sendMessage( ChatColor.RED + "We were unable to load the world data!" );
			return true;
		}

		switch ( args[0].toLowerCase() ) {

			case "add":
				playerManipulate( player, args, true, worldData );
				break;
			case "del":
			case "delete":
			case "remove":
				playerManipulate( player, args, false, worldData );
				break;
			case "on":
				toggleWhitelist( player, true, worldData );
				break;
			case "off":
				toggleWhitelist( player, false, worldData );
				break;
			case "toggle":
				toggleWhitelist( player, !worldData.isWhiteListed(), worldData );
				break;
			default:
				sendHelp( player );
				break;

		}

		return true;

	}

	private void playerManipulate( Player player, String[] args, boolean addPlayer, WorldData worldData ) {

		if ( args.length != 2 ) {
			player.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			player.sendMessage( ChatColor.YELLOW + " /whitelist add [player]" );
			return;
		}

		Callback<Callable<Void>, UUID> uuidCallback = param -> () -> {
			if ( param == null ) {
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
			} else {

				if ( addPlayer ) {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" has been added to the whitelist!" );
					worldData.getWhiteListedUsers().add( param );
				} else {
					player.sendMessage( ChatColor.BLUE + "UUID \"" + param + "\" has been removed to the whitelist!" );
					worldData.getWhiteListedUsers().remove( param );
				}

				core.getPlayerHandler().save( worldData );

			}
			return null;
		};

		player.sendMessage( ChatColor.YELLOW + "Processing..." );
		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {

			try ( Jedis jedis = core.getJedis() ) {

				UUID uuid = core.getUUIDManager().getUuid( jedis, args[1] );
				core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), uuidCallback.call( uuid ) );

			} catch ( ProcessingException | NoJedisException ex ) {
				ex.printStackTrace();
				player.sendMessage( ChatColor.RED + "An error occurred whilst getting the player's UUID..." );
				player.sendMessage( ChatColor.RED + "Error:  " + ex.getMessage() );
			}

		} );


	}

	private void toggleWhitelist( Player sender, boolean whitelistMode, WorldData worldData ) {

		worldData.setWhiteListed( whitelistMode );
		core.getPlayerHandler().save( worldData );

		sender.sendMessage( ChatColor.BLUE + "White listing is now " + (whitelistMode ? "enabled" : "disabled") + "." );

	}

	private WorldData getWorldData( World world ) {

		UUID uuid = WorldDimension.getUUIDOfWorld( world );
		if ( uuid == null ) return null;
		return core.getPlayerHandler().getWorldData( world );

	}

	@Override
	public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args ) {
		return null;
	}

	private void sendHelp( CommandSender sender ) {

		String cmd = ChatColor.BOLD.toString() + ChatColor.YELLOW;
		String inf = ChatColor.BLUE.toString();

		sender.sendMessage( ChatColor.BOLD.toString() + ChatColor.BLUE + "Whitelisting is a great feature to restrict access to the server for a specific set of players." );
		sender.sendMessage( cmd + "/whitelist add [player]" );
		sender.sendMessage( inf + " - Add the specified player to the white list." );
		sender.sendMessage( cmd + "/whitelist del [player]" );
		sender.sendMessage( inf + " - Remove the specified player from the white list." );
		sender.sendMessage( cmd + "/whitelist on" );
		sender.sendMessage( inf + " - Turn on white listing." );
		sender.sendMessage( cmd + "/whitelist off" );
		sender.sendMessage( inf + " - Turn off white listing." );
		sender.sendMessage( cmd + "/whitelist toggle" );
		sender.sendMessage( inf + " - Toggle on/off white listing." );

	}

}
