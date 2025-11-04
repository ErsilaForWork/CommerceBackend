package ers.backend.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;


@Component
public class JwtUtils {

    private final String JWTSecretKey;
    private final int jwtExpireMillis;

    public JwtUtils(@Value("${jwt.secret}") String JWTSecretKey, @Value("${jwt.expire.millis}") int jwtExpireMillis) {
        this.JWTSecretKey = JWTSecretKey;
        this.jwtExpireMillis = jwtExpireMillis;
    }

    public String getTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpireMillis))
                .signWith(getKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWTSecretKey));
    }


    public boolean validateToken(String token) {
        try{
            System.out.println("Validating!");
            Jwts.parser().verifyWith((SecretKey) getKey()).build().parseSignedClaims(token);
            return true;
        }catch (MalformedJwtException e) {
            System.out.println("Invalid JWT Token {} " + e.getMessage());
        }catch (ExpiredJwtException e) {
            System.out.println("JWT Token is expired {} " + e.getMessage());
        }catch (UnsupportedJwtException e) {
            System.out.println("JWT Token is unsupported {} " + e.getMessage());
        }catch (IllegalArgumentException e) {
            System.out.println("JWT Claims String is empty" + e.getMessage());
        }
        return false;
    }
}
