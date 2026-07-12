package com.cloudbank.digitalbanking.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected com.cloudbank.digitalbanking.account.repository.AccountRepository accountRepository;

    @Autowired
    protected com.cloudbank.digitalbanking.customer.repository.CustomerRepository customerRepository;

    @Autowired
    protected com.cloudbank.digitalbanking.beneficiary.repository.BeneficiaryRepository beneficiaryRepository;

    @Autowired
    protected com.cloudbank.digitalbanking.transaction.repository.TransactionRepository transactionRepository;

    @Autowired
    protected com.cloudbank.digitalbanking.notification.repository.NotificationRepository notificationRepository;

    @Autowired
    protected com.cloudbank.digitalbanking.audit.repository.AuditRepository auditRepository;

    @Autowired
    protected com.cloudbank.digitalbanking.payment.repository.PaymentRepository paymentRepository;

    protected Map<String, Object> buildCustomerPayload(String emailSuffix) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("firstName", "Alex");
        payload.put("lastName", "Mercier");
        payload.put("email", "alex.mercier." + emailSuffix + "@fictional-mail.demo");
        payload.put("phoneNumber", "+14165559001");
        payload.put("dateOfBirth", LocalDate.of(1991, 4, 18).toString());
        payload.put("addressLine1", "42 Demo Street");
        payload.put("city", "Ottawa");
        payload.put("province", "Ontario");
        payload.put("postalCode", "K1A0B1");
        return payload;
    }

    @WithMockUser(roles = "CUSTOMER")
    protected ResultActions createCustomer(String emailSuffix) throws Exception {
        return mockMvc.perform(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildCustomerPayload(emailSuffix))));
    }

    @WithMockUser(roles = "CUSTOMER")
    protected UUID createCustomerAndReturnId(String emailSuffix) throws Exception {
        String response = createCustomer(emailSuffix)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
    }

    @WithMockUser(roles = "CUSTOMER")
    protected UUID createAccount(UUID customerId, String accountType) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId.toString());
        payload.put("accountType", accountType);

        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
    }

    @WithMockUser(roles = "CUSTOMER")
    protected UUID createAccountWithDailyLimit(UUID customerId, String accountType, BigDecimal dailyLimit)
            throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId.toString());
        payload.put("accountType", accountType);
        payload.put("dailyTransferLimit", dailyLimit);

        String response = mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
    }

    protected void fundAccount(UUID accountId, BigDecimal amount) {
        var account = accountRepository.findById(accountId).orElseThrow();
        account.setCurrentBalance(amount);
        account.setAvailableBalance(amount);
        accountRepository.save(account);
    }

    @WithMockUser(roles = "CUSTOMER")
    protected UUID createBeneficiary(UUID customerId, String accountNumber, String nickname) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("customerId", customerId.toString());
        payload.put("beneficiaryName", "Demo Beneficiary " + nickname);
        payload.put("beneficiaryAccountNumber", accountNumber);
        payload.put("bankName", "Fictional Canadian Bank");
        payload.put("transitNumber", "12345");
        payload.put("institutionNumber", "001");
        payload.put("nickname", nickname);

        String response = mockMvc.perform(post("/beneficiaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).path("data").path("id").asText());
    }
}
