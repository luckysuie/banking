package com.cloudbank.digitalbanking.config;

import com.cloudbank.digitalbanking.account.entity.Account;
import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.account.enums.AccountType;
import com.cloudbank.digitalbanking.account.repository.AccountRepository;
import com.cloudbank.digitalbanking.audit.constants.AuditResourceType;
import com.cloudbank.digitalbanking.audit.entity.AuditEvent;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.repository.AuditRepository;
import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import com.cloudbank.digitalbanking.beneficiary.repository.BeneficiaryRepository;
import com.cloudbank.digitalbanking.common.constants.BankingConstants;
import com.cloudbank.digitalbanking.customer.entity.Customer;
import com.cloudbank.digitalbanking.customer.enums.CustomerStatus;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.notification.entity.Notification;
import com.cloudbank.digitalbanking.notification.enums.NotificationStatus;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;
import com.cloudbank.digitalbanking.notification.repository.NotificationRepository;
import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import com.cloudbank.digitalbanking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.data", name = "initialize", havingValue = "true", matchIfMissing = true)
public class LocalDataInitializer implements CommandLineRunner {

    static final String DEMO_MARKER_CUSTOMER_NUMBER = "CUS-DEMO000001";
    static final String DEMO_CORRELATION_ID = "demo-seed-2026";

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final AuditRepository auditRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (customerRepository.findByCustomerNumber(DEMO_MARKER_CUSTOMER_NUMBER).isPresent()) {
            log.info("Demo banking data already present; skipping local data initialization.");
            return;
        }

        log.info("Initializing fictional Canadian demo banking data...");

        List<Customer> customers = seedCustomers();
        List<Account> accounts = seedAccounts(customers);
        seedBeneficiaries(customers);
        seedTransactions(accounts);
        seedNotifications(customers, accounts);
        seedAuditEvents(customers, accounts);

