package edu.pd.authservice.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import edu.pd.authservice.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Instant;
import java.util.*;

@Service
public class TokenService implements JwtDecoder {
    private final JWKSet keys;
    private final RSAKey activeKey;
    private final String issuer;
    private final String audience;
    private final long ttl;

    public TokenService(@Value("${auth.jwt.key-file}") String keyFile,
                        @Value("${auth.jwt.active-kid:}") String activeKid,
                        @Value("${auth.jwt.issuer}") String issuer,
                        @Value("${auth.jwt.audience}") String audience,
                        @Value("${auth.jwt.ttl-seconds}") long ttl) throws Exception {
        this.issuer = issuer;
        this.audience = audience;
        this.ttl = ttl;
        if (ttl <= 0) throw new IllegalArgumentException("JWT TTL must be positive");
        Path path = Path.of(keyFile).toAbsolutePath();
        if (!Files.exists(path)) {
            Files.createDirectories(path.getParent());
            RSAKey generated = new RSAKeyGenerator(2048).keyID(UUID.randomUUID().toString())
                    .algorithm(JWSAlgorithm.RS256).generate();
            // The key is persisted; service restarts must not invalidate issued tokens.
            Path temporary = Files.createTempFile(path.getParent(), "key-", ".tmp");
            try {
                try {
                    Files.setPosixFilePermissions(temporary, PosixFilePermissions.fromString("rw-------"));
                } catch (UnsupportedOperationException ignored) { /* Windows: secure the directory with ACLs. */ }
                Files.writeString(temporary, new JWKSet(generated).toString(false));
                Files.move(temporary, path);
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
        keys = JWKSet.parse(Files.readString(path));
        activeKey = keys.getKeys().stream().filter(RSAKey.class::isInstance).map(RSAKey.class::cast)
                .filter(RSAKey::isPrivate).filter(key -> activeKid.isBlank() || activeKid.equals(key.getKeyID()))
                .findFirst().orElseThrow(() -> new IllegalStateException("No active private RSA signing key"));
        if (activeKey.getKeyID() == null || activeKey.size() < 2048)
            throw new IllegalStateException("Signing key must have kid and at least 2048 bits");
    }

    public Map<String, Object> publicJwks() {
        return keys.toPublicJWKSet().toJSONObject();
    }

    public String issue(AppUser user) {
        Instant now = Instant.now();
        List<String> permissions = user.getRole().getPermissions().stream().map(p -> p.getName()).sorted().toList();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().issuer(issuer).subject(user.getId().toString())
                .audience(audience).issueTime(Date.from(now)).notBeforeTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(ttl)))
                .claim("user_id", user.getId().toString()).claim("permissions", permissions)
                .claim("roles", List.of(user.getRole().getName().name())).build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT).keyID(activeKey.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(activeKey));
            return jwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Cannot sign JWT", exception);
        }
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.RS256.equals(jwt.getHeader().getAlgorithm())) throw new IllegalArgumentException();
            JWK key = keys.getKeyByKeyId(jwt.getHeader().getKeyID());
            if (!(key instanceof RSAKey rsa) || !jwt.verify(new RSASSAVerifier(rsa.toRSAPublicKey())))
                throw new IllegalArgumentException();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Instant now = Instant.now();
            if (!issuer.equals(claims.getIssuer()) || !claims.getAudience().contains(audience)
                    || claims.getExpirationTime() == null || !now.isBefore(claims.getExpirationTime().toInstant())
                    || claims.getNotBeforeTime() == null || now.isBefore(claims.getNotBeforeTime().toInstant()))
                throw new IllegalArgumentException();
            Object userId = claims.getClaim("user_id");
            UUID.fromString(userId == null ? claims.getSubject() : (String) userId);
            return Jwt.withTokenValue(token).headers(h -> h.putAll(jwt.getHeader().toJSONObject()))
                    .claims(c -> c.putAll(claims.getClaims()))
                    .issuedAt(claims.getIssueTime() == null ? null : claims.getIssueTime().toInstant())
                    .notBefore(claims.getNotBeforeTime().toInstant())
                    .expiresAt(claims.getExpirationTime().toInstant()).build();
        } catch (Exception exception) {
            throw new BadJwtException("Недействительный JWT-токен", exception);
        }
    }
}
