package com.gpt.product.gpcash.corporate.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.MalformedJwtException;
//import io.jsonwebtoken.SignatureException;
//import io.jsonwebtoken.UnsupportedJwtException;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
//		try {
//			if (checkJWTToken(request, response)) {
//				Claims claims = validateToken(request);
//				
//				if (claims.get("authorities") != null) {
//					setUpSpringAuthentication(claims);
//				} else {
//					SecurityContextHolder.clearContext();
//				}
//			}else {
//				SecurityContextHolder.clearContext();
//			}
//			chain.doFilter(request, response);
//		} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
//			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//			((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, "Token Authorization Failed");
//			return;
//		}
	}
	
//	private Claims validateToken(HttpServletRequest request) {
//		String jwtToken = request.getHeader("X-Token");
//		String key = request.getHeader("X-Authorization");
//		
//		return Jwts.parser().setSigningKey(key.getBytes()).parseClaimsJws(jwtToken).getBody();
//	}

	/**
	 * Authentication method in Spring flow
	 * 
	 * @param claims
	 */
//	private void setUpSpringAuthentication(Claims claims) {
//		@SuppressWarnings("unchecked")
//		List<String> authorities = (List<String>) claims.get("authorities");
//
//		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
//				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
//		SecurityContextHolder.getContext().setAuthentication(auth);
//
//	}

	private boolean checkJWTToken(HttpServletRequest request, HttpServletResponse res) {
		String jwtToken = request.getHeader("X-Token");
		String key = request.getHeader("X-Authorization");		
		return jwtToken != null && key !=null ;
	}

}
