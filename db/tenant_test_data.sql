SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 租户管理测试数据
-- =============================================
-- 注意：执行前请确保已执行以下脚本：
-- 1. init.sql - 基础表结构
-- 2. property_init.sql - 房产表结构
-- 3. owner_init.sql - 业主表结构
-- 4. tenant_init.sql - 租户表结构
-- 5. property_test_data.sql - 房产测试数据
-- 6. owner_test_data.sql - 业主测试数据
-- =============================================

-- =============================================
-- 1. 租户测试数据（8个）
-- =============================================
-- 个人租户（6个）
INSERT INTO park_tenant (tenant_code, tenant_type, name, gender, id_card, birth_date,
    phone, backup_phone, email, work_unit, occupation, income_range,
    emergency_contact, emergency_relation, emergency_phone,
    tenant_status, remark) VALUES
('ZH001', 1, '陈浩', 1, '110101199203151234', '1992-03-15',
    '13900139001', '13900139011', 'chenhao@example.com', '北京互联网公司', '软件工程师', '15000-25000',
    '陈母', '母亲', '13900139021',
    1, '优质租户，按时交租'),
('ZH002', 1, '林小雨', 2, '110102199508202345', '1995-08-20',
    '13900139002', NULL, 'linxiaoyu@example.com', '北京设计公司', 'UI设计师', '10000-18000',
    '林父', '父亲', '13900139022',
    1, '年轻白领，爱干净'),
('ZH003', 1, '赵磊', 1, '110105198812103456', '1988-12-10',
    '13900139003', '13900139013', 'zhaolei@example.com', '北京金融公司', '金融分析师', '25000-40000',
    '赵敏', '配偶', '13900139023',
    1, '一家三口租住'),
('ZH004', 1, '王小明', 1, '110106199805254567', '1998-05-25',
    '13900139004', NULL, 'wangxiaoming@example.com', '北京某大学', '研究生', '5000-8000',
    '王父', '父亲', '13900139024',
    1, '学生租房，父母支付房租'),
('ZH005', 1, '刘芳', 2, '110108199009105678', '1990-09-10',
    '13900139005', '13900139015', 'liufang@example.com', '北京教育机构', '教师', '12000-20000',
    '刘母', '母亲', '13900139025',
    2, '已退租，合同到期正常结束'),
('ZH006', 1, '孙强', 1, '110110198511206789', '1985-11-20',
    '13900139006', NULL, 'sunqiang@example.com', '自由职业', '个体户', '20000-35000',
    '孙妻', '配偶', '13900139026',
    3, '黑名单，多次拖欠租金，损坏房屋设施');

-- 企业租户（2个）
INSERT INTO park_tenant (tenant_code, tenant_type, name, gender, id_card, birth_date,
    phone, backup_phone, email, work_unit, occupation, income_range,
    emergency_contact, emergency_relation, emergency_phone,
    tenant_status, remark,
    enterprise_name, enterprise_credit_code, legal_person_name,
    contact_person, contact_position, contact_phone, enterprise_address,
    business_license_url) VALUES
('ZH007', 2, '北京科创信息技术有限公司', NULL, NULL, NULL,
    '13900139007', '010-88888888', 'hr@kechuang.com', NULL, NULL, NULL,
    '李总', '法定代表人', '13900139077',
    1, '企业员工宿舍租赁，长期合作',
    '北京科创信息技术有限公司', '91110108MA0ABC1234', '李建国',
    '张经理', '行政经理', '13900139007', '北京市海淀区中关村科技园',
    'https://example.com/license/kechuang.jpg'),
('ZH008', 2, '北京众创空间管理有限公司', NULL, NULL, NULL,
    '13900139008', '010-66666666', 'contact@zhongchuang.com', NULL, NULL, NULL,
    '王总', '法定代表人', '13900139088',
    1, '联合办公空间租赁，整层租用',
    '北京众创空间管理有限公司', '91110105MA0DEF5678', '王志远',
    '刘主管', '运营主管', '13900139008', '北京市朝阳区建国路88号',
    'https://example.com/license/zhongchuang.jpg');

-- =============================================
-- 2. 租赁合约测试数据（5个）
-- =============================================
-- 合约1：陈浩 租 1-102（业主出租，YZ001张伟）- 生效中
INSERT INTO park_lease_contract (contract_code, tenant_id, tenant_code, tenant_name,
    property_id, property_code, owner_id, owner_code, owner_name,
    lease_type, start_date, end_date, lease_months,
    monthly_rent, deposit_amount, payment_method,
    include_property_fee, property_fee_monthly,
    water_fee_bearer, electricity_fee_bearer, gas_fee_bearer,
    early_termination_penalty, late_payment_penalty_rate,
    contract_terms, contract_status,
    audit_time, auditor_id, auditor_name,
    create_time, update_time) VALUES
