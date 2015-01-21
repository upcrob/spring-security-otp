import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.*

import com.upcrob.derby.InMemoryDataSource
import com.upcrob.springsecurity.otp.Tokenstore
import com.upcrob.springsecurity.otp.JdbcTokenstore
import javax.sql.DataSource

class JdbcTokenstoreTests extends Specification {
	Tokenstore tokenstore
	
	def setup() {
		DataSource ds = new InMemoryDataSource()
		JdbcTemplate jdbc = new JdbcTemplate(ds)
		jdbc.execute("CREATE TABLE tokens (username VARCHAR(255) PRIMARY KEY, token VARCHAR(255), expires BIGINT)")
		tokenstore = new JdbcTokenstore(ds, "tokens", 1)
	}
	
	def 'valid token validates'() {
		when:
			tokenstore.putToken("user", "thetoken")
		then:
			tokenstore.isTokenValid("user", "thetoken")
	}
	
	def 'invalid token does not validate'() {
		when:
			tokenstore.putToken("user", "thetoken")
		then:
			!tokenstore.isTokenValid("user", "notthetoken")
	}
	
	def 'token expires'() {
		when:
			tokenstore.putToken("user", "thetoken")
			Thread.sleep(1500)
		then:
			!tokenstore.isTokenValid("user", "thetoken")
	}
}