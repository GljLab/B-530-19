
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 租户表
-- =============================================
CREATE TABLE IF NOT EXISTS park_tenant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '租户ID',
    tenant_code VARCHAR(50) NOT NULL COMMENT '租户编号（ZH+序号，如ZH001）',
    tenant_type TINYINT NOT NULL DEFAULT 1 COMMENT '租户类型：1-个人租户，2-企业租户',
    name VARCHAR(50) NOT NULL COMMENT '姓名/企业名称',
    gender TINYINT COMMENT '性别：1-男，2-女（个人租户）',
    id_card VARCHAR(18) COMMENT '身份证号（个人租户）',
    birth_date DATE COMMENT '出生日期（个人租户）',
    phone VARCHAR(11) NOT NULL COMMENT '手机号（主要）',
    backup_phone VARCHAR(20) COMMENT '备用手机',
    email VARCHAR(100) COMMENT '电子邮箱',
    work_unit VARCHAR(100) COMMENT '工作单位',
    occupation VARCHAR(50) COMMENT '职业',
    income_range VARCHAR(50) COMMENT '月收入范围',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人姓名',
    emergency_relation VARCHAR(20) COMMENT '紧急联系人关系',
    emergency_phone VARCHAR(11) COMMENT '紧急联系人电话',
    tenant_status TINYINT DEFAULT 1 COMMENT '租户状态：1-在租，2-已退租，3-黑名单',
    remark VARCHAR(500) COMMENT '租户备注（物业内部记录）',
    enterprise_name VARCHAR(100) COMMENT '企业名称（企业租户）',
    enterprise_credit_code VARCHAR(50) COMMENT '统一社会信用代码（企业租户）',
    legal_person_name VARCHAR(50) COMMENT '法定代表人（企业租户）',
    contact_person VARCHAR(50) COMMENT '联系人姓名（企业租户）',
    contact_position VARCHAR(50) COMMENT '联系人职务（企业租户）',
    contact_phone VARCHAR(11) COMMENT '联系人手机（企业租户）',
    enterprise_address VARCHAR(300) COMMENT '企业地址（企业租户）',
    business_license_url VARCHAR(500) COMMENT '营业执照扫描件URL（企业租户）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_tenant_code (tenant_code),
    UNIQUE INDEX uk_id_card (id_card),
    INDEX idx_phone (phone),
    INDEX idx_tenant_type (tenant_type),
    INDEX idx_tenant_status (tenant_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- =============================================
-- 租赁合约表
-- =============================================
CREATE TABLE IF NOT EXISTS park_lease_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '合约ID',
    contract_code VARCHAR(50) NOT NULL COMMENT '合约编号（HT+序号，如HT001）',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    tenant_code VARCHAR(50) COMMENT '租户编号（冗余）',
    tenant_name VARCHAR(50) COMMENT '租户姓名（冗余）',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    owner_id BIGINT NOT NULL COMMENT '业主ID',
    owner_code VARCHAR(50) COMMENT '业主编号（冗余）',
    owner_name VARCHAR(50) COMMENT '业主姓名（冗余）',
    trustee_id BIGINT COMMENT '委托人ID',
    trustee_name VARCHAR(50) COMMENT '委托人姓名',
    lease_type TINYINT NOT NULL DEFAULT 1 COMMENT '租赁类型：1-整租，2-合租',
    start_date DATE NOT NULL COMMENT '租赁起始日期',
    end_date DATE NOT NULL COMMENT '租赁结束日期',
    lease_months INT COMMENT '租期（月，自动计算）',
    monthly_rent DECIMAL(12, 2) NOT NULL COMMENT '月租金（元）',
    deposit_amount DECIMAL(12, 2) DEFAULT 0 COMMENT '押金金额（元）',
    payment_method TINYINT NOT NULL DEFAULT 1 COMMENT '付款方式：1-月付，2-季付，3-半年付，4-年付',
    include_property_fee TINYINT DEFAULT 0 COMMENT '是否包含物业费：0-否，1-是',
    property_fee_monthly DECIMAL(10, 2) DEFAULT 0 COMMENT '物业费（元/月）',
    water_fee_bearer TINYINT DEFAULT 1 COMMENT '水费承担方：1-租户，2-业主',
    electricity_fee_bearer TINYINT DEFAULT 1 COMMENT '电费承担方：1-租户，2-业主',
    gas_fee_bearer TINYINT DEFAULT 1 COMMENT '燃气费承担方：1-租户，2-业主',
    early_termination_penalty DECIMAL(12, 2) DEFAULT 0 COMMENT '提前解约违约金（元）',
    late_payment_penalty_rate DECIMAL(5, 2) DEFAULT 0 COMMENT '逾期付款违约金比例（%/日）',
    contract_terms TEXT COMMENT '合约条款（文本）',
    contract_file_url VARCHAR(500) COMMENT '合约文件URL',
    id_card_copy_url VARCHAR(500) COMMENT '身份证复印件URL',
    other_attachments TEXT COMMENT '其他附件（JSON数组存储URL）',
    contract_status TINYINT DEFAULT 1 COMMENT '合约状态：1-待生效，2-生效中，3-已续签，4-已解除，5-已到期，6-已拒绝',
    audit_reject_reason VARCHAR(500) COMMENT '审核拒绝原因',
    audit_time DATETIME COMMENT '审核时间',
    auditor_id BIGINT COMMENT '审核人ID',
    auditor_name VARCHAR(50) COMMENT '审核人姓名',
    termination_reason VARCHAR(500) COMMENT '解除原因',
    termination_date DATE COMMENT '解除日期',
    termination_penalty DECIMAL(12, 2) DEFAULT 0 COMMENT '解除违约金（元）',
    termination_audit_status TINYINT COMMENT '解除审核状态：1-待审核，2-已通过，3-已拒绝',
    termination_audit_time DATETIME COMMENT '解除审核时间',
    termination_auditor_id BIGINT COMMENT '解除审核人ID',
    termination_auditor_name VARCHAR(50) COMMENT '解除审核人姓名',
    original_contract_id BIGINT COMMENT '原合约ID（续签时关联）',
    renewal_reminder_sent TINYINT DEFAULT 0 COMMENT '到期提醒是否已发送：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_contract_code (contract_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_property_id (property_id),
    INDEX idx_owner_id (owner_id),
    INDEX idx_contract_status (contract_status),
    INDEX idx_start_date (start_date),
    INDEX idx_end_date (end_date),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租赁合约表';

-- =============================================
-- 合约操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS park_lease_contract_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    contract_id BIGINT NOT NULL COMMENT '合约ID',
    contract_code VARCHAR(50) COMMENT '合约编号（冗余）',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型：创建、审核通过、审核拒绝、续签、解除、到期',
    operation_detail VARCHAR(500) COMMENT '操作详情',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_contract_id (contract_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合约操作日志表';

-- =============================================
-- 租户-房产关联表（由合约自动维护）
-- =============================================
CREATE TABLE IF NOT EXISTS park_tenant_property (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    tenant_code VARCHAR(50) COMMENT '租户编号（冗余）',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    contract_id BIGINT NOT NULL COMMENT '关联合约ID',
    contract_code VARCHAR(50) COMMENT '合约编号（冗余）',
    relation_status TINYINT DEFAULT 1 COMMENT '关联状态：1-生效中，2-已结束',
    start_date DATE COMMENT '关联开始日期',
    end_date DATE COMMENT '关联结束日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_property_id (property_id),
    INDEX idx_contract_id (contract_id),
    INDEX idx_relation_status (relation_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户-房产关联表';

-- =============================================
-- 新增租户管理菜单（ID: 700-731）
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(700, '租户管理', 100, 15, '/park/tenant', 'tenant/TenantList', 'park:tenant:list', 1, 1, 1, 'User'),
(701, '合约管理', 100, 16, '/park/contract', 'tenant/ContractList', 'park:contract:list', 1, 1, 1, 'Document'),
(702, '租户统计', 100, 17, '/park/tenant/stats', 'tenant/TenantStats', 'park:tenant:stats', 1, 1, 1, 'DataLine');

-- 租户管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(711, '租户查询', 700, 1, '', NULL, 'park:tenant:query', 2, 1, 1, NULL),
(712, '租户新增', 700, 2, '', NULL, 'park:tenant:add', 2, 1, 1, NULL),
(713, '租户修改', 700, 3, '', NULL, 'park:tenant:edit', 2, 1, 1, NULL),
(714, '租户删除', 700, 4, '', NULL, 'park:tenant:delete', 2, 1, 1, NULL),
(715, '加入黑名单', 700, 5, '', NULL, 'park:tenant:blacklist', 2, 1, 1, NULL),
(716, '导入租户', 700, 6, '', NULL, 'park:tenant:import', 2, 1, 1, NULL),
(717, '导出租户', 700, 7, '', NULL, 'park:tenant:export', 2, 1, 1, NULL);

-- 合约管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(721, '合约查询', 701, 1, '', NULL, 'park:contract:query', 2, 1, 1, NULL),
(722, '合约创建', 701, 2, '', NULL, 'park:contract:add', 2, 1, 1, NULL),
(723, '合约修改', 701, 3, '', NULL, 'park:contract:edit', 2, 1, 1, NULL),
(724, '合约审核', 701, 4, '', NULL, 'park:contract:audit', 2, 1, 1, NULL),
(725, '合约续签', 701, 5, '', NULL, 'park:contract:renew', 2, 1, 1, NULL),
(726, '合约解除', 701, 6, '', NULL, 'park:contract:terminate', 2, 1, 1, NULL),
(727, '合约打印', 701, 7, '', NULL, 'park:contract:print', 2, 1, 1, NULL);

-- 租户统计按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(731, '统计查询', 702, 1, '', NULL, 'park:tenant:statsQuery', 2, 1, 1, NULL);

-- 超级管理员拥有租户管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 700), (1, 701), (1, 702),
(1, 711), (1, 712), (1, 713), (1, 714), (1, 715), (1, 716), (1, 717),
(1, 721), (1, 722), (1, 723), (1, 724), (1, 725), (1, 726), (1, 727),
(1, 731);

-- 物业管理员拥有租户管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 700), (3, 701), (3, 702),
(3, 711), (3, 712), (3, 713), (3, 714), (3, 715), (3, 716), (3, 717),
(3, 721), (3, 722), (3, 723), (3, 724), (3, 725), (3, 726), (3, 727),
(3, 731);

-- 物业经理权限（查看+创建编辑租户+创建合约+审核合约，不可删除租户、不可强制解除合约）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 700), (4, 701), (4, 702),
(4, 711), (4, 712), (4, 713), (4, 717),
(4, 721), (4, 722), (4, 724), (4, 725),
(4, 731);

-- 客服人员权限（查看租户信息、创建租户、添加备注，不可创建合约、不可删除租户）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 700), (5, 701), (5, 702),
(5, 711), (5, 712),
(5, 721),
(5, 731);

-- 财务人员权限（查看租户和合约信息、查看租金和费用信息、导出数据，不可修改）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 700), (6, 701), (6, 702),
(6, 711), (6, 717),
(6, 721),
(6, 731);