        log.info("Demo banking data initialization completed for {} customers.", customers.size());
    }

    private List<Customer> seedCustomers() {
        List<Customer> customers = List.of(
                buildCustomer("CUS-DEMO000001", "Emma", "Thompson",
                        "emma.thompson@fictional-mail.demo", "+14165550101",
                        LocalDate.of(1988, 3, 14), "100 Maple Leaf Way", "Toronto", "Ontario", "M5V2T6"),
                buildCustomer("CUS-DEMO000002", "Noah", "Tremblay",
                        "noah.tremblay@fictional-mail.demo", "+15145550202",
                        LocalDate.of(1992, 7, 22), "250 Rue de la Montagne", "Montreal", "Quebec", "H3G1Z1"),
                buildCustomer("CUS-DEMO000003", "Priya", "Singh",
                        "priya.singh@fictional-mail.demo", "+16045550303",
                        LocalDate.of(1990, 11, 5), "88 Pacific View Dr", "Vancouver", "British Columbia", "V6B4N8")
        );
        return customerRepository.saveAll(customers);
    }

    private Customer buildCustomer(
            String customerNumber,
            String firstName,
            String lastName,
            String email,
            String phone,
            LocalDate dateOfBirth,
            String address,
            String city,
            String province,
            String postalCode) {
        Customer customer = new Customer();
        customer.setCustomerNumber(customerNumber);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail(email);
        customer.setPhoneNumber(phone);
        customer.setDateOfBirth(dateOfBirth);
        customer.setAddressLine1(address);
        customer.setCity(city);
        customer.setProvince(province);
        customer.setPostalCode(postalCode);
        customer.setCountry(BankingConstants.DEFAULT_COUNTRY);
        customer.setStatus(CustomerStatus.ACTIVE);
        return customer;
    }

    private List<Account> seedAccounts(List<Customer> customers) {
        List<Account> accounts = new ArrayList<>();

        accounts.add(buildAccount(customers.get(0).getId(), "CB1001001001", AccountType.CHEQUING,
                new BigDecimal("4250.75"), new BigDecimal("4250.75")));
        accounts.add(buildAccount(customers.get(0).getId(), "CB1001001002", AccountType.SAVINGS,
                new BigDecimal("12800.00"), new BigDecimal("12800.00")));

        accounts.add(buildAccount(customers.get(1).getId(), "CB2002002001", AccountType.CHEQUING,
                new BigDecimal("1890.50"), new BigDecimal("1890.50")));
        accounts.add(buildAccount(customers.get(1).getId(), "CB2002002002", AccountType.SAVINGS,
                new BigDecimal("6450.25"), new BigDecimal("6450.25")));

        accounts.add(buildAccount(customers.get(2).getId(), "CB3003003001", AccountType.CHEQUING,
                new BigDecimal("8120.00"), new BigDecimal("8120.00")));
        accounts.add(buildAccount(customers.get(2).getId(), "CB3003003002", AccountType.SAVINGS,
                new BigDecimal("22500.00"), new BigDecimal("22500.00")));

        return accountRepository.saveAll(accounts);
    }

    private Account buildAccount(
            UUID customerId,
            String accountNumber,
            AccountType accountType,
            BigDecimal currentBalance,
            BigDecimal availableBalance) {
        Account account = new Account();
        account.setCustomerId(customerId);
        account.setAccountNumber(accountNumber);
        account.setAccountType(accountType);
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCurrency(BankingConstants.DEFAULT_ACCOUNT_CURRENCY);
        account.setCurrentBalance(currentBalance);
        account.setAvailableBalance(availableBalance);
        account.setDailyTransferLimit(BankingConstants.DEFAULT_DAILY_TRANSFER_LIMIT);
        return account;
    }

    private void seedBeneficiaries(List<Customer> customers) {
        List<Beneficiary> beneficiaries = List.of(
                buildBeneficiary(customers.get(0).getId(), "Lucas Chen", "9001001001",
                        "Northern Lights Credit Union", "10001", "828", "Lucas"),
                buildBeneficiary(customers.get(0).getId(), "Maple Utilities Co", "9001001002",
                        "CloudBank Demo Branch", "20002", "001", "Utilities"),
                buildBeneficiary(customers.get(1).getId(), "Sophie Labelle", "9002002001",
                        "Laurentian Demo Bank", "30003", "614", "Sophie"),
                buildBeneficiary(customers.get(1).getId(), "Quebec Art Supplies", "9002002002",
                        "St-Laurent Financial", "40004", "815", "ArtShop"),
                buildBeneficiary(customers.get(2).getId(), "Jordan Park", "9003003001",
                        "Pacific Horizon Bank", "50005", "730", "Jordan"),
                buildBeneficiary(customers.get(2).getId(), "Cascadia Property Mgmt", "9003003002",
                        "West Coast Savings", "60006", "010", "Rent")
        );
        beneficiaryRepository.saveAll(beneficiaries);
    }

    private Beneficiary buildBeneficiary(
            UUID customerId,
            String name,
            String accountNumber,
            String bankName,
            String transit,
            String institution,
            String nickname) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomerId(customerId);
        beneficiary.setBeneficiaryName(name);
        beneficiary.setBeneficiaryAccountNumber(accountNumber);
        beneficiary.setBankName(bankName);
        beneficiary.setTransitNumber(transit);
        beneficiary.setInstitutionNumber(institution);
        beneficiary.setNickname(nickname);
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        return beneficiary;
    }

    private void seedTransactions(List<Account> accounts) {
        List<Transaction> transactions = new ArrayList<>();

        Account emmaChequing = accounts.get(0);
        transactions.add(buildTransaction("TXN-DEMO000001", emmaChequing.getId(), null,
                TransactionType.CREDIT, new BigDecimal("5000.00"), "Fictional payroll deposit",
                new BigDecimal("0.00"), new BigDecimal("5000.00")));
        transactions.add(buildTransaction("TXN-DEMO000002", emmaChequing.getId(), null,
                TransactionType.DEBIT, new BigDecimal("425.25"), "Demo grocery purchase",
                new BigDecimal("5000.00"), new BigDecimal("4574.75")));
        transactions.add(buildTransaction("TXN-DEMO000003", emmaChequing.getId(), null,
                TransactionType.DEBIT, new BigDecimal("324.00"), "Demo online bill payment",
                new BigDecimal("4574.75"), new BigDecimal("4250.75")));

        Account emmaSavings = accounts.get(1);
        transactions.add(buildTransaction("TXN-DEMO000004", emmaSavings.getId(), emmaChequing.getId(),
                TransactionType.CREDIT, new BigDecimal("12800.00"), "Demo savings transfer in",
                new BigDecimal("0.00"), new BigDecimal("12800.00")));

        Account noahChequing = accounts.get(2);
        transactions.add(buildTransaction("TXN-DEMO000005", noahChequing.getId(), null,
                TransactionType.CREDIT, new BigDecimal("2200.00"), "Demo freelance payment",
                new BigDecimal("0.00"), new BigDecimal("2200.00")));
        transactions.add(buildTransaction("TXN-DEMO000006", noahChequing.getId(), null,
                TransactionType.DEBIT, new BigDecimal("309.50"), "Demo transit pass purchase",
                new BigDecimal("2200.00"), new BigDecimal("1890.50")));

        Account priyaChequing = accounts.get(4);
        transactions.add(buildTransaction("TXN-DEMO000007", priyaChequing.getId(), null,
                TransactionType.CREDIT, new BigDecimal("8500.00"), "Demo consulting deposit",
                new BigDecimal("0.00"), new BigDecimal("8500.00")));
        transactions.add(buildTransaction("TXN-DEMO000008", priyaChequing.getId(), null,
                TransactionType.DEBIT, new BigDecimal("380.00"), "Demo fitness membership",
                new BigDecimal("8500.00"), new BigDecimal("8120.00")));

        Account priyaSavings = accounts.get(5);
        transactions.add(buildTransaction("TXN-DEMO000009", priyaSavings.getId(), null,
                TransactionType.CREDIT, new BigDecimal("22500.00"), "Demo annual bonus deposit",
                new BigDecimal("0.00"), new BigDecimal("22500.00")));

        transactionRepository.saveAll(transactions);
    }

    private Transaction buildTransaction(
            String reference,
            UUID accountId,
            UUID relatedAccountId,
            TransactionType type,
            BigDecimal amount,
            String description,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(reference);
        transaction.setAccountId(accountId);
        transaction.setRelatedAccountId(relatedAccountId);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setCurrency(BankingConstants.DEFAULT_ACCOUNT_CURRENCY);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        return transaction;
    }

    private void seedNotifications(List<Customer> customers, List<Account> accounts) {
        List<Notification> notifications = List.of(
                buildNotification(customers.get(0).getId(), NotificationType.ACCOUNT_CREATED,
                        "Your new demo account " + accounts.get(0).getAccountNumber() + " has been created."),
                buildNotification(customers.get(0).getId(), NotificationType.BENEFICIARY_ADDED,
                        "Beneficiary 'Lucas Chen' has been added to your profile."),
                buildNotification(customers.get(1).getId(), NotificationType.PAYMENT_SUCCESS,
                        "Demo payment PAY-DEMO000001 of 150.00 CAD completed successfully."),
                buildNotification(customers.get(2).getId(), NotificationType.ACCOUNT_CREATED,
                        "Your new demo account " + accounts.get(4).getAccountNumber() + " has been created."),
                buildNotification(customers.get(2).getId(), NotificationType.PAYMENT_FAILED,
                        "Payment failed: Demo transfer limit exceeded for fictional test.")
        );
        notificationRepository.saveAll(notifications);
    }

    private Notification buildNotification(UUID customerId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.SENT);
        notification.setRead(false);
        return notification;
    }

    private void seedAuditEvents(List<Customer> customers, List<Account> accounts) {
        List<AuditEvent> events = List.of(
                buildAuditEvent("AUD-DEMO000001", customers.get(0).getId().toString(),
                        AuditAction.CUSTOMER_CREATED, AuditResourceType.CUSTOMER,
                        customers.get(0).getId().toString(),
                        "Demo customer Emma Thompson created"),
                buildAuditEvent("AUD-DEMO000002", customers.get(0).getId().toString(),
                        AuditAction.ACCOUNT_CREATED, AuditResourceType.ACCOUNT,
                        accounts.get(0).getId().toString(),
                        "Demo chequing account " + accounts.get(0).getAccountNumber() + " created"),
                buildAuditEvent("AUD-DEMO000003", customers.get(1).getId().toString(),
                        AuditAction.BENEFICIARY_CREATED, AuditResourceType.BENEFICIARY,
                        "demo-beneficiary-002",
                        "Demo beneficiary Sophie Labelle created"),
                buildAuditEvent("AUD-DEMO000004", customers.get(2).getId().toString(),
                        AuditAction.PAYMENT_INITIATED, AuditResourceType.PAYMENT,
                        "demo-payment-003",
                        "Demo payment initiated with reference PAY-DEMO000001"),
                buildAuditEvent("AUD-DEMO000005", customers.get(2).getId().toString(),
                        AuditAction.PAYMENT_COMPLETED, AuditResourceType.PAYMENT,
                        "demo-payment-003",
                        "Demo payment completed with reference PAY-DEMO000001")
        );
        auditRepository.saveAll(events);
    }

    private AuditEvent buildAuditEvent(
            String eventReference,
            String actorId,
            AuditAction action,
            String resourceType,
            String resourceId,
            String description) {
        AuditEvent event = new AuditEvent();
        event.setEventReference(eventReference);
        event.setActorId(actorId);
        event.setAction(action);
        event.setResourceType(resourceType);
        event.setResourceId(resourceId);
        event.setDescription(description);
        event.setResult(AuditResult.SUCCESS);
        event.setCorrelationId(DEMO_CORRELATION_ID);
        return event;
    }
}
