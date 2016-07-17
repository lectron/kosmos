/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.local.userdata;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.bukkit.world.WorldDimension;
import com.minecraftly.bukkit.world.data.local.AbstractLocalData;
import com.minecraftly.core.MinecraftlyUtil;
import com.minecraftly.core.configuration.MinecraftlyConfiguration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Data
@EqualsAndHashCode( callSuper = true )
public class UserData extends AbstractLocalData {

	/**
	 * The bed location of the user.
	 */
	private Location bedLocation = null;

	/**
	 * The last known location of the user.
	 */
	private Location lastLocation = null;

	/**
	 * A map of home locations and their names.
	 */
	private HashMap<String, Location> homes = new HashMap<>();

	/**
	 * A list of users of whom to ignore.
	 */
	private List<UUID> ignoredUsers = new ArrayList<>();

	/**
	 * The file to save to.
	 */
	private transient File dataFile = null;

	/**
	 * The world which this instance belongs to.
	 */
	private transient World owner = null;

	public static UserData load( MinecraftlyBukkitCore core, World world, UUID uuid ) {

		UserData ret = new UserData();
		File dataFile = new File( getUserDataFolder( core, world ), uuid.toString() + ".json" );

		try {
			if( dataFile.exists() )
				ret = MinecraftlyConfiguration.getGson().fromJson( MinecraftlyUtil.readText( dataFile ), UserData.class );
		} catch ( IOException e ) {
			core.getLogger().log( Level.SEVERE, "An error occurred whilst reading the data file for world \"" + world.getName() + "\"", e );
		}

		ret.owner = world;
		ret.dataFile = dataFile;

		return ret;

	}

	public static File getUserDataFolder( MinecraftlyBukkitCore core, World world ) {

		world = WorldDimension.getBaseWorld( world );
		File saveFolder = new File( getSaveFolder( core, world ), "userdata" );

		if( !saveFolder.exists() ) {
			saveFolder.mkdirs();
		} else if( !saveFolder.isDirectory() ) {
			core.getLogger().warning( "\"" + saveFolder.getAbsolutePath() + "\" exists, but isn't a directory. We need this as a DIR!" );
		}

		return saveFolder;

	}

	public final void save( MinecraftlyBukkitCore core, World world, UUID uuid ) throws IOException {

		dataFile = new File( getUserDataFolder( core, world ), uuid.toString() + ".json" );
		owner = world;
		save( core );

	}

	@Override
	public final boolean save( MinecraftlyBukkitCore core ) throws IOException {

		if( dataFile == null ) return false;

		try ( FileWriter fw = new FileWriter( dataFile ) ) {
			fw.write( MinecraftlyConfiguration.getGson().toJson( this ) );
			fw.flush();
		} catch ( IOException e ) {
			core.getLogger().log( Level.SEVERE, "An error occurred whilst saving the data file for world \"" + dataFile.getName() + "\"", e );
			throw e;
		}

		return true;

	}
	
}
