CREATE DATABASE hotel_management_db;
USE hotel_management_db;

CREATE TABLE Room_Category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    hourly_price DECIMAL(10, 2),
    daily_price DECIMAL(10, 2),
    overnight_price DECIMAL(10, 2),
    early_checkin_fee DECIMAL(10, 2),
    late_checkout_fee DECIMAL(10, 2),
    extra_fee_type ENUM('FIXED', 'PERCENTAGE') DEFAULT 'FIXED',
    default_extra_fee DECIMAL(10, 2) DEFAULT 0.00,
    apply_to_all_categories BOOLEAN DEFAULT FALSE,
    standard_adult_capacity INT DEFAULT 1,
    standard_child_capacity INT DEFAULT 0,
    max_adult_capacity INT DEFAULT 2,
    max_child_capacity INT DEFAULT 1,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    img_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE Rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_category_id BIGINT NOT NULL,
    floor INT,
    start_date DATE,
   status ENUM(
    'AVAILABLE',        -- Đang trống
    'UPCOMING',         -- Sắp nhận
    'IN_USE',           -- Đang sử dụng
    'CHECKOUT_SOON',    -- Sắp trả
    'OVERDUE'           -- Quá giờ trả
) DEFAULT 'AVAILABLE',
    note TEXT,
    is_clean BOOLEAN DEFAULT TRUE,
    check_in_duration INT DEFAULT 0,
    img_1 VARCHAR(255),
    img_2 VARCHAR(255),
    img_3 VARCHAR(255),
    img_4 VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,

    -- Foreign Key
    CONSTRAINT fk_room_type
        FOREIGN KEY (room_category_id)
        REFERENCES Room_Category(id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


CREATE TABLE Users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    is_locked BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,

    -- Foreign Key constraint
    CONSTRAINT fk_activity_user
        FOREIGN KEY (user_id)
        REFERENCES Users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE TABLE Employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('FEMALE', 'MALE','OTHER') NOT NULL,
    dob DATE NOT NULL,
    phone VARCHAR(15) UNIQUE,
    id_card VARCHAR(20) UNIQUE,
    address VARCHAR(255),
    position VARCHAR(100),
    department VARCHAR(100),
    start_date DATE NOT NULL,
    note TEXT,
    img_url VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,

    -- Ràng buộc khóa ngoại
    CONSTRAINT fk_employee_user
        FOREIGN KEY (user_id)
        REFERENCES Users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_role_user
        FOREIGN KEY (user_id) REFERENCES Users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_user_role_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permission_role
        FOREIGN KEY (role_id) REFERENCES roles(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_role_permission_permission
        FOREIGN KEY (permission_id) REFERENCES permissions(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
// trigger
DELIMITER $$

CREATE TRIGGER trg_after_update_room
AFTER UPDATE ON Rooms
FOR EACH ROW
BEGIN
    -- Kiểm tra xem có thay đổi dữ liệu không
    IF NOT (
        OLD.floor <=> NEW.floor AND
        OLD.start_date <=> NEW.start_date AND
        OLD.status <=> NEW.status AND
        OLD.note <=> NEW.note AND
        OLD.is_clean <=> NEW.is_clean AND
        OLD.check_in_duration <=> NEW.check_in_duration AND
        OLD.img_1 <=> NEW.img_1 AND
        OLD.img_2 <=> NEW.img_2 AND
        OLD.img_3 <=> NEW.img_3 AND
        OLD.img_4 <=> NEW.img_4
    ) THEN
        INSERT INTO activity_logs (
            user_id,
            action,
            description
        ) VALUES (
            @current_user_id,
            'UPDATE_ROOM',
            CONCAT('Room ID ', NEW.id, ' has been updated.')
        );
    END IF;
END$$

DELIMITER ;
