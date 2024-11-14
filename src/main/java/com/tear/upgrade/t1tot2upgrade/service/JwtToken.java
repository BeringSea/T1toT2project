package com.tear.upgrade.t1tot2upgrade.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JwtToken {

    private String secretKey = "";

    public JwtToken() {

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
            SecretKey sk = keyGen.generateKey();
            secretKey = Base64.getEncoder().encodeToString(sk.getEncoded());
            log.info("JWT secret key successfully generated and encoded.");
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating secret key for JWT", e);
            throw new RuntimeException(e);
        }
    }

    public String generateToken(UserDetails userDetails) {
        log.info("Generating JWT token for user: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .and()
                .signWith(getKey())
                .compact();

    }

    public String extractUserName(String token) {
        log.info("Extracting username from token.");
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        log.info("Validating JWT token for user: {}", userDetails.getUsername());
        final String userName = extractUserName(token);
        final List<String> roles = extractRoles(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token) &&
                new HashSet<>(roles).containsAll(userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
    }

    private SecretKey getKey() {
        log.debug("Retrieving secret key for signing/verifying JWT.");
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        log.debug("Extracting claim from token.");
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        log.debug("Parsing all claims from JWT token.");
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        log.debug("Checking if the token has expired.");
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        log.debug("Extracting expiration date from token.");
        return extractClaim(token, Claims::getExpiration);
    }

    private List<String> extractRoles(String token) {
        log.debug("Extracting roles from token.");
        Claims claims = extractAllClaims(token);
        List<?> roles = claims.get("roles", List.class);
        return roles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .collect(Collectors.toList());
    }
}
