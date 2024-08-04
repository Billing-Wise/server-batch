
-- 계약 상태
INSERT INTO contract_status (contract_status_id, name, is_deleted, created_at, updated_at)
VALUES
    (1, '대기', false, NOW(), NOW()),
    (2, '진행', false, NOW(), NOW()),
    (3, '종료', false, NOW(), NOW());


-- 결제 수단
INSERT INTO payment_type (payment_type_id, name, is_basic, is_deleted, created_at, updated_at)
VALUES
    (1, '납부자 결제', false, false, NOW(), NOW()),
    (2, '자동 이체', true, false, NOW(), NOW());


-- 청구 타입
INSERT INTO invoice_type (invoice_type_id, name, is_deleted, created_at, updated_at)
VALUES
    (1, '자동청구', false, NOW(), NOW()),
    (2, '수동청구', false, NOW(), NOW());


-- 납부 상태
INSERT INTO payment_status (payment_status_id, name, is_deleted, created_at, updated_at)
VALUES
    (1, '미납', false, NOW(), NOW()),
    (2, '완납', false, NOW(), NOW()),
    (3, '대기', false, NOW(), NOW());


-- 통계 구분 값
INSERT INTO invoice_statistics_type (type_name, is_deleted)
VALUES
    ('주간', false),
    ('월간', false);


-- 통계 테이블 생성 쿼리
CREATE TABLE invoice_statistics_type (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         type_name VARCHAR(50) NOT NULL,
                                         is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE invoice_statistics (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    reference_date TIMESTAMP NOT NULL,
                                    total_invoiced BIGINT NOT NULL,
                                    total_collected BIGINT NOT NULL,
                                    outstanding BIGINT NOT NULL,
                                    type_id BIGINT NOT NULL,
                                    year INT NOT NULL,
                                    month INT DEFAULT NULL,
                                    week INT DEFAULT NULL,
                                    client_id BIGINT NOT NULL,
                                    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    FOREIGN KEY (type_id) REFERENCES invoice_statistics_type(id)
);

--- foreign key 참조 (invoice_statistics , client)
alter table invoice_statistics
    add constraint invoice_statistics_client_client_id_fk
        foreign key (client_id) references client (client_id);


