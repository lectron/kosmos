/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee.commands.tpa;

import com.google.common.collect.ImmutableSet;
import com.minecraftly.bungee.MinecraftlyBungeeCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class TpaCommand extends Command implements TabExecutor {

	private final MinecraftlyBungeeCore core;

	public TpaCommand( MinecraftlyBungeeCore core ) {
		super( "tpa", null, "tpr", "tprequest", "call", "tpask" );
		this.core = core;
	}

	public static void sendTpaRequest( ProxiedPlayer receiver, UUID uuid, String name ) {

		HoverEvent uuidHover = new HoverEvent( HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
				new TextComponent( "UUID: " + uuid )
		} );

		TextComponent playerTextComponent = new TextComponent( name );
		playerTextComponent.setColor( ChatColor.AQUA );
		playerTextComponent.setBold( true );
		playerTextComponent.setHoverEvent( uuidHover );

		TextComponent tpaTextComponent = new TextComponent( " has requested to teleport to you." );
		tpaTextComponent.setColor( ChatColor.GOLD );

		receiver.sendMessage( playerTextComponent, tpaTextComponent );
		receiver.sendMessage( ChatColor.GOLD + "Type /tpaccept to accept or /tpdeny to decline." );

	}

	@Override
	public void execute( CommandSender sender, String[] args ) {

		if ( !(sender instanceof ProxiedPlayer) ) {
			sender.sendMessage( ChatColor.RED + "Only players can tpa." );
			return;
		}

		if ( args.length != 1 ) {
			sender.sendMessage( ChatColor.RED + "Hey, that isn't how you do this.." );
			sender.sendMessage( ChatColor.YELLOW + " /tpa <playername>" );
			return;
		}

		final String playerName = args[0].trim();
		final ProxiedPlayer player = ((ProxiedPlayer) sender);

		core.getOriginObject().getProxy().getScheduler().runAsync( core.getOriginObject(), () -> {

			UUID uuidToJoin = null;

			try ( Jedis jedis = core.getJedis() ) {

				try {
					int size = core.getTransportManager().getAllForRequester( jedis, player.getUniqueId() ).size();
					if ( size > 10 ) {
						player.sendMessage( ChatColor.RED + "Woah! Calm down.. There's no need to visit that many people." );
						return;
					}
				} catch ( Exception ignored ) {
				}

				// If the name isn't valid, maybe it's a UUID?
				boolean uuidSet = false;
				try {
					if ( playerName.length() > 16 ) {
						uuidToJoin = MinecraftlyUtil.convertFromNoDashes( playerName );
						uuidSet = true;
					}
				} catch ( IllegalArgumentException ignored ) {
				}

				// Get the UUID from the name if it exists.
				try {
					if ( !uuidSet && core.getUUIDManager().hasUuid( jedis, playerName ) ) {
						uuidToJoin = core.getUUIDManager().getUuid( jedis, playerName );
					}
				} catch ( ProcessingException e ) {
					core.getLogger().log( Level.SEVERE, "Error getting the uuid for \"" + playerName + "\".", e );
				}

				// If the player is on our proxy tpa to them from here.
				ProxiedPlayer receiver;
				if ( (receiver = core.getOriginObject().getProxy().getPlayer( uuidToJoin )) != null ) {

					sendTpaRequest( receiver, player.getUniqueId(), player.getName() );
					player.sendMessage( ChatColor.BLUE + "Your teleport request has been sent!" );

					try {
						core.getTransportManager().setRequester( jedis, receiver.getUniqueId(), player.getUniqueId() );
					} catch ( ProcessingException e ) {
						core.getLogger().log( Level.SEVERE, "Error setting the tp request for \"" + player.getUniqueId() + "\".", e );
					}

					return;

				}

				// Send a tpa globally if they're not on our proxy.
				try {
					if ( core.getPlayerManager().hasServer( jedis, uuidToJoin ) ) {
						jedis.publish( RedisKeys.TRANSPORT.toString(), "TPA\00" + player.getUniqueId() + "\00" + player.getName() + "\00" + uuidToJoin );
						core.getTransportManager().setRequester( jedis, uuidToJoin, player.getUniqueId() );
					}
				} catch ( ProcessingException e ) {
					core.getLogger().log( Level.SEVERE, "Error getting the server for \"" + uuidToJoin + "\".", e );
				}

			} catch ( NoJedisException e ) {
				core.getLogger().log( Level.SEVERE, "There was an error fetching jedis!", e );
			}

			player.sendMessages( ChatColor.RED + "We were unable to find an online player by that name." );

		} );

	}

	@Override
	public Iterable<String> onTabComplete( CommandSender sender, String[] args ) {

		if ( args.length != 0 ) {
			return ImmutableSet.of();
		}

		String search = args[0].toLowerCase();
		return ProxyServer.getInstance().getPlayers().stream()
				.filter( player -> player.getName().toLowerCase().startsWith( search ) )
				.map( ProxiedPlayer::getName )
				.collect( Collectors.toList() );

	}
}
