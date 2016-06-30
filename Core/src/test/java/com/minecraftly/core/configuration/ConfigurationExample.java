/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.configuration;

import com.minecraftly.core.MinecraftlyCore;
import com.minecraftly.core.event.MCLYEvent;
import com.minecraftly.core.runnables.RunnableData;
import org.junit.Test;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class ConfigurationExample {

	@Test
	public void doConfigrationExampleJson() {

		MinecraftlyCore<ConfigurationExample> core = new MinecraftlyCore<ConfigurationExample>( Logger.getGlobal(), new File( "." ), this, 5555 ) {
			@Override
			public void shutdown() {
				throw new IllegalStateException();
			}

			@Override
			public <T extends MCLYEvent> T callEvent( T event ) {
				throw new IllegalStateException();
			}

			@Override
			public int getMaxPlayers() {
				throw new IllegalStateException();
			}

			@Override
			public int getPlayerCount() {
				throw new IllegalStateException();
			}

			@Override
			public void runTask( RunnableData runnable ) {
				throw new IllegalStateException();
			}
		};

		/*RedisConfiguration redisConfiguration = new RedisConfiguration();
		redisConfiguration.setIp( "redis.ip.address" );
		redisConfiguration.setPassword( "Password123" );
		redisConfiguration.setMaxNumPools( 5 );
		redisConfiguration.setPort( 555 );
		redisConfiguration.setTimeOut( 30 );

		MinecraftlyConfiguration conf = MinecraftlyConfiguration.getDefaultConfiguration();
		conf.setMyAddress( new IPAddressConfiguration( "127.0.0.1", 555 ) );
		conf.setRedisConfig( redisConfiguration );
		conf.setDefaultActionIfNoServer( DefaultServerAction.OWN );
		conf.setDomainNameRegex( "^(\\w{1,16})\\.(.*)\\.(\\w{2,18})$" );*/

		MinecraftlyConfiguration conf = MinecraftlyConfiguration.load( new File( "config.json" ), core );

		System.out.println( conf.toString() );

		//conf.save( new File( "config.json" ), core );

	}

}
