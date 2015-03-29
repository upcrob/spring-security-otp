import spock.lang.*

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority

import com.upcrob.springsecurity.otp.OtpGeneratingAuthenticationProvider
import com.upcrob.springsecurity.otp.PreOtpAuthenticationToken
import com.upcrob.springsecurity.otp.Tokenstore
import com.upcrob.springsecurity.otp.LookupStrategy
import com.upcrob.springsecurity.otp.SendStrategy
import com.upcrob.springsecurity.otp.OtpGenerator

class OtpGeneratingAuthenticationProviderTests extends Specification {
	Tokenstore tokenstore
	LookupStrategy lookupStrategy
	SendStrategy sendStrategy
	OtpGeneratingAuthenticationProvider otpGenProv
	AuthenticationProvider embeddedProv
	Authentication authToken
	
	def setup() {
		tokenstore = Mock()
		tokenstore.isTokenValid("testuser", "notthetoken") >> false
		tokenstore.isTokenValid("testuser", "thetoken") >> true
		
		lookupStrategy = Mock()
		lookupStrategy.lookup("testuser") >> "testuser@example.com"
		
		sendStrategy = Mock()
		
		authToken = new UsernamePasswordAuthenticationToken("testuser", "testpassword",
			[new SimpleGrantedAuthority("role_test")])
		
		embeddedProv = Mock()
		embeddedProv.authenticate(_) >> authToken
		
		OtpGenerator gen = Mock()
		gen.generateToken() >> "abc"
		
		otpGenProv = new OtpGeneratingAuthenticationProvider(embeddedProv, tokenstore, lookupStrategy, sendStrategy)
		otpGenProv.setOtpGenerator(gen)
	}
	
	def 'test authenticate'() {
		when:
			Authentication token = otpGenProv.authenticate(authToken)
		then:
			token instanceof PreOtpAuthenticationToken
			token.getName() == "testuser"
			token.getAuthorities().size() == 0
			1 * sendStrategy.send("abc", "testuser@example.com")
	}
}