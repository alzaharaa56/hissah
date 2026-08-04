CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    role VARCHAR(40) NOT NULL,
    account_status VARCHAR(30) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    legal_name VARCHAR(180) NOT NULL,
    trading_name VARCHAR(180),
    company_type VARCHAR(40) NOT NULL,
    cr_number VARCHAR(80) NOT NULL UNIQUE,
    governorate VARCHAR(100) NOT NULL,
    description TEXT,
    verification_status VARCHAR(30) NOT NULL,
    rejection_reason VARCHAR(500),
    user_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_company_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    parent_category_id BIGINT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_category_parent FOREIGN KEY (parent_category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS company_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT uk_company_category UNIQUE (company_id, category_id),
    CONSTRAINT fk_company_category_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_company_category_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS company_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type VARCHAR(40) NOT NULL,
    document_number VARCHAR(120),
    file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    expiry_date DATE,
    verification_status VARCHAR(30) NOT NULL,
    company_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_company_document_company FOREIGN KEY (company_id) REFERENCES companies(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    reference_number VARCHAR(40) NOT NULL UNIQUE,
    sector VARCHAR(40) NOT NULL,
    location VARCHAR(120) NOT NULL,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    contractor_company_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_project_contractor FOREIGN KEY (contractor_company_id) REFERENCES companies(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS work_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(40) NOT NULL UNIQUE,
    title VARCHAR(180) NOT NULL,
    scope TEXT NOT NULL,
    requirements TEXT,
    budget_min DECIMAL(15,3) NOT NULL,
    budget_max DECIMAL(15,3) NOT NULL,
    deadline DATETIME NOT NULL,
    location VARCHAR(120) NOT NULL,
    status VARCHAR(30) NOT NULL,
    eligibility_type VARCHAR(30) NOT NULL,
    project_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    published_at DATETIME,
    closed_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_work_package_budget CHECK (budget_min >= 0 AND budget_max >= budget_min),
    CONSTRAINT fk_work_package_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_work_package_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_work_package_status (status),
    INDEX idx_work_package_deadline (deadline),
    INDEX idx_work_package_project (project_id),
    INDEX idx_work_package_category (category_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bids (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(40) NOT NULL UNIQUE,
    amount DECIMAL(15,3) NOT NULL,
    delivery_days INT NOT NULL,
    proposal_text TEXT NOT NULL,
    submitted_at DATETIME,
    status VARCHAR(25) NOT NULL,
    decision_reason VARCHAR(500),
    bidder_company_id BIGINT NOT NULL,
    work_package_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_bid_amount CHECK (amount > 0),
    CONSTRAINT chk_bid_delivery_days CHECK (delivery_days > 0),
    CONSTRAINT fk_bid_company FOREIGN KEY (bidder_company_id) REFERENCES companies(id),
    CONSTRAINT fk_bid_work_package FOREIGN KEY (work_package_id) REFERENCES work_packages(id),
    INDEX idx_bid_package (work_package_id),
    INDEX idx_bid_company (bidder_company_id),
    INDEX idx_bid_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bid_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL UNIQUE,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    document_type VARCHAR(40) NOT NULL,
    bid_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_bid_document_bid FOREIGN KEY (bid_id) REFERENCES bids(id),
    INDEX idx_bid_document_bid (bid_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS awards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(40) NOT NULL UNIQUE,
    awarded_at DATETIME NOT NULL,
    agreed_amount DECIMAL(15,3) NOT NULL,
    agreed_delivery_days INT NOT NULL,
    notes TEXT,
    status VARCHAR(20) NOT NULL,
    bid_id BIGINT NOT NULL UNIQUE,
    work_package_id BIGINT NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_award_amount CHECK (agreed_amount > 0),
    CONSTRAINT chk_award_delivery_days CHECK (agreed_delivery_days > 0),
    CONSTRAINT fk_award_bid FOREIGN KEY (bid_id) REFERENCES bids(id),
    CONSTRAINT fk_award_work_package FOREIGN KEY (work_package_id) REFERENCES work_packages(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS milestones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description TEXT,
    due_date DATE NOT NULL,
    completion_percent INT NOT NULL DEFAULT 0,
    status VARCHAR(25) NOT NULL,
    evidence_note TEXT,
    submitted_at DATETIME,
    approved_at DATETIME,
    award_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_milestone_completion CHECK (completion_percent BETWEEN 0 AND 100),
    CONSTRAINT fk_milestone_award FOREIGN KEY (award_id) REFERENCES awards(id),
    INDEX idx_milestone_award (award_id),
    INDEX idx_milestone_status (status),
    INDEX idx_milestone_due_date (due_date)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(25) NOT NULL,
    read_flag BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    read_at DATETIME,
    user_id BIGINT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_unread (user_id, read_flag)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(30) NOT NULL,
    entity_id BIGINT NOT NULL,
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    changed_at DATETIME NOT NULL,
    note VARCHAR(1000),
    CONSTRAINT fk_status_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id),
    INDEX idx_history_entity (entity_type, entity_id),
    INDEX idx_history_changed_at (changed_at)
) ENGINE=InnoDB;
