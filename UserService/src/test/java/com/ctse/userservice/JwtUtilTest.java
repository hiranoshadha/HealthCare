package com.ctse.userservice;

import com.ctse.userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "MySecureJWTSecretKeyFor256BitHSAlgorithm12345");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 86400000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("johndoe", "PATIENT");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("johndoe", "PATIENT");
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("invalid.token.value")).isFalse();
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("johndoe", "PATIENT");
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("johndoe");
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("johndoe", "PATIENT");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("PATIENT");
    }
}
