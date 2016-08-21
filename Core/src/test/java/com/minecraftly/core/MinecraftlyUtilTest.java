/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class MinecraftlyUtilTest {

	@Test
	public void tickConversionTest() {

		Assert.assertEquals( 1, MinecraftlyUtil.convertMillisToTicks( 40 ) );
		Assert.assertEquals( 1, MinecraftlyUtil.convertMillisToTicks( 50 ) );
		Assert.assertEquals( 1, MinecraftlyUtil.convertMillisToTicks( 60 ) );

		Assert.assertEquals( 3, MinecraftlyUtil.convertMillisToTicks( 140 ) );
		Assert.assertEquals( 3, MinecraftlyUtil.convertMillisToTicks( 150 ) );
		Assert.assertEquals( 3, MinecraftlyUtil.convertMillisToTicks( 160 ) );

	}

	@Test
	public void downloadTest() {
		// TODO
	}

	@Test
	public void readFileTest() {
		// TODO
	}

}
