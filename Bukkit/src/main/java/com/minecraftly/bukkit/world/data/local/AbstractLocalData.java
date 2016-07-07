/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.local;

import com.minecraftly.bukkit.MinecraftlyBukkitCore;
import com.minecraftly.core.MinecraftlyCore;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public abstract class AbstractLocalData {

	public abstract boolean save( MinecraftlyBukkitCore core ) throws IOException;

	public static File getSaveFolder( MinecraftlyCore core, World world ) {
		return getSaveFolder( core, world.getWorldFolder() );
	}

	public static File getSaveFolder( MinecraftlyCore core, File worldFolder ) {

		File saveFolder = new File( worldFolder, "minecraftly" );

		if( !saveFolder.exists() ) {
			saveFolder.mkdirs();
		} else if( !saveFolder.isDirectory() ) {
			core.getLogger().warning( "\"" + saveFolder.getAbsolutePath() + "\" exists, but isn't a directory. We need this as a DIR!" );
		}

		return saveFolder;

	}

}
