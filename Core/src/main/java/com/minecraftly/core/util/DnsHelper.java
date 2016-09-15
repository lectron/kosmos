/*
 * See provided LICENCE.txt in the project root.
 * Licenced to Minecraftly under GNU-GPLv3.
 */

package com.minecraftly.core.util;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Properties;

/**
 * @author Cory Redmond &lt;ace@ac3-servers.eu&gt;
 */
public class DnsHelper {

	private static final String CNAME_ATTRIB = "CNAME";
	private static Properties env;
	private static String[] CNAME_ATTRIBS = { CNAME_ATTRIB };

	static {
		env = new Properties();
		env.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory" );
	}

	public static String getCname( String host ) {
		try {
			return getCname( new InitialDirContext( env ), host );
		} catch ( Exception ex ) {
			return null;
		}
	}

	private static String getCname( InitialDirContext idc, String host ) throws NamingException {
		Attributes attrs = idc.getAttributes( host, CNAME_ATTRIBS );
		Attribute attr = attrs.get( CNAME_ATTRIB );

		try {
			return attr.get( 0 ).toString();
		} catch ( Exception ex ) {
			return host;
		}
	}

}
