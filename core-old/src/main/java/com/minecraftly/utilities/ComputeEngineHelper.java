package com.minecraftly.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * These methods USED TO only work in Google Compute Engine environments. HOWEVER, I made a quick fix for it to work with public IP of any server using http://ipinfo.io/ip service
 */
public class ComputeEngineHelper {

    private ComputeEngineHelper() {}

    /**
     * Gets this instance's unique id.
     * This value never changes and therefore should be cached where possible.
     *
     * @return the instance's unique id
     * @throws IOException
     */
    public static String queryUniqueId() throws IOException {
        return queryComputeAPI("http://ipinfo.io/ip");
    }

    /**
     * Gets this instance's ip address.
     * This value never changes and therefore should be cached where possible.
     *
     * @return the instance's unique id
     * @throws IOException
     */
    public static String queryIpAddress() throws IOException {
        return queryComputeAPI("http://ipinfo.io/ip");
    }

    /**
     * Queries the compute api on the url specified and returns the response.
     * A lot of the API responses are values that never change and therefore should be cached where possible.
     *
     * @param url the compute API url to query
     * @return the response
     * @throws IOException
     */
    public static String queryComputeAPI(String url) throws IOException {
        try {
            URLConnection computeIdUrlConnection = new URL(url).openConnection();
            computeIdUrlConnection.addRequestProperty("Metadata-Flavor", "Google");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(computeIdUrlConnection.getInputStream()))) {
                return reader.readLine(); // compute is single line response
            }
        } catch (IOException e) {
            throw new IOException("Error retrieving response from Google Compute API.", e);
        }
    }
}
