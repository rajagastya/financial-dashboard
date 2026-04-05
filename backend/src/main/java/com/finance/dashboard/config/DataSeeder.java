package com.finance.dashboard.config;

import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.model.UserStatus;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository financialRecordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      FinancialRecordRepository financialRecordRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.financialRecordRepository = financialRecordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedRecords();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            userRepository.findByEmailIgnoreCase("admin@finance.local").ifPresent(user -> ensurePassword(user, "Admin@123"));
            userRepository.findByEmailIgnoreCase("analyst@finance.local").ifPresent(user -> ensurePassword(user, "Analyst@123"));
            userRepository.findByEmailIgnoreCase("viewer@finance.local").ifPresent(user -> ensurePassword(user, "Viewer@123"));
            userRepository.findByEmailIgnoreCase("inactive@finance.local").ifPresent(user -> ensurePassword(user, "Viewer@123"));
            return;
        }

        userRepository.saveAll(List.of(
                createUser("Admin User", "admin@finance.local", Role.ADMIN, UserStatus.ACTIVE, "Admin@123"),
                createUser("Analyst User", "analyst@finance.local", Role.ANALYST, UserStatus.ACTIVE, "Analyst@123"),
                createUser("Viewer User", "viewer@finance.local", Role.VIEWER, UserStatus.ACTIVE, "Viewer@123"),
                createUser("Inactive Viewer", "inactive@finance.local", Role.VIEWER, UserStatus.INACTIVE, "Viewer@123")
        ));
    }

    private void seedRecords() {
        if (financialRecordRepository.count() > 0) {
            return;
        }

        financialRecordRepository.saveAll(List.of(
                createRecord("Salary", RecordType.INCOME, "Salary", "2026-04-01", "Monthly salary"),
                createRecord("Freelance Project", RecordType.INCOME, "Consulting", "2026-03-16", "Spring API work"),
                createRecord("Office Rent", RecordType.EXPENSE, "Rent", "2026-03-05", "Workspace rent"),
                createRecord("Cloud Hosting", RecordType.EXPENSE, "Infrastructure", "2026-02-21", "Production hosting"),
                createRecord("Travel Reimbursement", RecordType.INCOME, "Travel", "2026-02-12", "Client meeting travel"),
                createRecord("Marketing Campaign", RecordType.EXPENSE, "Marketing", "2026-01-28", "Quarterly ads"),
                createRecord("Equipment Purchase", RecordType.EXPENSE, "Equipment", "2026-01-12", "New monitors")
        ));
    }

    private User createUser(String name, String email, Role role, UserStatus status, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private FinancialRecord createRecord(String notes, RecordType type, String category, String date, String description) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(defaultAmount(category, type));
        record.setType(type);
        record.setCategory(category);
        record.setTransactionDate(LocalDate.parse(date));
        record.setNotes(notes + " - " + description);
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        return record;
    }

    private BigDecimal defaultAmount(String category, RecordType type) {
        return switch (category) {
            case "Salary" -> new BigDecimal("5000.00");
            case "Consulting" -> new BigDecimal("1800.00");
            case "Rent" -> new BigDecimal("1200.00");
            case "Infrastructure" -> new BigDecimal("450.00");
            case "Travel" -> new BigDecimal("300.00");
            case "Marketing" -> new BigDecimal("650.00");
            case "Equipment" -> new BigDecimal("900.00");
            default -> type == RecordType.INCOME ? new BigDecimal("1000.00") : new BigDecimal("500.00");
        };
    }

    private void ensurePassword(User user, String password) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
