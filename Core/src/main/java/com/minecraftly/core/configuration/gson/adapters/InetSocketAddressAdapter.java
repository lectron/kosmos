/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.configuration.gson.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.InetSocketAddress;

/**
 * A Gson adapter that handles {@link InetSocketAddress}es.
 * https://github.com/DesignAndDeploy/dnd/blob/master/DND/src/edu/teco/dnd/util/InetSocketAddressAdapter.java
 */
public class InetSocketAddressAdapter implements JsonSerializer<InetSocketAddress>, JsonDeserializer<InetSocketAddress> {

	public static final InetSocketAddressAdapter INSTANCE = new InetSocketAddressAdapter();

	@Override
	public InetSocketAddress deserialize( final JsonElement json, final Type typeOfT, final JsonDeserializationContext context ) throws JsonParseException {

		if ( !json.isJsonObject() ) {
			throw new JsonParseException( "not a JSON object" );
		}

		final JsonObject obj = (JsonObject) json;
		final JsonElement address = obj.get( "address" );
		final JsonElement port = obj.get( "port" );

		if ( address == null || port == null ) {
			throw new JsonParseException( "address/port missing" );
		}

		if ( !address.isJsonPrimitive() || !((JsonPrimitive) address).isString() ) {
			throw new JsonParseException( "address is not a string" );
		}

		if ( !port.isJsonPrimitive() || !((JsonPrimitive) port).isNumber() ) {
			throw new JsonParseException( "port is not a number" );
		}

		return new InetSocketAddress( address.getAsString(), port.getAsInt() );

	}

	@Override
	public JsonElement serialize( final InetSocketAddress src, final Type typeOfSrc, final JsonSerializationContext context ) {

		final JsonObject obj = new JsonObject();
		obj.addProperty( "address", src.getHostName() );
		obj.addProperty( "port", src.getPort() );
		return obj;

	}

}
