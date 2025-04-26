package codegym.c10.hotel.service;
import codegym.c10.hotel.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final JwtUtil jwtUtil;

    public AuthenticatedUserService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Trích xuất userId từ HttpServletRequest (Authorization Header)
     */
    public Long extractUserId(HttpServletRequest request) {
        String jwt = getJwtFromRequest(request);
        if (jwt == null) {
            throw new RuntimeException("Authorization token is missing");
        }
        return getUserIdFromJwt(jwt);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Long getUserIdFromJwt(String jwt) {
        try {
            return jwtUtil.extractUserId(jwt);
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Token has expired");
        } catch (Exception e) {
            throw new RuntimeException("Error extracting user ID from token");
        }
    }
}
