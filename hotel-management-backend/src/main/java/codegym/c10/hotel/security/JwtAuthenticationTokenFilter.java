package codegym.c10.hotel.security;

import codegym.c10.hotel.service.user.IUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ObjectMapper objectMapper;

    // Sử dụng lazy loading để tránh circular dependency
    private IUserService getUserService() {
        return applicationContext.getBean(IUserService.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            logger.debug("Processing request: {}", requestURI);
            
            // Kiểm tra token JWT
            String jwt = getJwtFromRequest(request);
            if (jwt != null) {
                try {
                    String username = jwtUtil.extractUsername(jwt);
                    logger.debug("Extracted username from JWT: {}", username);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // Tải thông tin người dùng từ username trong token
                        UserDetails userDetails = getUserService().loadUserByUsername(username);
                        logger.debug("Loaded UserDetails with authorities: {}", userDetails.getAuthorities());

                        // Kiểm tra token có hợp lệ không
                        if (jwtUtil.validateToken(jwt, userDetails)) {
                            // Tạo authentication object
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails,
                                            null,
                                            userDetails.getAuthorities()
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            
                            // Thiết lập authentication trong SecurityContext
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.debug("Successfully set authentication in SecurityContext");
                        } else {
                            logger.warn("JWT token validation failed for user: {}", username);
                        }
                    }
                } catch (ExpiredJwtException e) {
                    logger.error("JWT token has expired: {}", e.getMessage());
                    handleExpiredJwtException(response, e);
                    return;
                }
            } else {
                logger.debug("No JWT token found in request");
            }
            
            // Tiếp tục chuỗi filter
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
            filterChain.doFilter(request, response);
        }
    }

    private void handleExpiredJwtException(HttpServletResponse response, ExpiredJwtException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", "Token has expired");
        errorDetails.put("path", "JWT Authentication");
        
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            logger.debug("Found Bearer token in Authorization header");
            return bearerToken.substring(7);
        }
        logger.debug("No Bearer token found in Authorization header");
        return null;
    }
}