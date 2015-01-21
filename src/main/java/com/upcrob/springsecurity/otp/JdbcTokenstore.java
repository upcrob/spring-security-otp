package com.upcrob.springsecurity.otp;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Tokenstore that uses a relational database over JDBC.
 * 
 * The table used to store the tokens must conform to the following schema:
 *   username VARCHAR(255) PRIMARY KEY
 *   token CHAR/VARCHAR(255)            (size depends upon maximum token size selected)
 *   expires BIGINT
 */
public class JdbcTokenstore implements Tokenstore {
	
	private JdbcTemplate jdbc;
	private String table;
	private int maxLifetime;
	private TokenRowMapper mapper;
	
	public JdbcTokenstore(DataSource ds, String table, int maxLifetime) {
		Assert.notNull(ds, "JDBC DataSource must not be null.");
		Assert.notNull(ds, "Table must be non-null.");
		
		this.table = table;
		this.jdbc = new JdbcTemplate(ds);
		this.mapper = new TokenRowMapper();
		this.maxLifetime = maxLifetime;
	}

	@Override
	public void putToken(String username, String token) {
		String query = "INSERT INTO "
				+ table
				+ " (username,token,expires) VALUES ("
				+ "'" + username + "',"
				+ "'" + token + "',"
				+ (System.currentTimeMillis() + (maxLifetime * 1000))
				+ ")";
		jdbc.execute(query);
	}

	@Override
	@Transactional
	public boolean isTokenValid(String username, String token) {
		String query = "SELECT token,expires FROM "
				+ table
				+ " WHERE username = '"
				+ username
				+ "'";
		List<Token> tokens = jdbc.query(query, mapper);
		if (tokens.size() > 0) {
			query = "DELETE FROM " + table + " WHERE username = '" + username + "'";
			jdbc.update(query);
			Token t = tokens.get(0);
			if (!t.value.equals(token) || t.expires < System.currentTimeMillis()) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	private static class TokenRowMapper implements RowMapper<Token> {
		@Override
		public Token mapRow(ResultSet rs, int rowNum) throws SQLException {
			return new Token(rs.getString("token"), rs.getLong("expires"));
		}
	}
	
	private static class Token {
		private String value;
		private long expires;
		
		public Token(String value, long expires) {
			this.value = value;
			this.expires = expires;
		}
	}
}
