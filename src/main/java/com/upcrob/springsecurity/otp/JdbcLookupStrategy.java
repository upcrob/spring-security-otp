package com.upcrob.springsecurity.otp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Looks up user contact information (email, phone, etc) from a relational database.  If a user
 * exists, the query should return a row with the contact information in a column named, 'contact'.
 * The username will be inserted into the query wherever the placeholder, $username, is found.
 */
public class JdbcLookupStrategy implements LookupStrategy {
	
	private static final String CONTACT_COLUMN_KEY = "contact";
	private static final String USERNAME_SEARCH_REGEX = "\\$username";
	private JdbcTemplate jdbc;
	private String query;
	private Mapper mapper;
	
	public JdbcLookupStrategy(DataSource ds, String query) {
		this.jdbc = new JdbcTemplate(ds);
		this.mapper = new Mapper();
		this.query = query;
	}
	
	@Override
	public String lookup(String username) {
		List<String> info = jdbc.query(query.replaceAll(USERNAME_SEARCH_REGEX, username.replaceAll("'", "")), mapper);
		if (info.size() > 0) {
			return info.get(0);
		} else {
			return null;
		}
	}
	
	private static class Mapper implements RowMapper<String> {
		@Override
		public String mapRow(ResultSet rs, int rowNum) throws SQLException {
			return rs.getString(CONTACT_COLUMN_KEY);
		}
	}
}
