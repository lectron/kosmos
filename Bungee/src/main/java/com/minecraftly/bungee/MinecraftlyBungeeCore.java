/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bungee;

import com.minecraftly.bungee.listeners.MessageListener;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.RedisKeys;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.runnables.RunnableData;
import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import net.md_5.bungee.protocol.packet.Handshake;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MinecraftlyBungeeCore extends MinecraftlyCore<MinecraftlyBungeePlugin> {

	public MinecraftlyBungeeCore( @NonNull MinecraftlyBungeePlugin originObject ) {
		super( originObject.getLogger(), originObject.getDataFolder(), originObject, 0 );

		getEventBus().register( new MessageListener( this ) );

	}

	@Override
	public void shutdown() {
		// TODO
	}

	@Override
	public int getMaxPlayers() {
		return getOriginObject().getProxy().getConfig().getPlayerLimit();
	}

	@Override
	public int getPlayerCount() {
		return getOriginObject().getProxy().getOnlineCount();
	}

	@Override
	public void runTask( RunnableData runnable ) {

		runnable.check();

		TaskScheduler scheduler = getOriginObject().getProxy().getScheduler();
		Collection<RunnableData.TaskType> types = runnable.getTaskTypes();

		if ( types.contains( RunnableData.TaskType.DELAYED ) ) {
			scheduler.schedule( getOriginObject(), runnable, runnable.getStartTime(), runnable.getTimeUnit() );
		} else if ( types.contains( RunnableData.TaskType.REPEATING ) ) {
			scheduler.schedule( getOriginObject(), runnable, runnable.getStartTime(), runnable.getRepeatTime(), runnable.getTimeUnit() );
		} else if ( runnable.getTaskTypes().size() == 1 && (types.contains( RunnableData.TaskType.ASYNC ) || types.contains( RunnableData.TaskType.SYNC )) ) {
			scheduler.runAsync( getOriginObject(), runnable );
		} else {
			getLogger().log( Level.WARNING, "There was an error scheduling task of class \"" + runnable.getClass() + "\"." );
		}

	}

	@Override
	public boolean sendToServer( UUID playerUuid, UUID server, boolean messageDownstream ) {

		ProxiedPlayer player = getOriginObject().getProxy().getPlayer( playerUuid );
		if( player == null ) return false;

		String serverId;

		try (Jedis jedis = getJedis() ) {

			boolean hasServer = getWorldManager().hasServer( jedis, server );
			if( hasServer ) {
				serverId = getWorldManager().getServer( jedis, server );
			} else {
				serverId = getWorldManager().loadWorld( jedis, server );
			}

			Handshake hs = ReflectionUtil.getHandshake( player.getPendingConnection() );
			if( hs == null ) return false;

			hs.setHost( server.toString() + ".m.ly" );

			if( serverId == null ) {
				player.sendMessage( ChatColor.RED + "We were unable to send you to the specified server." );
				return false;
			}
			ServerInfo si = ProxyServer.getInstance().constructServerInfo( serverId, MinecraftlyUtil.parseAddress( serverId ), "", false );

			if( player.getServer().getInfo().equals( si ) ) {

				if( !messageDownstream )
					return true;

				jedis.publish( RedisKeys.TRANSPORT.toString(), "SEND\00" + playerUuid.toString() + "\00" + server.toString() );

			} else {
				player.connect( si );
			}

		} catch ( NoJedisException | ProcessingException e ) {
			e.printStackTrace();
			player.sendMessage( ChatColor.RED + "We were unable to send you to the specified server." );
			return false;
		}

		return false;

	}

	public static final class ReflectionUtil {

		private static Field handshakeField = null;
		private static Class initialHandlerClass = null;

		public static Handshake getHandshake( PendingConnection pendingConnection ) {
			try {
				return (Handshake) getHandshakeField( pendingConnection ).get( pendingConnection );
			} catch ( ReflectiveOperationException ex ) {
				return null;
			}
		}

		public static void setHandshake( PendingConnection pendingConnection, Handshake handshake ) {
			try {
				getHandshakeField( pendingConnection ).set( pendingConnection, handshake );
			} catch ( ReflectiveOperationException ignored ) {
			}
		}

		public static Field getHandshakeField( PendingConnection pendingConnection ) throws ReflectiveOperationException {

			Class handlerClass = getInitialHandlerClass( pendingConnection );

			if( handshakeField == null ) {
				handshakeField = handlerClass.getDeclaredField( "handshake" );
				handshakeField.setAccessible( true );
			}

			return handshakeField;

		}

		public static Class getInitialHandlerClass( PendingConnection pendingConnection ) throws ReflectiveOperationException {

			if( !"net.md_5.bungee.connection.InitialHandler".equals( pendingConnection.getClass().getName() ) ) {
				throw new IllegalArgumentException( "Provided connection isn't an instance of InitialHandler." );
			} else if( initialHandlerClass == null ) {
				initialHandlerClass = Class.forName( "net.md_5.bungee.connection.InitialHandler" );
			}

			return initialHandlerClass;

		}

	}

}
