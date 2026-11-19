package edu.pd.authservice;

import com.fasterxml.jackson.databind.*;
import edu.pd.authservice.entity.AppUser;
import edu.pd.authservice.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:auth;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "auth.jwt.issuer=http://auth.test", "auth.seed-demo-users=true"})
class BffControllerOtpTest {
    private static final Path KEY_FILE;
    static {
        try { KEY_FILE = Files.createTempDirectory("auth-test-").resolve("keys.json"); }
        catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource
    static void keys(DynamicPropertyRegistry registry) { registry.add("auth.jwt.key-file", KEY_FILE::toString); }
    @Value("${local.server.port}") private int port;
    @Autowired private UserRepository users;
    private final ObjectMapper json = new ObjectMapper();
    private HttpClient client;
    @BeforeEach void client() {
        client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
    }
    private HttpResponse<String> post(String path, String body, String token) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
    private void login(String name) throws Exception {
        assertEquals(200, post("/api/auth/login", "{\"username\":\"" + name + "\",\"password\":\"" + name + "\"}", null).statusCode());
    }
    private HttpResponse<String> otp(String value) throws Exception {
        return post("/api/auth/otp/verify", json.writeValueAsString(Map.of("otp", value)), null);
    }
    private String token(HttpResponse<String> response) { return response.headers().firstValue("Authorization").orElseThrow().substring(7); }

    @Test void loginUsesDatabaseAndPasswordHash() throws Exception {
        var user = users.findByUsername("admin").orElseThrow();
        assertTrue(user.getPasswordHash().startsWith("$2"));
        assertNotEquals("admin", user.getPasswordHash());
        assertEquals(401, post("/api/auth/login", "{\"username\":\"admin\",\"password\":\"wrong\"}", null).statusCode());
        assertEquals(401, post("/api/auth/login", "{\"username\":\"missing\",\"password\":\"missing\"}", null).statusCode());
    }
    @Test void successfulOtpIssuesVerifiableTokenOnce() throws Exception {
        login("admin");
        var result = otp("111111"); assertEquals(200, result.statusCode(), result.body());
        assertEquals("ADMIN", json.readTree(result.body()).get("role").asText());
        String token = token(result);
        var verified = post("/api/auth/verify", json.writeValueAsString(Map.of("token", token)), token);
        assertEquals(200, verified.statusCode(), verified.body());
        assertEquals(json.readTree(result.body()).get("id"), json.readTree(verified.body()).get("userId"));
        assertEquals(401, otp("111111").statusCode());
        assertEquals(401, post("/api/auth/verify", "{\"token\":\"broken\"}", token).statusCode());
    }
    @ParameterizedTest @ValueSource(strings = {"000000", "123456", "111112"})
    void wrongCodeAllowsRetry(String code) throws Exception {
        login("admin"); var result = otp(code);
        assertEquals(401, result.statusCode());
        assertEquals("Неверный OTP-код", json.readTree(result.body()).get("detail").asText());
        assertEquals(200, otp("111111").statusCode());
    }
    @ParameterizedTest @ValueSource(strings = {"", " ", "abc", "11111", "1111111", "111111 "})
    void invalidOtpFormatHasReadableError(String code) throws Exception {
        login("admin"); var result = otp(code);
        assertEquals(400, result.statusCode(), result.body());
        assertTrue(json.readTree(result.body()).get("detail").asText().contains("Неверный OTP-код"));
    }
    @Test void noPendingLoginIsUnauthorized() throws Exception { assertEquals(401, otp("111111").statusCode()); }
    @Test void permissionsComeFromDatabaseAndAdminHasFullAccess() throws Exception {
        login("technician"); String t = token(otp("111111"));
        String id = users.findByUsername("technician").orElseThrow().getId().toString();
        for (String permission : List.of("checklist:read", "checklist:archive", "unknown")) {
            var result = post("/api/auth/permissions/check", json.writeValueAsString(Map.of("userId", id, "permission", permission)), t);
            assertEquals(200, result.statusCode(), result.body());
            assertEquals(permission.equals("checklist:read"), json.readTree(result.body()).get("allowed").asBoolean());
        }
        var missing = post("/api/auth/permissions/check", json.writeValueAsString(Map.of("userId", UUID.randomUUID(), "permission", "checklist:read")), t);
        assertFalse(json.readTree(missing.body()).get("allowed").asBoolean());
        String adminId = users.findByUsername("admin").orElseThrow().getId().toString();
        var admin = post("/api/auth/permissions/check", json.writeValueAsString(Map.of("userId", adminId, "permission", "checklist:archive")), t);
        assertTrue(json.readTree(admin.body()).get("allowed").asBoolean());
        assertEquals(401, post("/api/auth/permissions/check", "{}", null).statusCode());
        assertEquals(400, post("/api/auth/permissions/check", "{}", t).statusCode());
    }
    @Test void disabledUserCannotLoginOrPassVerify() throws Exception {
        login("supervisor"); String t = token(otp("111111"));
        AppUser user = users.findByUsername("supervisor").orElseThrow();
        user.setEnabled(false); users.saveAndFlush(user);
        try {
            assertEquals(401, post("/api/auth/login", "{\"username\":\"supervisor\",\"password\":\"supervisor\"}", null).statusCode());
            assertEquals(401, post("/api/auth/verify", json.writeValueAsString(Map.of("token", t)), t).statusCode());
        } finally { user.setEnabled(true); users.saveAndFlush(user); }
    }
    @Test void swaggerAndPublicJwksAreAvailable() throws Exception {
        for (String path : List.of("/v3/api-docs", "/swagger-ui/index.html", "/.well-known/jwks.json")) {
            var result = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, result.statusCode(), result.body());
            if (path.endsWith("jwks.json")) {
                JsonNode key = json.readTree(result.body()).get("keys").get(0);
                assertEquals("RSA", key.get("kty").asText()); assertNull(key.get("d")); assertNotNull(key.get("kid"));
            }
        }
    }
}
