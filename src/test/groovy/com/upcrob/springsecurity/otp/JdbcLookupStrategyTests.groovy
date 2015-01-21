import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.*

import com.upcrob.derby.InMemoryDataSource
import com.upcrob.springsecurity.otp.LookupStrategy
import com.upcrob.springsecurity.otp.JdbcLookupStrategy

import javax.sql.DataSource

class JdbcLookupStrategyTests extends Specification {
	LookupStrategy lookupStrategy
	
	def setup() {
		DataSource ds = new InMemoryDataSource()
		JdbcTemplate jdbc = new JdbcTemplate(ds)
		jdbc.execute("CREATE TABLE users (username VARCHAR(255) PRIMARY KEY, email VARCHAR(255))")
		jdbc.execute("INSERT INTO users VALUES ('testuser', 'testuser@example.com')")
		lookupStrategy = new JdbcLookupStrategy(ds, "SELECT email AS contact FROM users WHERE username = '\$username'")
	}
	
	def 'lookup returns contact information'() {
		expect:
			lookupStrategy.lookup("testuser") == "testuser@example.com"
	}
	
	def 'lookup returns null when no contact information found'() {
		expect:
			lookupStrategy.lookup("nouser") == null
	}
}