('HT001', 1, 'ZH001', '陈浩',
    2, '1-102', 1, 'YZ001', '张伟',
    1, '2024-06-01', '2025-05-31', 12,
    3500.00, 7000.00, 1,
    0, 300.00,
    1, 1, 1,
    3500.00, 0.05,
    '租赁期间，承租人应按时支付租金及各项费用。提前解约需提前30天通知，并支付1个月租金作为违约金。', 2,
    '2024-05-25 10:30:00', 1, '管理员',
    '2024-05-20 14:00:00', '2024-05-25 10:30:00');

-- 合约2：林小雨 租 1-204（业主出租，YZ003王芳）- 生效中
INSERT INTO park_lease_contract (contract_code, tenant_id, tenant_code, tenant_name,
    property_id, property_code, owner_id, owner_code, owner_name,
    lease_type, start_date, end_date, lease_months,
    monthly_rent, deposit_amount, payment_method,
    include_property_fee, property_fee_monthly,
    water_fee_bearer, electricity_fee_bearer, gas_fee_bearer,
    early_termination_penalty, late_payment_penalty_rate,
    contract_terms, contract_status,
    audit_time, auditor_id, auditor_name,
    create_time, update_time) VALUES
('HT002', 2, 'ZH002', '林小雨',
    8, '1-204', 3, 'YZ003', '王芳',
    1, '2024-07-15', '2025-07-14', 12,
    2800.00, 5600.00, 2,
    0, 250.00,
    1, 1, 1,
    2800.00, 0.03,
    '承租人不得擅自改变房屋结构，应爱护屋内设施。押金在合同到期后7个工作日内退还。', 2,
    '2024-07-10 15:00:00', 1, '管理员',
    '2024-07-05 09:30:00', '2024-07-10 15:00:00');

-- 合约3：赵磊 租 1-1002（业主出租，YZ008周明）- 生效中
INSERT INTO park_lease_contract (contract_code, tenant_id, tenant_code, tenant_name,
    property_id, property_code, owner_id, owner_code, owner_name,
    lease_type, start_date, end_date, lease_months,
    monthly_rent, deposit_amount, payment_method,
    include_property_fee, property_fee_monthly,
    water_fee_bearer, electricity_fee_bearer, gas_fee_bearer,
    early_termination_penalty, late_payment_penalty_rate,
    contract_terms, contract_status,
    audit_time, auditor_id, auditor_name,
    create_time, update_time) VALUES
('HT003', 3, 'ZH003', '赵磊',
    14, '1-1002', 8, 'YZ008', '周明',
    1, '2024-03-01', '2026-02-28', 24,
    4200.00, 8400.00, 4,
    1, 350.00,
    1, 1, 1,
    8400.00, 0.05,
    '两年期租赁合同，年付优惠10%。物业费包含在租金内。提前解约需支付2个月租金作为违约金。', 2,
    '2024-02-25 11:00:00', 2, '物业经理',
    '2024-02-20 16:00:00', '2024-02-25 11:00:00');

-- 合约4：北京科创信息技术有限公司 租 1-1502（业主出租，YZ004刘强）- 待生效
INSERT INTO park_lease_contract (contract_code, tenant_id, tenant_code, tenant_name,
    property_id, property_code, owner_id, owner_code, owner_name,
    lease_type, start_date, end_date, lease_months,
    monthly_rent, deposit_amount, payment_method,
    include_property_fee, property_fee_monthly,
    water_fee_bearer, electricity_fee_bearer, gas_fee_bearer,
    early_termination_penalty, late_payment_penalty_rate,
    contract_terms, contract_status,
    create_time, update_time) VALUES
('HT004', 7, 'ZH007', '北京科创信息技术有限公司',
    18, '1-1502', 4, 'YZ004', '刘强',
    2, '2025-01-01', '2025-12-31', 12,
    6000.00, 12000.00, 3,
    1, 500.00,
    1, 1, 1,
    12000.00, 0.05,
    '企业员工宿舍，租住5人。不得从事违法经营活动。半年付一次。', 1,
    '2024-12-15 10:00:00', '2024-12-15 10:00:00');

-- 合约5：刘芳 租 1-103（空置，YZ006陈刚）- 已到期（历史数据）
INSERT INTO park_lease_contract (contract_code, tenant_id, tenant_code, tenant_name,
    property_id, property_code, owner_id, owner_code, owner_name,
    lease_type, start_date, end_date, lease_months,
    monthly_rent, deposit_amount, payment_method,
    include_property_fee, property_fee_monthly,
    water_fee_bearer, electricity_fee_bearer, gas_fee_bearer,
    early_termination_penalty, late_payment_penalty_rate,
    contract_terms, contract_status,
    audit_time, auditor_id, auditor_name,
    create_time, update_time) VALUES
