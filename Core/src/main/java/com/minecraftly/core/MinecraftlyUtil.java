/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Cory Redmond <ace@ac3-servers.eu>
 */
public class MinecraftlyUtil {

	private static final Pattern UUID_DASH_PATTERN = Pattern.compile( "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})" );

	public static UUID convertFromNoDashes(String uuidString) {
		return UUID.fromString(UUID_DASH_PATTERN.matcher(uuidString).replaceAll("$1-$2-$3-$4-$5"));
	}

	public static String convertToNoDashes(UUID uuid) {
		return convertToNoDashes(uuid.toString());
	}

	public static String convertToNoDashes(String uuidString) {
		return uuidString.replace("-", "");
	}

	/**
	 * Download text from a string url.
	 *
	 * @param url The url to download from.
	 * @return The text from the webpage.
	 * @throws IOException
	 */
	public static String downloadText( String url ) throws IOException {
		return downloadText( new URL( url ) );
	}

	/**
	 * Download text from a {@link URL}.
	 *
	 * @param url The url to download from.
	 * @return The text from the webpage.
	 * @throws IOException
	 */
	public static String downloadText( URL url ) throws IOException {
		return new Scanner( url.openStream() ).useDelimiter( "\\A" ).next();
	}

	/**
	 * Read text from a string filepath.
	 *
	 * @param filePath THe filepath of where to read.
	 * @return The string file contents.
	 * @throws IOException
	 */
	public static String readText( String filePath ) throws IOException {
		return readText( new File( filePath ) );
	}

	/**
	 * Read text from a {@link File}.
	 *
	 * @param file The file of where to read.
	 * @return The string file contents.
	 * @throws IOException
	 */
	public static String readText( File file ) throws IOException {
		return new Scanner( file ).useDelimiter( "\\A" ).next();
	}

	/**
	 * Convert milliseconds to minecraft ticks (20th of a second).
	 *
	 * @param milliseconds The milliseconds to convert from.
	 * @return The ticks converted from the millis.
	 */
	public static long convertMillisToTicks( long milliseconds ) {
		double nearestTickTime = round( milliseconds, 50 );
		return (long) ((nearestTickTime / 1000) * 20);
	}

	/**
	 * Round the value to the nearest factor.
	 *
	 * @param value  The value to round.
	 * @param factor The factor of where to round.
	 * @return The rounded value.
	 */
	public static double round( double value, double factor ) {
		return (Math.round( value / factor ) * factor);
	}

	/**
	 * Parse a string version of an address.
	 *
	 * @param address String representation of an IP:Port.
	 * @return The address.
	 */
	public static InetSocketAddress parseAddress( String address ) {

		String[] addressSections = address.split( ":" );
		int port = 25565;

		if ( addressSections.length < 1 )
			throw new IllegalArgumentException( "The address \"" + address + "\" is invalid." );

		if ( addressSections.length >= 2 ) {
			try {
				port = Integer.parseInt( addressSections[1].trim() );
			} catch ( NumberFormatException ex ) {
				port = 25565;
			}
		}

		return new InetSocketAddress( addressSections[0].trim(), port );

	}

	/**
	 * Formats milliseconds into human readable text.
	 *
	 * @param millis Milliseconds to be formatted.
	 * @return The formatted string that looks beautiful.
	 */
	public static String getTimeString(long millis) {

		if (millis < 1L) {
			return "not very long!";
		} else {
			long days = TimeUnit.MILLISECONDS.toDays(millis);

			millis -= TimeUnit.DAYS.toMillis(days);
			long hours = TimeUnit.MILLISECONDS.toHours(millis);

			millis -= TimeUnit.HOURS.toMillis(hours);
			long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

			millis -= TimeUnit.MINUTES.toMillis(minutes);
			long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
			StringBuilder sb = new StringBuilder();

			if (days > 0L) {
				sb.append(days).append(" day");
				if (days > 1L) {
					sb.append("s");
				}
			}

			if (hours > 0L) {
				if (days > 0L) {
					sb.append(", ");
				}

				sb.append(hours).append(" hour");
				if (hours > 1L) {
					sb.append("s");
				}
			}

			if (minutes > 0L) {
				if (hours > 0L || days > 0L) {
					sb.append(", ");
				}

				sb.append(minutes).append(" minute");
				if (minutes > 1L) {
					sb.append("s");
				}
			}

			if (seconds > 0L) {
				if (minutes > 0L || hours > 0L || days > 0L) {
					sb.append(", ");
				}

				sb.append(seconds).append(" second");
				if (seconds > 1L) {
					sb.append("s");
				}
			}

			return sb.toString();
		}

	}

}
