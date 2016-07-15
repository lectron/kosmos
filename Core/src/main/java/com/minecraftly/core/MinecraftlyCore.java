/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

import com.google.gson.GsonBuilder;
import com.minecraftly.core.configuration.IPAddressConfiguration;
import com.minecraftly.core.configuration.MinecraftlyConfiguration;
import com.minecraftly.core.configuration.RedisConfiguration;
import com.minecraftly.core.debugger.DebuggerEngine;
import com.minecraftly.core.event.MCLYEvent;
import com.minecraftly.core.event.events.LoadCompleteEvent;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import com.minecraftly.core.manager.messagelistener.RedisMessageListener;
import com.minecraftly.core.manager.redis.PlayerManager;
import com.minecraftly.core.manager.redis.ServerManager;
import com.minecraftly.core.manager.redis.UUIDManager;
import com.minecraftly.core.manager.redis.WorldManager;
import com.minecraftly.core.runnables.HeartbeatTask;
import com.minecraftly.core.runnables.RunnableData;
import com.minecraftly.core.runnables.SubscribeTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public abstract class MinecraftlyCore<P> implements Closeable {

	private static MinecraftlyCore core = null;

	/**
	 * The java logger that will be used for Minecraftly.
	 * Usually Plugin#getLogger();
	 */
	@Getter
	@NonNull
	private final Logger logger;

	/**
	 * The datafolder from Plugin#getDataFolder();
	 */
	@Getter
	@NonNull
	private final File minecraftlyDataFolder;

	/**
	 * The plugin instance of the minecraftly plugin.
	 */
	@Getter
	@NonNull
	private final P originObject;

	/**
	 * The ipaddress of the machine this is called on.
	 */
	@Getter
	@NonNull
	private final String myIpAddress;

	/**
	 * The port of this instance. IP+Port should be unique.
	 */
	@Getter
	private final int port;
	/**
	 * A sharable messageListener.
	 */
	@Getter
	private final RedisMessageListener messageListener = new RedisMessageListener( this );
	/**
	 * The debugger engine.
	 */
	@Getter
	private final DebuggerEngine<P> debugger = new DebuggerEngine<>( this );
	/**
	 * The manager of the world&lt;-&gt;server ownership.
	 */
	@Getter
	private WorldManager worldManager;
	/**
	 * The manager of the player-world-server ownership.
	 */
	@Getter
	private ServerManager serverManager;
	/**
	 * The manager of player&lt;-&gt;uuid relationships.
	 */
	@Getter
	private UUIDManager UUIDManager;

	/**
	 * The manager of players and their servers.
	 */
	@Getter
	private PlayerManager playerManager;

	/**
	 * The configuration for the server.
	 */
	@Getter
	@Setter
	private MinecraftlyConfiguration config = null;

	/**
	 * The jedispool for redis comms.
	 */
	@Getter
	private JedisPool jedisPool = null;

	/**
	 * The contained serverType.
	 */
	private ServerType serverType = null;

	/**
	 * The heartbeat task.
	 */
	private HeartbeatTask heartBeatTask;

	public MinecraftlyCore( @NonNull Logger parentLogger, @NonNull File minecraftlyDataFolder, @NonNull P originObject, int port ) {

		this.logger = new MinecraftlyLogger( this, parentLogger );
		this.minecraftlyDataFolder = minecraftlyDataFolder;
		this.originObject = originObject;
		this.port = port;

		// Get the IP address for this machine.
		logger.log( Level.FINE, "Getting my IP address..." );
		String myIpAddress;
		try {
			myIpAddress = MinecraftlyUtil.downloadText( "http://ipinfo.io/ip" );
			if ( myIpAddress != null ) myIpAddress = myIpAddress.trim();
			logger.log( Level.INFO, "My IP address: " + myIpAddress );
		} catch ( IOException e ) {
			myIpAddress = "";
			logger.log( Level.WARNING, "Unable to get my IP Address via ipinfo.io! Our identity may be corrupt!", e );
		}
		this.myIpAddress = myIpAddress;

		// Static access to core.
		core = this;

		logger.log( Level.FINE, "Core initialised. Waiting for load.." );

	}

	public MinecraftlyCore( Logger logger, File minecraftlyDataFolder, P originObject, int port, @NonNull MinecraftlyConfiguration config ) {
		this( logger, minecraftlyDataFolder, originObject, port );
		this.config = config;
	}

	/**
	 * Static access because yeah...
	 *
	 * @return The core instance.
	 * @throws IllegalStateException thrown when core == null.
	 */
	public static MinecraftlyCore getCore() {
		if ( core == null )
			throw new IllegalStateException( "MinecraftlyCore is being accessed before it's initialised..?" );
		return core;
	}

	/**
	 * Load up everything.
	 *
	 * @throws Exception
	 */
	public final void load() throws Exception {

		logger.log( Level.INFO, "Loading Minecraftly Core..." );

		// Configuration loading.
		if ( config == null ) {
			config = MinecraftlyConfiguration.load( new File( minecraftlyDataFolder, "config.json" ), this );
		}

		// Jedis loading and pool configuration.
		logger.log( Level.INFO, "Loading redis..." );
		RedisConfiguration redisConfig = config.getRedisConfig();

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setJmxEnabled( false );
		poolConfig.setMaxTotal( redisConfig.getMaxNumPools() );
		poolConfig.setMinIdle( (int) MinecraftlyUtil.round( redisConfig.getMaxNumPools() / 4, 1 ) );
		poolConfig.setBlockWhenExhausted( false );
		poolConfig.setTestOnBorrow( true );

		if ( jedisPool == null ) {
			if ( redisConfig.getPassword() == null || redisConfig.getPassword().isEmpty() ) {
				jedisPool = new JedisPool( poolConfig, redisConfig.getIp(), redisConfig.getPort(), redisConfig.getTimeOut() );
			} else {
				jedisPool = new JedisPool( poolConfig, redisConfig.getIp(), redisConfig.getPort(), redisConfig.getTimeOut(), redisConfig.getPassword() );
			}
		}
		logger.log( Level.INFO, "Redis loaded!" );

		// Create the core managers.
		this.worldManager = new WorldManager( this );
		this.serverManager = new ServerManager( this );
		this.UUIDManager = new UUIDManager( this );
		this.playerManager = new PlayerManager( this );

		// Run the tasks.
		logger.log( Level.INFO, "Starting tasks.." );

		this.heartBeatTask = new HeartbeatTask( this );
		runTask( heartBeatTask );
		runTask( new SubscribeTask( this ) );

		logger.log( Level.INFO, "Tasks started!" );

		// Broadcast a successful load.
		callEvent( new LoadCompleteEvent() );

		logger.log( Level.INFO, "Minecraftly Core loaded!" );

	}

	/**
	 * Sends a message to the desired key.
	 *
	 * @param key     The key to send the message to.
	 * @param message The message to send.
	 * @throws ProcessingException occurs when an execption happens.
	 * @throws NoJedisException    Jedis is unreachable or closed.
	 */
	public final void sendMessage( RedisKeys key, String message ) throws ProcessingException, NoJedisException {

		message = message.replace( "{identity}", identify() ).replace( "{myip}", getMyIpAddress() ).replace( "{port}", String.valueOf( getPort() ) );

		try ( Jedis jedis = core.getJedis() ) {
			jedis.publish( key.toString(), message );
		} catch ( NoJedisException ex ) {
			throw ex;
		} catch ( Exception ex ) {
			throw new ProcessingException( "There was an error sending a message to " + key + ".", ex );
		}

	}

	/**
	 * Gets jedis or throws nojedisexception if getJedis isn't possible.
	 *
	 * @return A jedis connection.
	 * @throws NoJedisException
	 */
	public final Jedis getJedis() throws NoJedisException {
		if ( core == null || core.getJedisPool() == null || core.getJedisPool().isClosed() )
			throw new NoJedisException();
		return core.getJedisPool().getResource();
	}

	/**
	 * Shut down everything including redis comms.
	 *
	 * @throws IOException
	 */
	@Override
	public final void close() throws IOException {

		logger.log( Level.INFO, "Minecraftly shutting down.." );

		// We don't have much of a choice but to kick everyone here.
		ServerType serverType = getServerType();
		logger.log( Level.FINE, "1 ServerType: " + serverType );
		if ( serverType == ServerType.BUNGEE || serverType == ServerType.CONTROLLER ) {

			logger.log( Level.FINE, "2 Unsubscribing." );
			getMessageListener().unsubscribe();

			logger.log( Level.FINE, "3 Closing jedis." );
			if ( jedisPool != null ) {
				if ( !jedisPool.isClosed() ) jedisPool.destroy();
				jedisPool = null;
			}

			core = null;

			return;
		}

		/*
		 * Here we're going to wait and see if we can move the players before the server dies.
		 * If after 30 seconds they're not moved gracefully, they'll be kicked.
		 */
		logger.log( Level.FINE, "Broadcasting death." );
		try ( Jedis jedis = getJedis() ) {
			jedis.publish( RedisKeys.IDENTIFY.toString(), "SERVER\000" + identify() + "\000DYING" );
		} catch ( NoJedisException e ) {
			getLogger().log( Level.SEVERE, "Error letting the network know we're dying...", e );
		}

		/*
		 * Ideally close would be called when a task is stopped but hey ho.
		 */
		if ( heartBeatTask != null )
			heartBeatTask.close();

		/*
		 * TODO:
		 * Enable this;
		 * Delay shutting down of the server either by ASM or maybe AspectJ.
		 *
		runTask( new CloseTask( () -> {
			close1();
			return null;
		} ) );
		*/

		logger.log( Level.FINE, "Close call completed, waiting for close1." );


		close1();

	}

	/**
	 * Internal close called either when close time expired or close message received.
	 *
	 * @see MinecraftlyCore#close()
	 */
	protected final void close1() {

		getLogger().log( Level.INFO, "Shutting down..." );
		shutdown();

		try ( Jedis jedis = getJedis() ) {

			if ( worldManager != null ) {
				getLogger().log( Level.INFO, "Closing the world manager." );
				try {
					worldManager.removeAll( jedis, identify() );
				} catch ( ProcessingException e ) {
					getLogger().log( Level.SEVERE, "Error removing the worlds belonging to me?", e );
				}
			}

			if ( serverManager != null ) {
				getLogger().log( Level.INFO, "Closing the server manager." );
				try {
					serverManager.removeAll( jedis, identify() );
				} catch ( ProcessingException e ) {
					getLogger().log( Level.SEVERE, "Error removing myself?", e );
				}
			}

		} catch ( NoJedisException e ) {
			e.printStackTrace();
		}

		logger.log( Level.FINE, "Unsubscribing the Jedis message listener.." );
		getMessageListener().unsubscribe();

		logger.log( Level.INFO, "Closing jedis.." );
		if ( jedisPool != null ) {
			jedisPool.destroy();
			jedisPool = null;
		}

		core = null;
		logger.log( Level.INFO, "Minecraftly core closed!" );

	}

	public abstract void shutdown();

	/**
	 * A method to clearly identify the server on the network/cloud..
	 *
	 * @return A unique string per server. Possibly IP:Port.
	 */
	public String identify() {

		int port = getPort();
		IPAddressConfiguration ipAddress = getConfig().getMyAddress();

		if ( ipAddress != null ) {

			if ( ipAddress.getPort() > 0 ) port = ipAddress.getPort();

			if ( ipAddress.getIpAddress() != null && !ipAddress.getIpAddress().trim().isEmpty() ) {
				return ipAddress.getIpAddress().trim() + ":" + port;
			}

		}

		return getMyIpAddress() + ":" + port;

	}

	/**
	 * Should call the event and allow all the handlers to handle the event.
	 *
	 * @param event the event to be called.
	 * @param <T>   the wrapped event, must extend MCLYEvent
	 * @return the called event.
	 */
	public abstract <T extends MCLYEvent> T callEvent( @NonNull T event );

	/**
	 * Allows identification of what the type of server is.. Proxy or slave.
	 *
	 * @return The server type or unknown.
	 */
	public ServerType getServerType() {

		if ( serverType != null ) return serverType;

		ServerType ret = ServerType.UNKNOWN;

		boolean bukkit = false;
		boolean bungee = false;

		try {
			Class.forName( "org.bukkit.Bukkit" );
			bukkit = true;
			ret = ServerType.BUKKIT;
		} catch ( ClassNotFoundException ignored ) {
		}

		try {
			Class.forName( "net.md_5.bungee.api.ProxyServer" );
			bungee = true;
			ret = ServerType.BUNGEE;
		} catch ( ClassNotFoundException ignored ) {
		}

		if ( bukkit && bungee || !bungee && !bukkit ) {
			getLogger().warning( "Some weirdness is going on? Is this a bungee/bukkit hybrid?! I don't know what I am!!" );
			ret = ServerType.UNKNOWN;
		}

		return serverType = ret;

	}

	public abstract int getMaxPlayers();

	public abstract int getPlayerCount();

	public abstract void runTask( @NonNull RunnableData runnable );

	public void playerJoined( @NonNull UUID uniqueId ) {

	}

	public void playerExited( @NonNull UUID uniqueId ) {

	}

	public GsonBuilder processGsonBuilder( @NonNull GsonBuilder gsonBuilder ) {
		return gsonBuilder;
	}

	public enum ServerType {

		BUKKIT,
		BUNGEE,
		UNKNOWN,

		/**
		 * For potential future use.
		 */
		CONTROLLER

	}

}