('HT005', 5, 'ZH005', '刘芳',
    3, '1-103', 6, 'YZ006', '陈刚',
    1, '2023-06-01', '2024-05-31', 12,
    3000.00, 6000.00, 1,
    0, 280.00,
    1, 1, 1,
    3000.00, 0.03,
    '一年期租赁合同，月付。到期后租户选择不续租。', 5,
    '2023-05-28 09:00:00', 1, '管理员',
    '2023-05-20 14:00:00', '2024-06-01 08:00:00');

-- =============================================
-- 3. 租户-房产关联数据（由合约生效自动创建，此处为示例）
-- =============================================
INSERT INTO park_tenant_property (tenant_id, tenant_code, property_id, property_code,
    contract_id, contract_code, relation_status, start_date, end_date,
    create_time, update_time) VALUES
(1, 'ZH001', 2, '1-102', 1, 'HT001', 1, '2024-06-01', '2025-05-31', '2024-05-25 10:30:00', '2024-05-25 10:30:00'),
(2, 'ZH002', 8, '1-204', 2, 'HT002', 1, '2024-07-15', '2025-07-14', '2024-07-10 15:00:00', '2024-07-10 15:00:00'),
(3, 'ZH003', 14, '1-1002', 3, 'HT003', 1, '2024-03-01', '2026-02-28', '2024-02-25 11:00:00', '2024-02-25 11:00:00'),
(5, 'ZH005', 3, '1-103', 5, 'HT005', 2, '2023-06-01', '2024-05-31', '2023-05-28 09:00:00', '2024-06-01 08:00:00');

-- =============================================
-- 4. 合约操作日志测试数据
-- =============================================
INSERT INTO park_lease_contract_log (contract_id, contract_code, operation_type, operation_detail,
    operator_id, operator_name, create_time) VALUES
(1, 'HT001', '创建', '创建租赁合约，租户：陈浩，房产：1-102', 1, '管理员', '2024-05-20 14:00:00'),
(1, 'HT001', '审核通过', '审核通过，合约生效', 1, '管理员', '2024-05-25 10:30:00'),
(2, 'HT002', '创建', '创建租赁合约，租户：林小雨，房产：1-204', 1, '管理员', '2024-07-05 09:30:00'),
(2, 'HT002', '审核通过', '审核通过，合约生效', 1, '管理员', '2024-07-10 15:00:00'),
(3, 'HT003', '创建', '创建租赁合约，租户：赵磊，房产：1-1002', 2, '物业经理', '2024-02-20 16:00:00'),
(3, 'HT003', '审核通过', '审核通过，合约生效，两年期年付', 2, '物业经理', '2024-02-25 11:00:00'),
(4, 'HT004', '创建', '创建租赁合约，租户：北京科创信息技术有限公司，房产：1-1502', 1, '管理员', '2024-12-15 10:00:00'),
(5, 'HT005', '创建', '创建租赁合约，租户：刘芳，房产：1-103', 1, '管理员', '2023-05-20 14:00:00'),
(5, 'HT005', '审核通过', '审核通过，合约生效', 1, '管理员', '2023-05-28 09:00:00'),
(5, 'HT005', '到期', '合约到期自动结束，租户未续租', NULL, '系统自动', '2024-06-01 08:00:00');

-- =============================================
-- 5. 更新房产状态（合约生效的房产应更新为已出租）
-- =============================================
-- 注意：实际应用中应由合约审核流程自动更新，此处手动同步测试数据
UPDATE park_property SET status = 6, tenant_id = 1 WHERE id = 2;  -- 1-102 已出租
UPDATE park_property SET status = 6, tenant_id = 2 WHERE id = 8;  -- 1-204 已出租
UPDATE park_property SET status = 6, tenant_id = 3 WHERE id = 14; -- 1-1002 已出租
-- 1-103 已恢复为空租（合约已到期）
-- 1-1502 仍为业主出租（合约待生效）

-- =============================================
-- 6. 插入房产状态变更日志
-- =============================================
INSERT INTO park_property_status_log (property_id, property_code, old_status, new_status,
    change_reason, operator_id, operator_name, create_time) VALUES
(2, '1-102', 4, 6, '租赁合约审核通过，房产状态更新为已出租', 1, '管理员', '2024-05-25 10:30:00'),
(8, '1-204', 4, 6, '租赁合约审核通过，房产状态更新为已出租', 1, '管理员', '2024-07-10 15:00:00'),
(14, '1-1002', 4, 6, '租赁合约审核通过，房产状态更新为已出租', 2, '物业经理', '2024-02-25 11:00:00'),
(3, '1-103', 6, 5, '租赁合约到期，房产状态恢复为空置', NULL, '系统自动', '2024-06-01 08:00:00');

-- =============================================
-- 测试数据创建完成
-- =============================================
SELECT '租户测试数据创建完成' AS message;
SELECT COUNT(*) AS tenant_count FROM park_tenant WHERE deleted = 0;
SELECT COUNT(*) AS contract_count FROM park_lease_contract WHERE deleted = 0;
