
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- =============================================
-- 业主表
-- =============================================
CREATE TABLE IF NOT EXISTS park_owner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '业主ID',
    owner_code VARCHAR(50) NOT NULL COMMENT '业主编号（YZ+序号，如YZ001）',
    owner_type TINYINT NOT NULL DEFAULT 1 COMMENT '业主类型：1-个人业主，2-企业业主，3-投资业主',
    name VARCHAR(50) NOT NULL COMMENT '姓名/企业名称',
    gender TINYINT COMMENT '性别：1-男，2-女（个人业主）',
    id_card VARCHAR(18) COMMENT '身份证号（个人业主）',
    birth_date DATE COMMENT '出生日期（个人业主）',
    age INT COMMENT '年龄（个人业主，自动计算）',
    nation VARCHAR(20) COMMENT '民族（个人业主）',
    marital_status TINYINT COMMENT '婚姻状况：1-未婚，2-已婚，3-离异，4-丧偶（个人业主）',
    phone VARCHAR(11) NOT NULL COMMENT '手机号（主要）',
    backup_phone VARCHAR(11) COMMENT '备用手机',
    email VARCHAR(100) COMMENT '电子邮箱',
    wechat VARCHAR(50) COMMENT '微信号',
    household_address VARCHAR(300) COMMENT '户籍地址',
    current_address VARCHAR(300) COMMENT '现居住地址',
    work_unit VARCHAR(100) COMMENT '工作单位',
    occupation VARCHAR(50) COMMENT '职业',
    family_count INT DEFAULT 1 COMMENT '家庭人口数',
    emergency_contact VARCHAR(50) COMMENT '紧急联系人姓名',
    emergency_relation VARCHAR(20) COMMENT '紧急联系人关系',
    emergency_phone VARCHAR(11) COMMENT '紧急联系人电话',
    occupancy_status TINYINT DEFAULT 2 COMMENT '入住状态：1-已入住，2-未入住，3-委托出租',
    auth_status TINYINT DEFAULT 1 COMMENT '认证状态：1-未认证，2-已认证，3-认证失败',
    auth_reject_reason VARCHAR(500) COMMENT '认证拒绝原因',
    owner_tags VARCHAR(200) COMMENT '业主标签（逗号分隔）：VIP业主、长期欠费、友好邻居、投诉较多',
    remark VARCHAR(500) COMMENT '业主备注（物业内部记录）',
    enterprise_credit_code VARCHAR(50) COMMENT '统一社会信用代码（企业业主）',
    enterprise_type VARCHAR(50) COMMENT '企业类型（企业业主）',
    legal_person_name VARCHAR(50) COMMENT '法定代表人姓名（企业业主）',
    legal_person_id_card VARCHAR(18) COMMENT '法定代表人身份证号（企业业主）',
    contact_person VARCHAR(50) COMMENT '联系人姓名（企业业主）',
    contact_position VARCHAR(50) COMMENT '联系人职务（企业业主）',
    contact_phone VARCHAR(11) COMMENT '联系人手机（企业业主）',
    enterprise_address VARCHAR(300) COMMENT '企业地址（企业业主）',
    registered_capital DECIMAL(15, 2) COMMENT '注册资本（万元，企业业主）',
    business_license_url VARCHAR(500) COMMENT '营业执照扫描件URL（企业业主）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_owner_code (owner_code),
    UNIQUE INDEX uk_id_card (id_card),
    INDEX idx_phone (phone),
    INDEX idx_owner_type (owner_type),
    INDEX idx_occupancy_status (occupancy_status),
    INDEX idx_auth_status (auth_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业主表';

-- =============================================
-- 业主-房产关联表
-- =============================================
CREATE TABLE IF NOT EXISTS park_owner_property (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    owner_id BIGINT NOT NULL COMMENT '业主ID',
    owner_code VARCHAR(50) COMMENT '业主编号（冗余）',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    property_right_type TINYINT NOT NULL DEFAULT 1 COMMENT '产权关系：1-完全产权，2-共有产权，3-使用权',
    property_right_ratio DECIMAL(5, 2) DEFAULT 100 COMMENT '产权比例（%，共有产权时）',
    property_cert_no VARCHAR(100) COMMENT '产权证号',
    acquire_type TINYINT DEFAULT 1 COMMENT '取得方式：1-购买，2-继承，3-赠与，4-拆迁',
    acquire_date DATE COMMENT '取得日期',
    purchase_price DECIMAL(15, 2) COMMENT '购买价格（万元）',
    move_in_date DATE COMMENT '入住日期',
    is_self_occupy TINYINT DEFAULT 1 COMMENT '是否自住：1-是，0-否（出租或空置）',
    relation_status TINYINT DEFAULT 1 COMMENT '关联状态：1-正常，2-已转让，3-冻结',
    unbind_time DATETIME COMMENT '解除关联时间',
    unbind_operator_id BIGINT COMMENT '解除关联操作人ID',
    unbind_operator_name VARCHAR(50) COMMENT '解除关联操作人姓名',
    unbind_reason VARCHAR(500) COMMENT '解除关联原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_owner_id (owner_id),
    INDEX idx_property_id (property_id),
    INDEX idx_relation_status (relation_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业主-房产关联表';

-- =============================================
-- 业主认证审核日志表
-- =============================================
CREATE TABLE IF NOT EXISTS park_owner_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审核记录ID',
    owner_id BIGINT NOT NULL COMMENT '业主ID',
    owner_code VARCHAR(50) COMMENT '业主编号（冗余）',
    owner_name VARCHAR(50) COMMENT '业主姓名（冗余）',
    audit_result TINYINT NOT NULL COMMENT '审核结果：2-通过（已认证），3-拒绝（认证失败）',
    audit_opinion VARCHAR(500) COMMENT '审核意见',
    reject_reason VARCHAR(500) COMMENT '拒绝原因（审核拒绝时）',
    operator_id BIGINT COMMENT '审核人ID',
    operator_name VARCHAR(50) COMMENT '审核人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    INDEX idx_owner_id (owner_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='业主认证审核日志表';

-- =============================================
-- 新增业主管理菜单
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(300, '业主管理', 100, 7, '/park/owner', 'owner/OwnerList', 'park:owner:list', 1, 1, 1, 'UserFilled'),
(301, '业主统计', 100, 8, '/park/owner/stats', 'owner/OwnerStats', 'park:owner:stats', 1, 1, 1, 'DataLine');

-- 业主管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(311, '业主查询', 300, 1, '', NULL, 'park:owner:query', 2, 1, 1, NULL),
(312, '业主新增', 300, 2, '', NULL, 'park:owner:add', 2, 1, 1, NULL),
(313, '业主修改', 300, 3, '', NULL, 'park:owner:edit', 2, 1, 1, NULL),
(314, '业主删除', 300, 4, '', NULL, 'park:owner:delete', 2, 1, 1, NULL),
(315, '业主认证', 300, 5, '', NULL, 'park:owner:audit', 2, 1, 1, NULL),
(316, '关联房产', 300, 6, '', NULL, 'park:owner:bindProperty', 2, 1, 1, NULL),
(317, '解除关联', 300, 7, '', NULL, 'park:owner:unbindProperty', 2, 1, 1, NULL),
(318, '导入业主', 300, 8, '', NULL, 'park:owner:import', 2, 1, 1, NULL),
(319, '导出业主', 300, 9, '', NULL, 'park:owner:export', 2, 1, 1, NULL);

-- 业主统计按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(321, '统计查询', 301, 1, '', NULL, 'park:owner:statsQuery', 2, 1, 1, NULL);

-- 超级管理员拥有业主管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 300), (1, 301),
(1, 311), (1, 312), (1, 313), (1, 314), (1, 315), (1, 316), (1, 317), (1, 318), (1, 319),
(1, 321);

-- 物业管理员拥有业主管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 300), (3, 301),
(3, 311), (3, 312), (3, 313), (3, 314), (3, 315), (3, 316), (3, 317), (3, 318), (3, 319),
(3, 321);

-- 物业经理权限（查看+编辑基本信息+关联房产+认证审核+导出，不能删除和解除关联）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 300), (4, 301),
(4, 311), (4, 313), (4, 315), (4, 316), (4, 319),
(4, 321);

-- 客服人员权限（查看+编辑联系方式和备注，不能删除、认证、关联房产）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 300), (5, 301),
(5, 311),
(5, 321);

-- 财务人员权限（查看业主基本信息和房产关联+导出，不可修改）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 300), (6, 301),
(6, 311), (6, 319),
(6, 321);
