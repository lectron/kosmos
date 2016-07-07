/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A handy little class that deals with player worlds and their names.
 *
 * @author IKeirNez
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public enum WorldDimension {

	/**
	 * The Nether dimension.
	 */
	NETHER( World.Environment.NETHER, "The Nether", "_nether" ),
	/**
	 * The end dimension.
	 */
	THE_END( World.Environment.THE_END, "The End", "_the_end" );

	private World.Environment environment;
	private String niceName;
	private String suffix;

	WorldDimension( World.Environment environment, String niceName, String suffix ) {
		this.environment = environment;
		this.niceName = niceName;
		this.suffix = suffix;
	}

	public static WorldDimension fromEnvironment( @NonNull World.Environment environment ) {
		for ( WorldDimension worldDimension : values() ) {
			if ( worldDimension.getEnvironment() == environment ) {
				return worldDimension;
			}
		}

		return null;
	}

	public static String getBaseWorldName( @NonNull String worldName ) {
		String returnWorldName = worldName;

		for ( WorldDimension worldDimension : values() ) {
			if ( worldDimension.matches( worldName ) ) {
				returnWorldName = worldDimension.getBaseName( worldName );
				break;
			}
		}

		return returnWorldName;
	}

	public static World getBaseWorld( @NonNull World world ) {
		World baseWorld = Bukkit.getWorld( getBaseWorldName( world.getName() ) );
		return baseWorld != null ? baseWorld : world;
	}

	public static UUID getUUIDOfWorld( @NonNull World world ) {

		world = getBaseWorld( world );

		try {
			return UUID.fromString( world.getName() );
		} catch ( IllegalArgumentException ex ) {
			return null;
		}

	}

	public static List<Player> getPlayersAllDimensions( @NonNull World unknownWorld ) {

		World baseWorld = getBaseWorld( unknownWorld );
		List<Player> players = new ArrayList<>( baseWorld.getPlayers() );

		for ( WorldDimension worldDimension : WorldDimension.values() ) {
			World world = worldDimension.convertTo( baseWorld );
			if ( world != null ) {
				players.addAll( world.getPlayers() );
			}
		}

		return players;
	}

	public World.Environment getEnvironment() {
		return environment;
	}

	public String getNiceName() {
		return niceName;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean matches( @NonNull String worldName ) {
		return worldName.endsWith( suffix );
	}

	public String getBaseName( @NonNull String worldName ) {

		for ( WorldDimension worldDimension : values() ) {
			if ( worldDimension.matches( worldName ) ) {
				return worldName.substring( 0, worldName.length() - suffix.length() );
			}
		}

		return worldName;

	}

	public String convertTo( @NonNull String worldName ) {
		return getBaseName( worldName ) + suffix;
	}

	public World convertTo( @NonNull World world ) {
		return Bukkit.getWorld( convertTo( world.getName() ) );
	}

	public void convertToLoad( @NonNull MinecraftlyBukkitCore core, @NonNull World world, @NonNull Consumer<World> consumer ) {

		World loadedWorld = convertTo( world );

		if ( loadedWorld != null ) {
			consumer.accept( loadedWorld );
		} else {
			String newWorldName = convertTo( world.getName() );
			World newWorld = core.getWorldHandler().loadWorld( newWorldName, getEnvironment() );
			consumer.accept( newWorld ); // I'm going to keep the consumer because it can be useful.
		}

	}

}
