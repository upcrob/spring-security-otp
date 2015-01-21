import spock.lang.*

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication

import com.upcrob.springsecurity.otp.Tokenstore
import com.upcrob.springsecurity.otp.OtpAuthenticationFilter
import com.upcrob.springsecurity.otp.OtpAuthenticationException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class OtpAuthenticationFilterTests extends Specification {
	Tokenstore tokenstore
	OtpAuthenticationFilter filter
	HttpServletRequest req
	HttpServletResponse resp
	AuthenticationManager manager
	Authentication authToken
	
	def setup() {
		tokenstore = Mock()
		tokenstore.isTokenValid("testuser", "notthetoken") >> false
		tokenstore.isTokenValid("testuser", "thetoken") >> true
		
		authToken = Mock()
		authToken.getName() >> "testuser"
		manager = Mock()
		manager.authenticate(_) >> authToken
		
		filter = new OtpAuthenticationFilter(tokenstore)
		filter.setAuthenticationManager(manager)
		
		req = Mock()
		req.getMethod() >> "POST"
		resp = Mock()
	}
	
	def 'test no token on request'() {
		given:
			req.getParameter(filter.getUsernameParameter()) >> "testuser"
			req.getParameter(filter.getOtpParameter()) >> null
		when:
			filter.attemptAuthentication(req, resp)
		then:
			0 * tokenstore.isTokenValid(_, _)
			thrown(OtpAuthenticationException)
	}
	
	def 'test no username on request'() {
		given:
			req.getParameter(filter.getUsernameParameter()) >> null
			req.getParameter(filter.getOtpParameter()) >> "thetoken"
		when:
			filter.attemptAuthentication(req, resp)
		then:
			0 * tokenstore.isTokenValid(_, _)
			thrown(OtpAuthenticationException)
	}
	
	def 'test token not valid'() {
		given:
			req.getParameter(filter.getUsernameParameter()) >> "testuser"
			req.getParameter(filter.getPasswordParameter()) >> "dummypassword"
			req.getParameter(filter.getOtpParameter()) >> "notthetoken"
		when:
			filter.attemptAuthentication(req, resp)
		then:
			1 * tokenstore.isTokenValid("testuser", "notthetoken")
			thrown(OtpAuthenticationException)
	}
	
	def 'test token valid'() {
		given:
			Authentication authn
			req.getParameter(filter.getUsernameParameter()) >> "testuser"
			req.getParameter(filter.getPasswordParameter()) >> "dummypassword"
			req.getParameter(filter.getOtpParameter()) >> "thetoken"
		when:
			authn = filter.attemptAuthentication(req, resp)
		then:
			authn.getName() == "testuser"
	}
}