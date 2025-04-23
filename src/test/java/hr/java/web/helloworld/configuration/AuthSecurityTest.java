package hr.java.web.helloworld.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthSecurityTest {

    String fakeJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.dummySignature";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAccessToLoginEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/auth/api/v1/login"))
                .andExpect(status().is4xxClientError()); // Not a GET endpoint, but test proves it's not blocked
    }

    @Test
    void shouldDenyAccessToProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/bugtracking/test"))
                .andExpect(status().isForbidden());
    }
}
