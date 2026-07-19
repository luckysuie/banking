package com.cloudbank.digitalbanking.integration;

import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;
import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;
import com.cloudbank.digitalbanking.support.IntegrationTestSupport;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BankingApiIntegrationTest extends IntegrationTestSupport {

    @Nested
    @DisplayName("Customer APIs")
    class CustomerApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void createCustomer_shouldPersistCustomerWithGeneratedNumber() throws Exception {
            // Arrange
            Map<String, Object> payload = buildCustomerPayload("create");

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.firstName").value("Alex"))
                    .andExpect(jsonPath("$.data.lastName").value("Mercier"))
                    .andExpect(jsonPath("$.data.email").value("alex.mercier.create@fictional-mail.demo"))
                    .andExpect(jsonPath("$.data.customerNumber").value(org.hamcrest.Matchers.startsWith("CUS-")))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            assertThat(customerRepository.findByEmail("alex.mercier.create@fictional-mail.demo"))
                    .isPresent();
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void createCustomer_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
            // Arrange
            String email = "alex.mercier.duplicate@fictional-mail.demo";
            Map<String, Object> payload = buildCustomerPayload("duplicate");
            createCustomer("duplicate");

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_RESOURCE"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(email)));

            assertThat(customerRepository.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Account APIs")
    class AccountApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void createAccount_shouldOpenActiveAccountForExistingCustomer() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("account");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("customerId", customerId.toString());
            payload.put("accountType", "CHEQUING");

            // Act & Assert
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.data.accountType").value("CHEQUING"))
                    .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                    .andExpect(jsonPath("$.data.currency").value("CAD"))
                    .andExpect(jsonPath("$.data.currentBalance").value(0))
                    .andExpect(jsonPath("$.data.accountNumber").value(org.hamcrest.Matchers.startsWith("CB")));

            assertThat(accountRepository.findByCustomerId(customerId)).hasSize(1);
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void getAccountsByCustomer_shouldReturnAllCustomerAccounts() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("accounts-list");
            createAccount(customerId, "CHEQUING");
            createAccount(customerId, "SAVINGS");

            // Act & Assert
            mockMvc.perform(get("/api/accounts/customer/{customerId}", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[*].accountType")
                            .value(org.hamcrest.Matchers.containsInAnyOrder("CHEQUING", "SAVINGS")));
        }
    }

    @Nested
    @DisplayName("Beneficiary APIs")
    class BeneficiaryApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void createBeneficiary_shouldPersistActiveBeneficiaryForCustomer() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("beneficiary");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("customerId", customerId.toString());
            payload.put("beneficiaryName", "Jordan Park");
            payload.put("beneficiaryAccountNumber", "9001234567");
            payload.put("bankName", "Fictional Canadian Bank");
            payload.put("transitNumber", "54321");
            payload.put("institutionNumber", "010");
            payload.put("nickname", "Jordan");

            // Act & Assert
            mockMvc.perform(post("/api/beneficiaries")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.customerId").value(customerId.toString()))
                    .andExpect(jsonPath("$.data.beneficiaryName").value("Jordan Park"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            assertThat(beneficiaryRepository.findByCustomerId(customerId)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Payment and transfer APIs")
    class PaymentApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void processFundTransfer_shouldCompleteTransferAndReduceSourceBalance() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("transfer-success");
            UUID sourceAccountId = createAccount(customerId, "CHEQUING");
            fundAccount(sourceAccountId, new BigDecimal("500.00"));
            UUID beneficiaryId = createBeneficiary(customerId, "8001234567", "payee");

            Map<String, Object> transfer = new LinkedHashMap<>();
            transfer.put("sourceAccountId", sourceAccountId.toString());
            transfer.put("beneficiaryId", beneficiaryId.toString());
            transfer.put("amount", "150.00");
            transfer.put("description", "Demo rent payment");
            transfer.put("idempotencyKey", "idem-success-001");

            // Act & Assert
            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transfer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.amount").value(150.00))
                    .andExpect(jsonPath("$.data.idempotencyKey").value("idem-success-001"));

            var updatedAccount = accountRepository.findById(sourceAccountId).orElseThrow();
            assertThat(updatedAccount.getAvailableBalance()).isEqualByComparingTo("350.00");
            assertThat(paymentRepository.findByIdempotencyKey("idem-success-001"))
                    .isPresent()
                    .get()
                    .extracting(com.cloudbank.digitalbanking.payment.entity.Payment::getStatus)
                    .isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void processFundTransfer_shouldReturnBadRequest_whenInsufficientBalance() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("transfer-insufficient");
            UUID sourceAccountId = createAccount(customerId, "CHEQUING");
            fundAccount(sourceAccountId, new BigDecimal("50.00"));
            UUID beneficiaryId = createBeneficiary(customerId, "8002345678", "payee2");

            Map<String, Object> transfer = buildTransferRequest(
                    sourceAccountId, beneficiaryId, "200.00", "idem-insufficient-001");

            // Act & Assert
            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transfer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_BALANCE"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient")));

            assertThat(accountRepository.findById(sourceAccountId).orElseThrow().getAvailableBalance())
                    .isEqualByComparingTo("50.00");
            assertThat(paymentRepository.findByIdempotencyKey("idem-insufficient-001")).isEmpty();
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void processFundTransfer_shouldReturnBadRequest_whenTransferExceedsDailyLimit() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("transfer-limit");
            UUID sourceAccountId = createAccountWithDailyLimit(customerId, "CHEQUING", new BigDecimal("100.00"));
            fundAccount(sourceAccountId, new BigDecimal("1000.00"));
            UUID beneficiaryId = createBeneficiary(customerId, "8003456789", "payee3");

            Map<String, Object> transfer = buildTransferRequest(
                    sourceAccountId, beneficiaryId, "150.00", "idem-limit-001");

            // Act & Assert
            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transfer)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_TRANSFER"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("daily transfer limit")));

            assertThat(accountRepository.findById(sourceAccountId).orElseThrow().getAvailableBalance())
                    .isEqualByComparingTo("1000.00");
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void processFundTransfer_shouldReturnExistingPayment_whenIdempotencyKeyIsReused() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("transfer-idempotent");
            UUID sourceAccountId = createAccount(customerId, "CHEQUING");
            fundAccount(sourceAccountId, new BigDecimal("500.00"));
            UUID beneficiaryId = createBeneficiary(customerId, "8004567890", "payee4");

            Map<String, Object> transfer = buildTransferRequest(
                    sourceAccountId, beneficiaryId, "75.00", "idem-repeat-001");

            String firstResponse = mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transfer)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String firstPaymentId = objectMapper.readTree(firstResponse).path("data").path("id").asText();

            // Act & Assert
            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transfer)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id").value(firstPaymentId))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));

            assertThat(paymentRepository.findAll()).hasSize(1);
            assertThat(accountRepository.findById(sourceAccountId).orElseThrow().getAvailableBalance())
                    .isEqualByComparingTo("425.00");
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void processFundTransfer_shouldReturnConflict_whenIdempotencyKeyReusedWithDifferentAmount() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("transfer-idempotent-conflict");
            UUID sourceAccountId = createAccount(customerId, "CHEQUING");
            fundAccount(sourceAccountId, new BigDecimal("500.00"));
            UUID beneficiaryId = createBeneficiary(customerId, "8005678901", "payee5");

            Map<String, Object> originalTransfer = buildTransferRequest(
                    sourceAccountId, beneficiaryId, "60.00", "idem-conflict-001");
            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(originalTransfer)))
                    .andExpect(status().isCreated());

            Map<String, Object> conflictingTransfer = buildTransferRequest(
                    sourceAccountId, beneficiaryId, "90.00", "idem-conflict-001");

            // Act & Assert
            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(conflictingTransfer)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("DUPLICATE_PAYMENT"))
                    .andExpect(jsonPath("$.message")
                            .value(org.hamcrest.Matchers.containsString("different payment details")));
        }

        private Map<String, Object> buildTransferRequest(
                UUID sourceAccountId,
                UUID beneficiaryId,
                String amount,
                String idempotencyKey) {
            Map<String, Object> transfer = new LinkedHashMap<>();
            transfer.put("sourceAccountId", sourceAccountId.toString());
            transfer.put("beneficiaryId", beneficiaryId.toString());
            transfer.put("amount", amount);
            transfer.put("idempotencyKey", idempotencyKey);
            return transfer;
        }
    }

    @Nested
    @DisplayName("Transaction history APIs")
    class TransactionApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void getTransactionsByAccount_shouldReturnDebitAfterSuccessfulTransfer() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("txn-history");
            UUID sourceAccountId = createAccount(customerId, "CHEQUING");
            fundAccount(sourceAccountId, new BigDecimal("300.00"));
            UUID beneficiaryId = createBeneficiary(customerId, "8006789012", "payee6");

            Map<String, Object> transfer = new LinkedHashMap<>();
            transfer.put("sourceAccountId", sourceAccountId.toString());
            transfer.put("beneficiaryId", beneficiaryId.toString());
            transfer.put("amount", "45.00");
            transfer.put("idempotencyKey", "idem-txn-001");

            mockMvc.perform(post("/api/payments/transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transfer)))
                    .andExpect(status().isCreated());

            // Act & Assert
            mockMvc.perform(get("/api/transactions/account/{accountId}", sourceAccountId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].transactionType").value("DEBIT"))
                    .andExpect(jsonPath("$.data.content[0].amount").value(45.00))
                    .andExpect(jsonPath("$.data.content[0].status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.content[0].balanceBefore").value(300.00))
                    .andExpect(jsonPath("$.data.content[0].balanceAfter").value(255.00));

            assertThat(transactionRepository.findAll()).hasSize(1)
                    .first()
                    .satisfies(transaction -> {
                        assertThat(transaction.getTransactionType()).isEqualTo(TransactionType.DEBIT);
                        assertThat(transaction.getAccountId()).isEqualTo(sourceAccountId);
                    });
        }
    }

    @Nested
    @DisplayName("Notification APIs")
    class NotificationApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void createAccount_shouldCreateAccountCreatedNotification() throws Exception {
            // Arrange
            UUID customerId = createCustomerAndReturnId("notification-account");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("customerId", customerId.toString());
            payload.put("accountType", "SAVINGS");

            // Act
            mockMvc.perform(post("/api/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated());

            // Assert
            mockMvc.perform(get("/api/notifications/customer/{customerId}", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].type").value("ACCOUNT_CREATED"))
                    .andExpect(jsonPath("$.data[0].status").value("SENT"))
                    .andExpect(jsonPath("$.data[0].read").value(false))
                    .andExpect(jsonPath("$.data[0].message")
                            .value(org.hamcrest.Matchers.containsString("has been created")));

            assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId))
                    .isNotEmpty()
                    .first()
                    .satisfies(notification -> assertThat(notification.getType())
                            .isEqualTo(NotificationType.ACCOUNT_CREATED));
        }
    }

    @Nested
    @DisplayName("Audit APIs")
    class AuditApiTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        void createCustomer_shouldRecordCustomerCreatedAuditEvent() throws Exception {
            // Arrange
            Map<String, Object> payload = buildCustomerPayload("audit");

            // Act
            String response = mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            String customerId = objectMapper.readTree(response).path("data").path("id").asText();

            // Assert
            mockMvc.perform(get("/api/audit-events")
                            .param("action", "CUSTOMER_CREATED")
                            .param("actorId", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$.data.content[0].action").value("CUSTOMER_CREATED"))
                    .andExpect(jsonPath("$.data.content[0].resourceType").value("CUSTOMER"))
                    .andExpect(jsonPath("$.data.content[0].result").value("SUCCESS"));

            assertThat(auditRepository.findAll())
                    .anySatisfy(event -> {
                        assertThat(event.getAction()).isEqualTo(AuditAction.CUSTOMER_CREATED);
                        assertThat(event.getActorId()).isEqualTo(customerId);
                    });
        }
    }

    @Nested
    @DisplayName("Validation and security APIs")
    class ValidationAndSecurityApiTests {

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void createCustomer_shouldReturnValidationErrors_whenRequiredFieldsAreInvalid() throws Exception {
            // Arrange
            Map<String, Object> invalidPayload = new LinkedHashMap<>();
            invalidPayload.put("firstName", "");
            invalidPayload.put("lastName", "Mercier");
            invalidPayload.put("email", "not-an-email");
            invalidPayload.put("phoneNumber", "abc");
            invalidPayload.put("dateOfBirth", "2030-01-01");
            invalidPayload.put("addressLine1", "");
            invalidPayload.put("city", "");
            invalidPayload.put("province", "");
            invalidPayload.put("postalCode", "INVALID");

            // Act & Assert
            mockMvc.perform(post("/api/customers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidPayload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.fieldErrors").isArray())
                    .andExpect(jsonPath("$.fieldErrors[?(@.field == 'email')].message").exists())
                    .andExpect(jsonPath("$.fieldErrors[?(@.field == 'firstName')].message").exists());
        }

        @Test
        void protectedEndpoint_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/customers"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"))
                    .andExpect(jsonPath("$.correlationId").exists());
        }

        @Test
        @WithMockUser(roles = "CUSTOMER")
        void auditEndpoint_withCustomerRole_shouldReturnForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/audit-events"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
        }
    }
}
