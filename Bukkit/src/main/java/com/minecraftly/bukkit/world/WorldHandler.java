/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.exceptions.WorldDoesNotExistException;
import com.minecraftly.core.manager.exceptions.NoJedisException;
import com.minecraftly.core.manager.exceptions.ProcessingException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.UUID;

/**
 * Handle loading of worlds.
 *
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@RequiredArgsConstructor
public class WorldHandler {

	private final MinecraftlyBukkitCore core;

	/**
	 * Load the world specified via the arguments.
	 *
	 * @param worldName   The name of the owner of the world.
	 * @param environment The type of world.
	 * @return The world that's loaded.
	 */
	public World loadWorld( @NonNull String worldName, @NonNull World.Environment environment ) {
		try {
			return loadWorld( null, worldName, environment );
		} catch ( WorldDoesNotExistException e ) {
			throw new RuntimeException( "null loader resulted in WorldDoesNotExist?! Impossible.", e );
		}
	}

	/**
	 * Load the world specified via the arguments.
	 *
	 * @param loader      The UUID of whom is requesting the world. Can be null.
	 * @param worldName   The name of the owner of the world.
	 * @param environment The type of world.
	 * @return The world that's loaded.
	 * @throws WorldDoesNotExistException Thrown if the world does not already exist and the loader isn't allowed to create it.
	 */
	public World loadWorld( UUID loader, @NonNull String worldName, @NonNull World.Environment environment ) throws WorldDoesNotExistException {

		// Check there's not an existing world already loaded.
		World existingWorld = Bukkit.getWorld( worldName );
		if ( existingWorld == null ) {

			// Check if the world exists and if the loader is allowed to create the world.
			File worldDirectory = new File( Bukkit.getWorldContainer(), worldName );
			if ( !worldDirectory.exists() && loader != null ) {
				if ( loader.toString().equalsIgnoreCase( WorldDimension.getBaseWorldName( worldName ) ) )
					throw new WorldDoesNotExistException();
			}

			// This shouldn't happen, but we're prepared.
			if ( worldDirectory.exists() && !worldDirectory.isDirectory() ) {
				throw new IllegalArgumentException( worldDirectory.getPath() + " exists, but is not a directory." );
			}

			// Create and init the world.
			World world = new WorldCreator( worldName ).environment( environment ).createWorld();
			initializeWorld( world );

			// Let the network know that we've got the world loaded now.
			Bukkit.getScheduler().runTaskAsynchronously( core.getOriginObject(), () -> {
				try ( Jedis jedis = core.getJedis() ) {
					UUID uuid = UUID.fromString( worldName );
					core.getWorldManager().setServer( jedis, uuid, core.identify() );
				} catch ( NoJedisException | ProcessingException e ) {
					e.printStackTrace();
				}
			} );

			return world;

		} else {
			return existingWorld;
		}

	}

	/**
	 * Any methods to call on the world to be globally enforced.
	 *
	 * @param world The world of which to manipulate.
	 */
	private void initializeWorld( World world ) {
		// settings and init?
		world.setGameRuleValue( "mobGreifing", "false" );
	}

}
