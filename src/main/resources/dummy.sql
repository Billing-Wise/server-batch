
-- 고객(업체)
INSERT INTO client (client_id, name, auth_code, phone, is_deleted, created_at, updated_at)
VALUES
    (1, '업체 A', 'AUTH12345', '010-1234-5678', false, NOW(), NOW());



-- 사용자(업체의 직원)
INSERT INTO user (user_id, client_id, email, password, name, phone, is_deleted, created_at, updated_at)
VALUES
    (1, 1, 'employee1@companyA.com', 'password1', '직원1', '010-1111-2222', false, NOW(), NOW()),
    (2, 1, 'employee2@companyA.com', 'password2', '직원2', '010-3333-4444', false, NOW(), NOW()),
    (3, 1, 'employee3@companyA.com', 'password3', '직원3', '010-5555-6666', false, NOW(), NOW()),
    (4, 1, 'employee4@companyA.com', 'password4', '직원4', '010-7777-8888', false, NOW(), NOW()),
    (5, 1, 'employee5@companyA.com', 'password5', '직원5', '010-9999-0000', false, NOW(), NOW());



-- 회원 데이터
INSERT INTO member (member_id, client_id, name, description, email, phone, is_deleted, created_at, updated_at)
VALUES
    (1, 1, '회원1', 'Description for Member 1', 'member1@companyA.com', '010-1111-2222', false, NOW(), NOW()),
    (2, 1, '회원2', 'Description for Member 2', 'member2@companyA.com', '010-3333-4444', false, NOW(), NOW()),
    (3, 1, '회원3', 'Description for Member 3', 'member3@companyA.com', '010-5555-6666', false, NOW(), NOW()),
    (4, 1, '회원4', 'Description for Member 4', 'member4@companyA.com', '010-7777-8888', false, NOW(), NOW()),
    (5, 1, '회원5', 'Description for Member 5', 'member5@companyA.com', '010-9999-0000', false, NOW(), NOW());



-- 동의정보 계좌
INSERT INTO consent_account (member_id, owner, bank, number, sign_url, is_deleted, created_at, updated_at)
VALUES
    (1, '회원1', 'Bank A', '1234567890', CONCAT('http://signurl.com/account/', 1), false, NOW(), NOW()),
    (2, '회원2', 'Bank B', '2345678901', CONCAT('http://signurl.com/account/', 2), false, NOW(), NOW()),
    (3, '회원3', 'Bank C', '3456789012', CONCAT('http://signurl.com/account/', 3), false, NOW(), NOW()),
    (4, '회원4', 'Bank D', '4567890123', CONCAT('http://signurl.com/account/', 4), false, NOW(), NOW()),
    (5, '회원5', 'Bank E', '5678901234', CONCAT('http://signurl.com/account/', 5), false, NOW(), NOW());



-- 상품 정보
INSERT INTO item (item_id, client_id, name, description, price, image_url, is_basic, is_deleted, created_at, updated_at)
VALUES
    (1, 1, 'Item 1', 'Description for Item 1', 1000, 'http://imageurl1.com', true, false, NOW(), NOW()),
    (2, 1, 'Item 2', 'Description for Item 2', 2000, 'http://imageurl2.com', false, false, NOW(), NOW()),
    (3, 1, 'Item 3', 'Description for Item 3', 3000, 'http://imageurl3.com', true, false, NOW(), NOW()),
    (4, 1, 'Item 4', 'Description for Item 4', 4000, 'http://imageurl4.com', false, false, NOW(), NOW()),
    (5, 1, 'Item 5', 'Description for Item 5', 5000, 'http://imageurl5.com', true, false, NOW(), NOW());



