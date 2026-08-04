package com.hissah.Configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Optional local/demo data.
 *
 * Enable it with either:
 *   SPRING_PROFILES_ACTIVE=postman
 * or:
 *   APP_SEED_ENABLED=true
 *
 * The seeder is idempotent: it only inserts missing demo records.
 */
@Configuration
@ConditionalOnProperty(
        name = "app.seed.enabled",
        havingValue = "true"
)
public class DataSeeder {

    private static final String DEMO_PASSWORD = "Demo@123";

    @Bean
    CommandLineRunner seedHissahDemoData(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:false}") boolean seedEnabled
    ) {
        return arguments -> {
            if (!seedEnabled) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();
            String passwordHash = passwordEncoder.encode(DEMO_PASSWORD);

            ensureUser(
                    jdbcTemplate,
                    "Hissah Administrator",
                    "admin@hissah.demo",
                    "+96890000001",
                    "ADMIN",
                    passwordHash,
                    now
            );
            ensureUser(
                    jdbcTemplate,
                    "Demo Main Contractor",
                    "contractor@hissah.demo",
                    "+96890000002",
                    "MAIN_CONTRACTOR",
                    passwordHash,
                    now
            );
            ensureUser(
                    jdbcTemplate,
                    "Demo Omani SME",
                    "sme@hissah.demo",
                    "+96890000003",
                    "SUBCONTRACTOR",
                    passwordHash,
                    now
            );

            Long contractorUserId = findId(
                    jdbcTemplate,
                    "users",
                    "email",
                    "contractor@hissah.demo"
            );
            Long smeUserId = findId(
                    jdbcTemplate,
                    "users",
                    "email",
                    "sme@hissah.demo"
            );

            ensureCompany(
                    jdbcTemplate,
                    "Hissah Projects LLC",
                    "Hissah Projects",
                    "MAIN_CONTRACTOR",
                    "DEMO-CR-1001",
                    "Muscat",
                    "Fictional main contractor used for local API testing.",
                    contractorUserId,
                    now
            );
            ensureCompany(
                    jdbcTemplate,
                    "Nizwa Technical SME",
                    "Nizwa Technical",
                    "SUBCONTRACTOR",
                    "DEMO-CR-2001",
                    "Al Dakhiliyah",
                    "Fictional subcontractor used for local API testing.",
                    smeUserId,
                    now
            );

            ensureCategory(
                    jdbcTemplate,
                    "Electrical Works",
                    "Electrical installation and maintenance services.",
                    now
            );

            Long contractorCompanyId = findId(
                    jdbcTemplate,
                    "companies",
                    "cr_number",
                    "DEMO-CR-1001"
            );
            Long categoryId = findId(
                    jdbcTemplate,
                    "categories",
                    "name",
                    "Electrical Works"
            );

            ensureProject(
                    jdbcTemplate,
                    contractorCompanyId,
                    now
            );

            Long projectId = findId(
                    jdbcTemplate,
                    "projects",
                    "reference_number",
                    "PRJ-DEMO-001"
            );

            ensureWorkPackage(
                    jdbcTemplate,
                    projectId,
                    categoryId,
                    now
            );
        };
    }

    private void ensureUser(
            JdbcTemplate jdbcTemplate,
            String fullName,
            String email,
            String phone,
            String role,
            String passwordHash,
            LocalDateTime now
    ) {
        if (exists(jdbcTemplate, "users", "email", email)) {
            return;
        }

        jdbcTemplate.update("""
        INSERT INTO users
        (
            full_name,
            email,
            password_hash,
            phone,
            role,
            account_status,
            active,
            created_at
        )
        VALUES (?, ?, ?, ?, ?, ?, true, ?)
        """,
                fullName,
                email,
                passwordHash,
                phone,
                role,
                "ACTIVE",
                Timestamp.valueOf(now)
        );
    }

    private void ensureCompany(
            JdbcTemplate jdbcTemplate,
            String legalName,
            String tradingName,
            String companyType,
            String crNumber,
            String governorate,
            String description,
            Long userId,
            LocalDateTime now
    ) {
        if (exists(jdbcTemplate, "companies", "cr_number", crNumber)) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO companies
                (legal_name, trading_name, company_type, cr_number, governorate,
                 description, verification_status, rejection_reason, user_id,
                 created_at, updated_at, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, true)
                """,
                legalName,
                tradingName,
                companyType,
                crNumber,
                governorate,
                description,
                "VERIFIED",
                userId,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    private void ensureCategory(
            JdbcTemplate jdbcTemplate,
            String name,
            String description,
            LocalDateTime now
    ) {
        if (exists(jdbcTemplate, "categories", "name", name)) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO categories
                (name, description, parent_category_id, active, created_at, updated_at)
                VALUES (?, ?, NULL, true, ?, ?)
                """,
                name,
                description,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    private void ensureProject(
            JdbcTemplate jdbcTemplate,
            Long contractorCompanyId,
            LocalDateTime now
    ) {
        if (exists(
                jdbcTemplate,
                "projects",
                "reference_number",
                "PRJ-DEMO-001"
        )) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO projects
                (title, reference_number, sector, location, description,
                 start_date, end_date, status, contractor_company_id,
                 created_at, updated_at, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
                """,
                "Muscat Office Development",
                "PRJ-DEMO-001",
                "CONSTRUCTION",
                "Muscat",
                "Fictional project used for local API testing.",
                Date.valueOf(LocalDate.now()),
                Date.valueOf(LocalDate.now().plusMonths(6)),
                "ACTIVE",
                contractorCompanyId,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    private void ensureWorkPackage(
            JdbcTemplate jdbcTemplate,
            Long projectId,
            Long categoryId,
            LocalDateTime now
    ) {
        if (exists(
                jdbcTemplate,
                "work_packages",
                "reference_number",
                "WPK-DEMO-001"
        )) {
            return;
        }

        jdbcTemplate.update("""
                INSERT INTO work_packages
                (reference_number, title, scope, requirements,
                 budget_min, budget_max, deadline, location,
                 status, eligibility_type, project_id, category_id,
                 published_at, closed_at, created_at, updated_at, is_active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, true)
                """,
                "WPK-DEMO-001",
                "Electrical Installation Package",
                "Supply and install electrical fittings for one office floor.",
                "Valid CR, company profile, and technical proposal.",
                new BigDecimal("8000.000"),
                new BigDecimal("12000.000"),
                Timestamp.valueOf(now.plusDays(10)),
                "Muscat",
                "OPEN",
                "SME_ONLY",
                projectId,
                categoryId,
                Timestamp.valueOf(now),
                Timestamp.valueOf(now),
                Timestamp.valueOf(now)
        );
    }

    private boolean exists(
            JdbcTemplate jdbcTemplate,
            String table,
            String column,
            Object value
    ) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table
                        + " WHERE " + column + " = ?",
                Integer.class,
                value
        );
        return count != null && count > 0;
    }

    private Long findId(
            JdbcTemplate jdbcTemplate,
            String table,
            String column,
            Object value
    ) {
        return jdbcTemplate.queryForObject(
                "SELECT id FROM " + table
                        + " WHERE " + column + " = ?",
                Long.class,
                value
        );
    }
}
