
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