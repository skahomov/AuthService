package edu.pd.authservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BffControllerOtpTest {
    private MockHttpServletRequest request;
    private MockHttpSession session;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        session = new MockHttpSession();
        session.setAttribute("pendingUser", "admin");
        request.setSession(session);
        mvc = MockMvcBuilders.standaloneSetup(new BffController(request))
                .setControllerAdvice(new BffExceptionHandler()).build();
    }

    @Test
    void correctCodeReturnsProfileAndConsumesPendingLogin() throws Exception {
        verify("{\"otp\":\"111111\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("username").value("admin"))
                .andExpect(jsonPath("role").value("ADMIN"));
        assertNull(session.getAttribute("pendingUser"));
        verify("{\"otp\":\"111111\"}").andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {"000000", "123456", "111112", "222222"})
    void wrongCodeReturnsReadableErrorAndAllowsRetry(String otp) throws Exception {
        verify("{\"otp\":\"" + otp + "\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("status").value(401))
                .andExpect(jsonPath("detail").value("Неверный OTP-код"));
        assertEquals("admin", session.getAttribute("pendingUser"));
        verify("{\"otp\":\"111111\"}").andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "abc", "11111", "1111111", " 111111", "111111 ", "１１１１１１"})
    void invalidFormatReturnsReadableValidationError(String otp) throws Exception {
        verify("{\"otp\":\"" + otp + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value("Неверный OTP-код"))
                .andExpect(jsonPath("fieldErrors[0].field").value("otp"));
        assertEquals("admin", session.getAttribute("pendingUser"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"otp\":null}"})
    void missingCodeReturnsReadableValidationError(String body) throws Exception {
        verify(body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value("Неверный OTP-код"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "{broken"})
    void unreadableBodyReturnsReadableError(String body) throws Exception {
        verify(body).andExpect(status().isBadRequest())
                .andExpect(jsonPath("detail").value("Неверный OTP-код: передайте JSON с полем otp"));
    }

    @Test
    void correctCodeRequiresLogin() throws Exception {
        request.setSession(null);
        verify("{\"otp\":\"111111\"}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("detail").value("Сначала выполните login"));
    }

    private org.springframework.test.web.servlet.ResultActions verify(String body) throws Exception {
        return mvc.perform(post("/api/auth/otp/verify")
                .contentType("application/json").characterEncoding("UTF-8").content(body));
    }
}
