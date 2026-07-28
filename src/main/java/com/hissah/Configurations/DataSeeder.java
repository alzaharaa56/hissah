package com.hissah.Configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Development-only demonstration data.
 *
 * Run with:
 *   SPRING_PROFILES_ACTIVE=dev
 *   APP_SEED_ENABLED=true
 *
 * Never enable demo accounts in a real deployment.
 */
@Configuration
@Profile("dev")
public class DataSeeder {

    @Bean
    CommandLineRunner seedHissahDemoData(
            JdbcTemplate jdbcTemplate,
            @Value("${app.seed.enabled:false}") boolean seedEnabled
    ) {
        return arguments -> {
            if (!seedEnabled || tableHasRows(jdbcTemplate, "users")) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            String passwordHash = new BCryptPasswordEncoder().encode("Demo@123");

            insertUser(jdbcTemplate, "Hissah Administrator", "admin@hissah.demo",
                    "+96890000001", "ADMIN", passwordHash, now);
            insertUser(jdbcTemplate, "Demo Main Contractor", "contractor@hissah.demo",
                    "+96890000002", "MAIN_CONTRACTOR", passwordHash, now);
            insertUser(jdbcTemplate, "Demo Omani SME", "sme@hissah.demo",
                    "+96890000003", "SUBCONTRACTOR", passwordHash, now);

            Long contractorUserId = findId(jdbcTemplate, "users", "email", "contractor@hissah.demo");
            Long smeUserId = findId(jdbcTemplate, "users", "email", "sme@hissah.demo");

            jdbcTemplate.update("""
                    INSERT INTO companies
                    (legal_name, trading_name, company_type, cr_number, governorate,
                     description, verification_status, user_id, created_at, updated_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
                    """,
                    "Hissah Projects LLC", "Hissah Projects", "MAIN_CONTRACTOR",
                    "DEMO-CR-1001", "Muscat",
                    "Fictional main contractor used for development demonstrations.",
                    "VERIFIED", contractorUserId, Timestamp.valueOf(now), Timestamp.valueOf(now));

            jdbcTemplate.update("""
                    INSERT INTO companies
                    (legal_name, trading_name, company_type, cr_number, governorate,
                     description, verification_status, user_id, created_at, updated_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
                    """,
                    "Nizwa Technical SME", "Nizwa Technical", "SUBCONTRACTOR",
                    "DEMO-CR-2001", "Al Dakhiliyah",
                    "Fictional SME used for development demonstrations.",
                    "VERIFIED", smeUserId, Timestamp.valueOf(now), Timestamp.valueOf(now));

            jdbcTemplate.update("""
                    INSERT INTO categories (name, description, active, created_at, updated_at)
                    VALUES (?, ?, true, ?, ?)
                    """, "Electrical Works", "Electrical installation and maintenance services.",
                    Timestamp.valueOf(now), Timestamp.valueOf(now));

            Long contractorCompanyId = findId(jdbcTemplate, "companies", "cr_number", "DEMO-CR-1001");
            Long categoryId = findId(jdbcTemplate, "categories", "name", "Electrical Works");

            jdbcTemplate.update("""
                    INSERT INTO projects
                    (title, reference_number, sector, location, description, start_date, end_date,
                     status, contractor_company_id, created_at, updated_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
                    """,
                    "Muscat Office Development", "PRJ-DEMO-001", "CONSTRUCTION", "Muscat",
                    "Fictional awarded project used to demonstrate work-package bidding.",
                    Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusMonths(6)),
                    "ACTIVE", contractorCompanyId, Timestamp.valueOf(now), Timestamp.valueOf(now));

            Long projectId = findId(jdbcTemplate, "projects", "reference_number", "PRJ-DEMO-001");

            jdbcTemplate.update("""
                    INSERT INTO work_packages
                    (reference_number, title, scope, requirements, budget_min, budget_max,
                     deadline, location, status, eligibility_type, project_id, category_id,
                     published_at, created_at, updated_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
                    """,
                    "WPK-DEMO-001", "Electrical Installation Package",
                    "Supply and install the electrical fittings for one office floor.",
                    "Valid CR, company profile, and technical proposal.",
                    new BigDecimal("8000.000"), new BigDecimal("12000.000"),
                    Timestamp.valueOf(now.plusDays(10)), "Muscat", "OPEN", "SME_ONLY",
                    projectId, categoryId, Timestamp.valueOf(now),
                    Timestamp.valueOf(now), Timestamp.valueOf(now));
        };
    }

    private boolean tableHasRows(JdbcTemplate jdbcTemplate, String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName,
                Integer.class
        );
        return count != null && count > 0;
    }

    private void insertUser(
            JdbcTemplate jdbcTemplate,
            String fullName,
            String email,
            String phone,
            String role,
            String passwordHash,
            LocalDateTime now
    ) {
        jdbcTemplate.update("""
                INSERT INTO users
                (full_name, email, password_hash, phone, role, account_status,
                 created_at, updated_at, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, true)
                """,
                fullName, email, passwordHash, phone, role, "ACTIVE",
                Timestamp.valueOf(now), Timestamp.valueOf(now));
    }

    private Long findId(JdbcTemplate jdbcTemplate, String table, String column, String value) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM " + table + " WHERE " + column + " = ?",
                Long.class,
                value
        );
    }
}
