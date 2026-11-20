package edu.pd.authservice;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.*;
import edu.pd.authservice.entity.*;
import edu.pd.authservice.service.TokenService;
import edu.pd.contract.dto.UserRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.oauth2.jwt.BadJwtException;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
    @TempDir
    Path directory;
    TokenService service;
    RSAKey key;

    @BeforeEach
    void setup() throws Exception {
        service = new TokenService(directory.resolve("keys.json").toString(), "", "http://auth.test", "hcc-logic-service", 900);
        key = (RSAKey) JWKSet.parse(Files.readString(directory.resolve("keys.json"))).getKeys().getFirst();
    }

    @Test
    void issuedClaimsMatchHccAndKeySurvivesRestart() throws Exception {
        var user = new AppUser("user", "hash", "Test User", "+79000000000", new Role(UserRole.TECHNICIAN, List.of(new Permission("checklist:read"))));
        String token = service.issue(user);
        var restarted = new TokenService(directory.resolve("keys.json").toString(), "", "http://auth.test", "hcc-logic-service", 900);
        var jwt = restarted.decode(token);
        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals(user.getId().toString(), jwt.getClaimAsString("user_id"));
        assertEquals(List.of("hcc-logic-service"), jwt.getAudience());
        assertEquals(List.of("TECHNICIAN"), jwt.getClaimAsStringList("roles"));
        assertEquals(List.of("checklist:read"), jwt.getClaimAsStringList("permissions"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"issuer", "audience", "expired", "future", "missing-exp", "missing-nbf", "invalid-uuid", "missing-identity", "unknown-key", "wrong-signature"})
    void invalidTokensAreRejected(String scenario) throws Exception {
        assertThrows(BadJwtException.class, () -> service.decode(custom(scenario)));
    }

    @Test
    void uuidSubjectIsUsedWhenUserIdMissing() throws Exception {
        assertNotNull(service.decode(custom("fallback")));
    }

    @Test
    void retainedPublicKeyAllowsRotation() throws Exception {
        String old = custom("valid");
        RSAKey next = new RSAKeyGenerator(2048).keyID("next").generate();
        Files.writeString(directory.resolve("keys.json"), new JWKSet(List.of(next, key.toPublicJWK())).toString(false));
        var rotated = new TokenService(directory.resolve("keys.json").toString(), "next", "http://auth.test", "hcc-logic-service", 900);
        assertNotNull(rotated.decode(old));
        var user = new AppUser("user", "hash", "Test User", "+79000000000", new Role(UserRole.ADMIN, List.of()));
        assertEquals("next", SignedJWT.parse(rotated.issue(user)).getHeader().getKeyID());
    }

    private String custom(String scenario) throws Exception {
        Instant now = Instant.now();
        String id = UUID.randomUUID().toString();
        var claims = new JWTClaimsSet.Builder().issuer(scenario.equals("issuer") ? "wrong" : "http://auth.test")
                .audience(scenario.equals("audience") ? "other" : "hcc-logic-service");
        if (!scenario.equals("missing-identity")) claims.subject(id);
        if (!scenario.equals("fallback") && !scenario.equals("missing-identity"))
            claims.claim("user_id", scenario.equals("invalid-uuid") ? "bad" : id);
        if (!scenario.equals("missing-exp"))
            claims.expirationTime(Date.from(now.plusSeconds(scenario.equals("expired") ? -120 : 900)));
        if (!scenario.equals("missing-nbf"))
            claims.notBeforeTime(Date.from(now.plusSeconds(scenario.equals("future") ? 120 : -1)));
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(scenario.equals("unknown-key") ? "other" : key.getKeyID()).build(), claims.build());
        jwt.sign(new RSASSASigner(scenario.equals("wrong-signature") ? new RSAKeyGenerator(2048).generate() : key));
        return jwt.serialize();
    }
}
