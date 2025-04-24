package hr.java.web.helloworld.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.java.web.helloworld.domain.RefreshToken;
import hr.java.web.helloworld.domain.UserInfo;
import hr.java.web.helloworld.dto.AuthRequestDTO;
import hr.java.web.helloworld.dto.RefreshTokenRequestDTO;
import hr.java.web.helloworld.service.JwtService;
import hr.java.web.helloworld.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @Test
    void testAuthenticateAndGetToken_Success() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO("testuser", "password");

        Authentication mockAuthentication = Mockito.mock(Authentication.class);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);

        when(jwtService.generateToken("testuser")).thenReturn("mockedAccessToken");

        UserInfo user = new UserInfo();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRoles(Collections.emptyList());

        RefreshToken refreshToken = RefreshToken.builder()
                .id(1)
                .token("mockedRefreshToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .userInfo(user)
                .build();

        when(refreshTokenService.createRefreshToken("testuser")).thenReturn(refreshToken);

        mockMvc.perform(post("/auth/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mockedAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockedRefreshToken"));
    }

    @Test
    void testAuthenticateAndGetToken_InvalidUser() throws Exception {
        AuthRequestDTO authRequest = new AuthRequestDTO("invalidUser", "wrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        RefreshTokenRequestDTO refreshRequest = new RefreshTokenRequestDTO("validRefreshToken");

        UserInfo user = new UserInfo();
        user.setId(2L);
        user.setUsername("refresher");
        user.setPassword("password");
        user.setRoles(Collections.emptyList());

        RefreshToken token = RefreshToken.builder()
                .id(2)
                .token("validRefreshToken")
                .expiryDate(Instant.now().plusSeconds(3600))
                .userInfo(user)
                .build();

        when(refreshTokenService.findByToken("validRefreshToken")).thenReturn(Optional.of(token));
        when(refreshTokenService.verifyExpiration(token)).thenReturn(token);
        when(jwtService.generateToken("refresher")).thenReturn("newAccessToken");

        mockMvc.perform(post("/auth/api/v1/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("validRefreshToken"));
    }
}
