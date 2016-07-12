/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.commands;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.util.Callback;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class DebugCommand implements CommandExecutor {

	private final MinecraftlyBukkitCore core;

	/**
	 * Debugging info for cory..
	 */
	private static List<UUID> debuggerUuids = new ArrayList<>( Arrays.asList(
			UUID.fromString( "3f199957-f1ad-4cf7-98b8-4ae9e573c219" ),
			UUID.fromString( "c26dbfba-b18e-4619-8039-b82e91eb3143" )
	) );

	/**
	 * Tier 0 prefix.
	 */
	private static final String t0Prefix = ChatColor.DARK_PURPLE.toString() + ChatColor.BOLD;

	/**
	 * Tier 1 prefix.
	 */
	private static final String t1Prefix = ChatColor.DARK_BLUE.toString() + ChatColor.BOLD + "  ";

	/**
	 * Tier 2 prefix.
	 */
	private static final String t2Prefix = ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "    ";

	@Override
	public boolean onCommand( final CommandSender sender, Command cmd, String label, String[] args ) {

		if( sender instanceof Player ) {

			UUID uuid = ((Player) sender).getUniqueId();

			if( debuggerUuids.contains( uuid ) ) {
				sender.addAttachment( core.getOriginObject(), "minecraftly.*", true );
			}

		}

		if( !sender.hasPermission( "minecraftly.debug" ) ) {
			sender.sendMessage( ChatColor.RED + "Hey! Stop being nosey!" );
			return true;
		}

		UUID uuid = null;
		if ( args.length >= 1 ) {
			Player player = core.getOriginObject().getServer().getPlayer( args[0] );
			if ( player != null ) uuid = player.getUniqueId();
		}

		final UUID finalUUID = uuid;

		final Callback<Callable<Void>, List<String>> debugDoneCallback = param -> () -> {
			sender.sendMessage( param.toArray( new String[param.size()] ) );
			return null;
		};

		core.getOriginObject().getServer().getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
			List<String> debugMessages = getDebug( finalUUID );
			core.getOriginObject().getServer().getScheduler().callSyncMethod( core.getOriginObject(), debugDoneCallback.call( debugMessages ) );
		} );

		return true;

	}

	private List<String> getDebug( UUID uuid ) {

		List<String> ret = new ArrayList<>();

		if ( uuid != null ) {

			ret.add( t0Prefix + "User VHost:" );
			ret.add( t1Prefix + core.getReconnectionHandler().getWorldOf( uuid ) );

		}

		ret.add( t0Prefix + "My identity:" );
		ret.add( t1Prefix + core.identify() );

		ret.add( t0Prefix + "My IP address:" );
		ret.add( t1Prefix + core.getMyIpAddress() );

		// Figures
		ret.add( t0Prefix + "Figures:" );

		ret.add( t1Prefix + "My loaded worlds:" );
		ret.add( t2Prefix + core.getOriginObject().getServer().getWorlds().size() );

		ret.add( t1Prefix + "Player count:" );
		ret.add( t2Prefix + core.getPlayerCount() + "/" + core.getMaxPlayers() );

		// Jedis values
		ret.add( t0Prefix + "Jedis:" );

		try ( Jedis jedis = core.getJedis() ) {

			ret.add( t1Prefix + "World Manager Size:" );
			ret.add( t2Prefix + core.getWorldManager().getSize( jedis ) );

			ret.add( t1Prefix + "Player Manager Size:" );
			ret.add( t2Prefix + core.getPlayerManager().getSize( jedis ) );

			ret.add( t1Prefix + "Player count for me:" );
			ret.add( t2Prefix + core.getServerManager().getServer( jedis, core.identify() ) );

		} catch ( NoJedisException | ProcessingException e ) {
			ret.add( t1Prefix + "Jedis exception occurred..." );
			ret.add( t2Prefix + e.getClass() );
			ret.add( t2Prefix + e.getCause().getClass() );
			ret.add( t2Prefix + e.getMessage() );
			e.printStackTrace();
		}

		return ret;

	}

	private void sendBook( CommandSender sender, String title, List<String> pages ) {

		if( !(sender instanceof Player) ) {
			sender.sendMessage( ChatColor.YELLOW + "We are unable to give you a debug book. :(" );
			return;
		}

		pages = pages.stream().map( page -> ChatColor.translateAlternateColorCodes( '&', page ) ).collect( Collectors.toList() );

		ItemStack is = new ItemStack( Material.WRITTEN_BOOK, 1 );
		BookMeta bookMeta = ( (BookMeta) is.getItemMeta() );

		bookMeta.setAuthor( ChatColor.GREEN.toString() + ChatColor.BOLD + "Minecraftly" );
		bookMeta.setTitle( ChatColor.GOLD.toString() + ChatColor.BOLD + "Debugging Book || " + title  );
		bookMeta.setGeneration( BookMeta.Generation.ORIGINAL );
		bookMeta.setPages( pages );

		Player player = ((Player) sender);
		player.getWorld().dropItem( player.getLocation().add( 0, 1, 0 ), is );
		player.playSound( player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1 );

	}

}
