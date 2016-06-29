/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minecraftly.core.DefaultServerAction;
import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.configuration.gson.adapters.InetSocketAddressAdapter;
import lombok.*;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * The configuration handler for everything.
 *
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
public class MinecraftlyConfiguration {

	@Getter
	private static Gson gson = getDefaultGsonBuilder().create();

	/**
	 * Redis configuration.
	 */
	@NonNull
	private MinecraftlyRedisConfiguration redisConfig;

	/**
	 * Allow overriding of the server address.
	 */
	private IPAddressConfiguration myAddress = null;

	/**
	 * The regex for matching the username in the domain name.
	 */
	private String domainNameRegex = "^(\\w{1,16})\\.(.*)\\.(\\w{2,18})$";

	/**
	 * Allows for a default action if there is no existing server.
	 */
	private DefaultServerAction defaultActionIfNoServer = DefaultServerAction.OWN;

	/**
	 * Allows loading the configuration from a remote/static/shared location.
	 */
	private String configLocation = null;

	/**
	 * Runtime generated pattern from <link>MinecraftlyConfiguration#getDomainNamePattern()</link>
	 */
	private transient Pattern domainNamePattern = null;

	/**
	 * Creates a default configuration.
	 *
	 * @return The default configuration.
	 */
	public static MinecraftlyConfiguration getDefaultConfiguration() {

		MinecraftlyRedisConfiguration redisConfiguration = new MinecraftlyRedisConfiguration();
		redisConfiguration.setPassword( "Password123" );

		MinecraftlyConfiguration configuration = new MinecraftlyConfiguration();
		configuration.setRedisConfig( redisConfiguration );

		return configuration;

	}

	/**
	 * Load the configuration from a file.
	 *
	 * @param file The file of which to load.
	 * @param core The core instance.
	 * @return A new instance of {@link MinecraftlyConfiguration}
	 */
	@SuppressWarnings( "ResultOfMethodCallIgnored" )
	public static MinecraftlyConfiguration load( @NonNull File file, @NonNull MinecraftlyCore core ) {

		// TODO yaml loading.

		// Make sure any extra parts are being parsed and loaded.
		gson = core.processGsonBuilder( getDefaultGsonBuilder() ).create();

		MinecraftlyConfiguration configuration;

		// Load the configuration.
		try {
			core.getLogger().info( "Loading Minecraftly configuration." );
			configuration = gson.fromJson( new Scanner( file ).useDelimiter( "\\A" ).next(), MinecraftlyConfiguration.class );
		} catch ( FileNotFoundException e ) {

			// Create it if it doesn't exist.
			try {

				/*
				 * Mkdirs makes the file a dir as well,
				 * and calling getParentFile sometimes may result in NPE.
				 * Dirty fix.. :/
				 */
				file.mkdirs();
				file.delete();
				file.createNewFile();

				configuration = getDefaultConfiguration();

				try ( FileWriter fw = new FileWriter( file ) ) {
					fw.write( gson.toJson( configuration ) );
				}

				core.getLogger().warning( "Saved the default configuration, please edit this to your liking and restart the server!" );

			} catch ( IOException e1 ) {
				ExceptionUtils.setCause( e1, e );
				core.getLogger().severe( "Unable to save the default configuration!" );
				throw new RuntimeException( "Error making default configuration!", e1 );
			}

		}

		// TODO recurse 15 hops.
		// Loads the config from a location, if one is specified.
		if ( configuration.getConfigLocation() != null && !configuration.getConfigLocation().isEmpty() ) {

			try {

				String json = new Scanner( new URL( configuration.getConfigLocation() ).openStream() ).useDelimiter( "\\A" ).next();
				configuration = gson.fromJson( json, MinecraftlyConfiguration.class );

				core.getLogger().info( "Minecraftly configuration loaded via \"" + configuration.getConfigLocation() + "\"." );

			} catch ( IOException e ) {
				core.getLogger().warning( "Unable to load configuration via \"" + configuration.getConfigLocation() + "\"." );
				throw new RuntimeException( "Unable to load configuration via URL..", e );
			}

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
	 * Runtime generates a pattern from <link>MinecraftlyConfiguration#getDomainNamePattern()</link>
	 *
	 * @return the pattern to match domain names against.
	 */
	public Pattern getDomainNamePattern() {

		if ( domainNamePattern == null && getDomainNameRegex() != null ) {
			domainNamePattern = Pattern.compile( getDomainNameRegex(), Pattern.CASE_INSENSITIVE );
		}

		return domainNamePattern;

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
