import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import spock.lang.*

import com.upcrob.springsecurity.otp.Tokenstore
import com.upcrob.springsecurity.otp.OtpValidationFilter
import com.upcrob.springsecurity.otp.LookupStrategy
import com.upcrob.springsecurity.otp.SendStrategy
import com.upcrob.springsecurity.otp.OtpAuthenticationException
import com.upcrob.springsecurity.otp.PreOtpAuthenticationToken
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException;

class OtpValidationFilterTests extends Specification {
	Tokenstore tokenstore
	LookupStrategy lookupStrategy
	SendStrategy sendStrategy
	OtpValidationFilter filter
	HttpServletRequest req
	HttpServletResponse resp
	FilterChain chain
	
	def setup() {
		tokenstore = Mock()
		tokenstore.isTokenValid("testuser", "notthetoken") >> false
		tokenstore.isTokenValid("testuser", "thetoken") >> true
		tokenstore.isTokenValid("testuser2", "token") >> false
		
		req = Mock()
		req.getContextPath() >> ""
		req.getRequestURI() >> "/validate"
		req.getParameter("otptoken") >> "thetoken"
		
		PreOtpAuthenticationToken tok = new PreOtpAuthenticationToken(
			new UsernamePasswordAuthenticationToken("testuser", "password",
				[new SimpleGrantedAuthority("role_a")]))
		SecurityContextHolder.getContext().setAuthentication(tok)
		
		resp = Mock()
		chain = Mock()
		
		filter = new OtpValidationFilter(tokenstore, "/validate", "/success", "/failure")
	}
	
	def 'test continue chain if different endpoint'() {
		given:
			req = Mock()
			req.getContextPath() >> ""
			req.getRequestURI() >> "/other"
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * chain.doFilter(req, resp)
	}
	
	def 'test successful validation'() {
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * resp.sendRedirect("/success")
	}
	
	def 'test failure when no token'() {
		given:
			req = Mock()
			req.getContextPath() >> ""
			req.getRequestURI() >> "/validate"
			req.getParameter("otptoken") >> null
			PreOtpAuthenticationToken tok = new PreOtpAuthenticationToken(
				new UsernamePasswordAuthenticationToken("testuser", "password",
					[new SimpleGrantedAuthority("role_a")]))
			SecurityContextHolder.getContext().setAuthentication(tok)
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * resp.sendRedirect("/failure")
	}
	
	def 'test failure when invalid token'() {
		given:
			req = Mock()
			req.getContextPath() >> ""
			req.getRequestURI() >> "/validate"
			req.getParameter("otptoken") >> "notthetoken"
			PreOtpAuthenticationToken tok = new PreOtpAuthenticationToken(
				new UsernamePasswordAuthenticationToken("testuser", "password",
					[new SimpleGrantedAuthority("role_a")]))
			SecurityContextHolder.getContext().setAuthentication(tok)
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * resp.sendRedirect("/failure")
	}
	
	def 'test failure when no user'() {
		given:
			req = Mock()
			req.getContextPath() >> ""
			req.getRequestURI() >> "/validate"
			req.getParameter("otptoken") >> "thetoken"
			SecurityContextHolder.getContext().setAuthentication(null)
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * resp.sendRedirect("/failure")
	}
	
	def 'test failure when invalid user'() {
		given:
			req = Mock()
			req.getContextPath() >> ""
			req.getRequestURI() >> "/validate"
			req.getParameter("otptoken") >> "token"
			PreOtpAuthenticationToken tok = new PreOtpAuthenticationToken(
				new UsernamePasswordAuthenticationToken("testuser2", "password",
					[new SimpleGrantedAuthority("role_a")]))
			SecurityContextHolder.getContext().setAuthentication(tok)
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * resp.sendRedirect("/failure")
	}
	
	def 'test failure when not PreOtpAuthenticationToken'() {
		given:
			req = Mock()
			req.getContextPath() >> ""
			req.getRequestURI() >> "/validate"
			req.getParameter("otptoken") >> "thetoken"
			UsernamePasswordAuthenticationToken tok = new UsernamePasswordAuthenticationToken(
				"testuser",
				"password",
				[new SimpleGrantedAuthority("role_a")])
			SecurityContextHolder.getContext().setAuthentication(tok)
		when:
			filter.doFilter(req, resp, chain)
		then:
			1 * resp.sendRedirect("/failure")
	}
}