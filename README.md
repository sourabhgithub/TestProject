package com.example.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev") // Set the profile for the environment you want to test
public class IpAddressFilterDevTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAllowRequestFromAllowedIpInDev() throws Exception {
        // Simulate request from an allowed IP address in the Dev environment
        mockMvc.perform(get("/cash/settle/test")
                        .header("X-Forwarded-For", "192.168.1.10")) // Allowed IP in Dev
                .andExpect(status().isOk()); // Expect OK status
    }

    @Test
    public void shouldRejectRequestFromDisallowedIpInDev() throws Exception {
        // Simulate request from a disallowed IP address
        mockMvc.perform(get("/cash/settle/test")
                        .header("X-Forwarded-For", "203.0.113.10")) // Disallowed IP
                .andExpect(status().isForbidden()); // Expect 403 Forbidden status
    }
}

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod") // Set the profile for the environment you want to test
public class IpAddressFilterProdTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldAllowRequestFromAllowedIpInProd() throws Exception {
        // Simulate request from an allowed IP address in the Prod environment
        mockMvc.perform(get("/cash/settle/test")
                        .header("X-Forwarded-For", "10.0.1.10")) // Allowed IP in Prod
                .andExpect(status().isOk()); // Expect OK status
    }

    @Test
    public void shouldRejectRequestFromDisallowedIpInProd() throws Exception {
        // Simulate request from a disallowed IP address
        mockMvc.perform(get("/cash/settle/test")
                        .header("X-Forwarded-For", "203.0.113.10")) // Disallowed IP
                .andExpect(status().isForbidden()); // Expect 403 Forbidden status
    }
}
