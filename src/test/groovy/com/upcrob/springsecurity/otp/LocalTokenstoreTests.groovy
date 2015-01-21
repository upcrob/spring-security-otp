import spock.lang.*

import com.upcrob.springsecurity.otp.Tokenstore
import com.upcrob.springsecurity.otp.LocalTokenstore

class LocalTokenstoreTests extends Specification {
	Tokenstore tokenstore
	
	def setup() {
		tokenstore = new LocalTokenstore(1)
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