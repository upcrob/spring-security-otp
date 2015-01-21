package com.upcrob.springsecurity.otp;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Looks up user contact information from an LDAP data source.  This class must be configured with a
 * filter and a lookup attribute.  The filter tells instances of this class how to find the user by
 * username.  The lookup attribute is the field that should be returned from the ldap data source
 * ("mail", for example).
 * 
 * Note that the username passed into the lookup() method replaces each occurrence of "$username" found
 * in the filter string.  If, for example, you needed to lookup a user by sAMAccountName, your filter
 * query might look like the following: (sAMAccountName = $username)
 */
public class LdapLookupStrategy implements LookupStrategy {

	private LdapTemplate ldap;
	private String filter;
	private AttributesMapper mapper;

	public LdapLookupStrategy(LdapTemplate ldap, String filter, String lookupAttribute) {
		this.ldap = ldap;
		this.filter = filter;
		this.mapper = new Mapper(lookupAttribute);
	}

	@Override
	public String lookup(String username) {
		if (username.contains("(") || username.contains(")")) {
			return null;
		}
		List<?> users = ldap.search("", filter.replaceAll("\\$username", username), mapper);
		if (users.size() > 0) {
			return users.get(0).toString();
		} else {
			return null;
		}
	}
	
	private static class Mapper implements AttributesMapper {
		private String lookupAttribute;
		
		public Mapper(String lookupAttribute) {
			this.lookupAttribute = lookupAttribute;
		}
		
		@Override
		public Object mapFromAttributes(Attributes attributes)
				throws NamingException {
			return attributes.get(lookupAttribute).get();
		}
	}
}
