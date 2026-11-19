package edu.pd.authservice;

import edu.pd.authservice.service.TokenService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class JwksController {
    private final TokenService tokens;

    public JwksController(TokenService tokens) {
        this.tokens = tokens;
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return tokens.publicJwks();
    }
}
