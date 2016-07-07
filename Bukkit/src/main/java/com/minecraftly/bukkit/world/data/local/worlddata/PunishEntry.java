/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.bukkit.world.data.local.worlddata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PunishEntry {

	/**
	 * The time of the ban being issued + bantime.
	 */
	private long time = -1;

	/**
	 * The reason for the ban.
	 */
	private String reason = "&c&lYou've been banned!";

	/**
	 * Is the banentry banned.
	 * @return true if they're banned.
	 */
	public boolean isBanned() {
		return time < 0 || (System.currentTimeMillis() < time);
	}

	/**
	 * Gets the remaining ban time.
	 * @return The remaining ban time.
	 */
	public long getRemainingBanTime() {
		return time - System.currentTimeMillis();
	}

}
