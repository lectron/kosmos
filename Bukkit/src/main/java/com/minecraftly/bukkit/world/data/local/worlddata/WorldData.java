/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.local.worlddata;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.AbstractLocalData;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.configuration.MinecraftlyConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Data
@EqualsAndHashCode( callSuper = true )
public class WorldData extends AbstractLocalData {

	/**
	 * A map of banned user UUID's and their PunishEntry.
	 */
	private Map<UUID, PunishEntry> bannedUsers = new HashMap<>();

	/**
	 * A map of muted user UUID's and their PunishEntry.
	 */
	private Map<UUID, PunishEntry> mutedUsers = new HashMap<>();

	/**
	 * A list of trusted users.
	 */
	private List<UUID> trustedUsers = new ArrayList<>();

	/**
	 * Is the "universe" white listed?
	 */
	private boolean whiteListed = false;

	/**
	 * A list of white listed users.
	 */
	private List<UUID> whiteListedUsers = new ArrayList<>();

	/**
	 * The file of which to save to upon unloading.
	 */
	private transient File dataFile = null;

	public static WorldData load( MinecraftlyBukkitCore core, World world ) {

		WorldData ret = new WorldData();
		world = WorldDimension.getBaseWorld( world );
		File dataFile = new File( getSaveFolder( core, world ), "world_data.json" );

		try {
			ret = MinecraftlyConfiguration.getGson().fromJson( MinecraftlyUtil.readText( dataFile ), WorldData.class );
		} catch ( IOException e ) {
			core.getLogger().log( Level.SEVERE, "An error occurred whilst reading the data file for world \"" + world.getName() + "\"", e );
		}

		ret.dataFile = dataFile;

		return ret;

	}

	public final void save( MinecraftlyBukkitCore core, World world ) throws IOException {

		world = WorldDimension.getBaseWorld( world );
		dataFile = new File( getSaveFolder( core, world ), "world_data.json" );
		save( core );

	}

	@Override
	public final boolean save( MinecraftlyBukkitCore core ) throws IOException {

		if( dataFile == null ) return false;

		try ( FileWriter fw = new FileWriter( dataFile ) ) {
			fw.write( MinecraftlyConfiguration.getGson().toJson( this ) );
			fw.flush();
			return true;
		} catch ( IOException e ) {
			core.getLogger().log( Level.SEVERE, "An error occurred whilst saving the data file for world \"" + dataFile.getName() + "\"", e );
			throw e;
		}

	}

}
