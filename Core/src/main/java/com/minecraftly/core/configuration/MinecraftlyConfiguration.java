/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraftly.core.DefaultServerAction;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.configuration.gson.adapters.InetSocketAddressAdapter;
import lombok.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * The configuration handler for everything.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MinecraftlyConfiguration {

	@Getter
	private static Gson gson = getDefaultGsonBuilder().create();

	/**
	 * Redis configuration.
	 */
	private RedisConfiguration redisConfig = null;

	/**
	 * Allow overriding of the server address.
	 */
	private IPAddressConfiguration myAddress = null;

	/**
	 * Allows for a default action if there is no existing server.
	 */
	private DefaultServerAction defaultActionIfNoServer = null;

	/**
	 * Allows loading the configuration from a remote/static/shared location.
	 */
	private String configLocation = null;

	/**
	 * Runtime generated pattern from <link>MinecraftlyConfiguration#getDomainNamePattern()</link>
	 */
	@Setter( AccessLevel.PRIVATE )
	private transient Pattern domainNamePattern = null;

	/**
	 * Creates a default configuration.
	 *
	 * @return The default configuration.
	 */
	public static MinecraftlyConfiguration getDefaultConfiguration() {

		RedisConfiguration redisConfiguration = new RedisConfiguration();
		redisConfiguration.setPassword( "Password123" );
		redisConfiguration.setIp( "127.0.0.1" );
		redisConfiguration.setPort( 6379 );
		redisConfiguration.setMaxNumPools( 6 );
		redisConfiguration.setTimeOut( 100 );

		IPAddressConfiguration ipAddressConfiguration = new IPAddressConfiguration();
		ipAddressConfiguration.setIpAddress( "127.0.0.1" );
		ipAddressConfiguration.setPort( 25565 );

		MinecraftlyConfiguration configuration = new MinecraftlyConfiguration();
		configuration.setRedisConfig( redisConfiguration );
		configuration.setMyAddress( ipAddressConfiguration );
		configuration.setDefaultActionIfNoServer( DefaultServerAction.OWN );
		configuration.setConfigLocation( null );

		return configuration;

	}

	/**
	 * Load the configuration from a file.
	 *
	 * @param file The file to load from.
	 * @param core The core instance.
	 * @return The configuration loaded from the file.
	 */
	public static MinecraftlyConfiguration load( @NonNull File file, @NonNull MinecraftlyCore core ) {

		try {
			return load( MinecraftlyUtil.readText( file ), core );
		} catch ( FileNotFoundException e ) {

			/*
			 * Mkdirs makes the file a dir as well,
			 * and calling getParentFile sometimes may result in NPE.
			 * Dirty fix.. :/
			 */
			if ( !file.exists() ) {
				boolean made = file.mkdirs() && file.delete();
				if ( !made )
					core.getLogger().warning( "Something went wrong when we were creating the file, it may already exist.." );
			}


			MinecraftlyConfiguration defaultConfig = getDefaultConfiguration();
			try ( FileWriter fw = new FileWriter( file ) ) {
				fw.write( gson.toJson( getDefaultConfiguration() ) );
			} catch ( IOException e1 ) {
				core.getLogger().severe( "Unable to save default the configuration!" );
			}

			core.getLogger().warning( "Saved the default configuration, please edit this to your liking and restart the server!" );

			return defaultConfig;

		} catch ( IOException e1 ) {
			core.getLogger().severe( "Unable to save/load the configuration!" );
			throw new RuntimeException( "Error making configuration!", e1 );
		}

	}

	/**
	 * Load the configuration from a file.
	 *
	 * @param json The json string.
	 * @param core The core instance.
	 * @return A new instance of {@link MinecraftlyConfiguration}
	 */
	public static MinecraftlyConfiguration load( @NonNull String json, @NonNull MinecraftlyCore core ) {

		// TODO yaml loading.

		// Make sure any extra parts are being parsed and loaded.
		gson = core.processGsonBuilder( getDefaultGsonBuilder() ).create();

		MinecraftlyConfiguration configuration;

		// Load the configuration.
		core.getLogger().info( "Loading Minecraftly configuration." );
		configuration = gson.fromJson( json, MinecraftlyConfiguration.class );

		// Loads the config from a location, if one is specified.
		if ( configuration.getConfigLocation() != null && !configuration.getConfigLocation().isEmpty() ) {

			String nextUrl = configuration.getConfigLocation();
			int maxHops = 16;

			// Allow more than 15 hops, not recommended.
			try {
				String maxHopsString = System.getProperty( "minecraftly.configuration.MaxHops" );
				if ( maxHopsString != null ) maxHops = Integer.parseInt( maxHopsString ) + 1;
			} catch ( Exception ignored ) {
				// Ignored.
			}

			// Iterates through the locations until max hops or null location.
			for ( int hop = 1; hop < maxHops; hop++ ) {

				try {

					if ( nextUrl == null ) break;

					MinecraftlyConfiguration hopConfig = gson.fromJson( MinecraftlyUtil.downloadText( nextUrl.trim() ), MinecraftlyConfiguration.class );
					if ( hopConfig == null ) break;

					core.getLogger().info( "Minecraftly config [" + hop + "] loaded via \"" + nextUrl.trim() + "\"." );

					hopConfig.applyTo( configuration );
					nextUrl = hopConfig.getConfigLocation();

				} catch ( Exception e ) {
					core.getLogger().log( Level.WARNING, "Unable to load configuration [" + hop + "] via \"" + configuration.getConfigLocation() + "\".", e );
					nextUrl = null;
					break;
				}

			}

			if ( nextUrl != null )
				core.getLogger().log( Level.WARNING, "There was more configurations to load but the max hops was reached!" );

		} else {
			core.getLogger().info( "Minecraftly configuration loaded!" );
		}

		return configuration;

	}

	/**
	 * The default GSON builder..
	 */
	private static GsonBuilder getDefaultGsonBuilder() {
		return new GsonBuilder().setPrettyPrinting().registerTypeAdapter( InetSocketAddress.class, InetSocketAddressAdapter.INSTANCE );
	}

	/**
	 * Apply this instance to the masterConfig.
	 *
	 * @param masterConfig The upper configuration to be applied to.
	 */
	public void applyTo( MinecraftlyConfiguration masterConfig ) {

		// Is there really not a better way to do this?

		// Apply the address configuration.
		if ( getMyAddress() != null ) {

			IPAddressConfiguration ipAddressConfiguration = masterConfig.getMyAddress();
			if ( ipAddressConfiguration == null )
				masterConfig.setMyAddress( ipAddressConfiguration = new IPAddressConfiguration() );

			if ( ipAddressConfiguration.getIpAddress() == null || ipAddressConfiguration.getIpAddress().isEmpty() )
				ipAddressConfiguration.setIpAddress( getMyAddress().getIpAddress() );

			if ( ipAddressConfiguration.getPort() <= 0 )
				ipAddressConfiguration.setPort( getMyAddress().getPort() );

		}

		// Apply the redis configuration.
		if ( getRedisConfig() != null ) {

			RedisConfiguration redisConfiguration = masterConfig.getRedisConfig();
			if ( redisConfiguration == null )
				masterConfig.setRedisConfig( redisConfiguration = new RedisConfiguration() );

			if ( redisConfiguration.getIp() == null || redisConfiguration.getIp().isEmpty() )
				redisConfiguration.setIp( getRedisConfig().getIp() );

			if ( redisConfiguration.getPassword() == null )
				redisConfiguration.setPassword( getRedisConfig().getPassword() );

			if ( redisConfiguration.getPort() <= 0 )
				redisConfiguration.setPort( getRedisConfig().getPort() );

			if ( redisConfiguration.getTimeOut() <= 0 )
				redisConfiguration.setTimeOut( getRedisConfig().getTimeOut() );

			if ( redisConfiguration.getMaxNumPools() <= 0 )
				redisConfiguration.setMaxNumPools( getRedisConfig().getMaxNumPools() );

		}

		// Misc applications.
		if ( masterConfig.getDefaultActionIfNoServer() == null )
			masterConfig.setDefaultActionIfNoServer( getDefaultActionIfNoServer() );

	}

	/**
	 * Save the configuration to a file.
	 *
	 * @param file The file of which to save to.
	 * @param core The core instance.
	 */
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public void save( @NonNull File file, @NonNull MinecraftlyCore core ) {

		try {

			/*
			 * Mkdirs makes the file a dir as well,
			 * and calling getParentFile sometimes may result in NPE.
			 * Dirty fix.. :/
			 */
			file.mkdirs();
			file.delete();
			file.createNewFile();

			try ( FileWriter fw = new FileWriter( file ) ) {
				fw.write( gson.toJson( this ) );
				fw.flush();
			}

		} catch ( IOException e1 ) {
			core.getLogger().severe( "Unable to save the default configuration!" );
			throw new RuntimeException( "Error making default configuration!", e1 );
		}

	}

}
