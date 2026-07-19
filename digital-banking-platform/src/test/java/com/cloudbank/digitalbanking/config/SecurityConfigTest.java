package com.cloudbank.digitalbanking.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void protectedEndpoint_withoutAuth_shouldReturn401Json() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.correlationId").exists());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void auditEndpoint_withCustomerRole_shouldReturn403Json() throws Exception {
        mockMvc.perform(get("/api/audit-events"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void accountStatusUpdate_withCustomerRole_shouldReturn403() throws Exception {
        mockMvc.perform(patch("/api/accounts/00000000-0000-0000-0000-000000000001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountStatus\":\"FROZEN\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void listCustomers_withCustomerRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listCustomers_withAdminRole_shouldBeAllowed() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUi_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                        .string("Location", org.hamcrest.Matchers.containsString("/swagger-ui/index.html")));
    }
}
