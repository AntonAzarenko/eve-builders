package com.azarenka.evebuilders.service.impl.auth.eve.ui;

import com.azarenka.evebuilders.domain.auth.auth.ui.JwtProperties;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final JwtProperties props;
    private final SecretKey key;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails user) {
        Instant now = Instant.now();
        List<String> roles = user.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return Jwts.builder()
            .issuer(props.issuer())
            .subject(user.getUsername())
            .claim("roles", roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.accessTtlSeconds())))
            .signWith(key, Jwts.SIG.HS256)
            .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(props.issuer())
            .subject(user.getUsername())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(props.refreshTtlSeconds())))
            .claim("typ", "refresh")
            .signWith(key, Jwts.SIG.HS256)
            .compact();
    }

    public Jws<Claims> parseAndValidate(String jwt) {
        return Jwts.parser()
            .verifyWith(key)
            .requireIssuer(props.issuer())
            .build()
            .parseSignedClaims(jwt);
    }

    public String extractUsername(String jwt) {
        return parseAndValidate(jwt).getPayload().getSubject();
    }

    public boolean isRefreshToken(String jwt) {
        Claims c = parseAndValidate(jwt).getPayload();
        Object typ = c.get("typ");
        return "refresh".equals(typ);
    }
}
