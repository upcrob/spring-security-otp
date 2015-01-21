import spock.lang.*

import com.upcrob.springsecurity.otp.Tokenstore
import com.upcrob.springsecurity.otp.OtpGenerationFilter
import com.upcrob.springsecurity.otp.LookupStrategy
import com.upcrob.springsecurity.otp.SendStrategy
import com.upcrob.springsecurity.otp.OtpAuthenticationException
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException;

class OtpGenerationFilterTests extends Specification {
	Tokenstore tokenstore
	LookupStrategy lookup
	SendStrategy send
	OtpGenerationFilter filter
	HttpServletRequest req
	HttpServletResponse resp
	FilterChain chain
	
	def setup() {
		tokenstore = Mock()
		
		lookup = Mock()
		lookup.lookup("testuser") >> "testuser@example.com"
		lookup.lookup("nouser") >> null
		
		PrintWriter out = Mock()
		req = Mock()
		req.getContextPath() >> ""
		req.getRequestURI() >> "/generate"
		resp = Mock()
		resp.getWriter() >> out
		chain = Mock()
		send = Mock()
		
		filter = new OtpGenerationFilter(tokenstore, lookup, send, "/generate")
	}
	
	def 'test no username on request'() {
		given:
			req.getParameter(filter.getUsernameParameter()) >> null
		when:
			filter.doFilter(req, resp, chain)
		then:
			thrown(ServletException)
	}
	
	def 'test invalid username'() {
		given:
			req.getParameter(filter.getUsernameParameter()) >> "nouser"
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * lookup.lookup("nouser")
			0 * send.send(_, _)
	}
	
	def 'test valid username'() {
		given:
			req.getParameter(filter.getUsernameParameter()) >> "testuser"
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * send.send(_, "testuser@example.com")
	}
}