
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE permission_system;

-- =============================================
-- 房产表新增字段
-- =============================================
-- 检查并添加字段（兼容重复执行）
SET @dbname = 'permission_system';
SET @tablename = 'park_property';

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'vacant_since') > 0,
  'SELECT 1',
  'ALTER TABLE park_property ADD COLUMN vacant_since DATE COMMENT ''空置开始日期'' AFTER status'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'vacancy_reason') > 0,
  'SELECT 1',
  'ALTER TABLE park_property ADD COLUMN vacancy_reason VARCHAR(50) COMMENT ''空置原因：装修中/待出租/待出售/业主外地/其他'' AFTER vacant_since'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'vacancy_reason_remark') > 0,
  'SELECT 1',
  'ALTER TABLE park_property ADD COLUMN vacancy_reason_remark VARCHAR(200) COMMENT ''空置原因备注（其他时填写）'' AFTER vacancy_reason'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = 'is_long_term_vacant') > 0,
  'SELECT 1',
  'ALTER TABLE park_property ADD COLUMN is_long_term_vacant TINYINT DEFAULT 0 COMMENT ''是否长期空置：0-否，1-是（空置超过6个月）'' AFTER vacancy_reason_remark'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- =============================================
-- 批量操作批次表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_batch_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '批次ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号（BATCH+时间戳）',
    batch_type TINYINT NOT NULL COMMENT '批次类型：1-批量修改状态，2-批量修改属性',
    target_status TINYINT COMMENT '目标状态（批量改状态时）',
    target_decoration_status VARCHAR(20) COMMENT '目标装修状况（批量改属性时）',
    target_special_tags VARCHAR(200) COMMENT '目标特殊标识（批量改属性时）',
    operate_reason VARCHAR(500) NOT NULL COMMENT '操作原因',
    total_count INT DEFAULT 0 COMMENT '总房产数',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    skip_count INT DEFAULT 0 COMMENT '跳过数量',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_batch_no (batch_no),
    INDEX idx_operator_id (operator_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产批量操作批次表';

-- =============================================
-- 批量操作明细表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_batch_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    batch_id BIGINT NOT NULL COMMENT '批次ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号（冗余）',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT COMMENT '新状态',
    old_decoration_status VARCHAR(20) COMMENT '原装修状况',
    new_decoration_status VARCHAR(20) COMMENT '新装修状况',
    old_special_tags VARCHAR(200) COMMENT '原特殊标识',
    new_special_tags VARCHAR(200) COMMENT '新特殊标识',
    result TINYINT NOT NULL COMMENT '操作结果：1-成功，2-失败，3-跳过',
    fail_reason VARCHAR(500) COMMENT '失败/跳过原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_batch_id (batch_id),
    INDEX idx_property_id (property_id),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产批量操作明细表';

-- =============================================
-- 房产转让申请表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_transfer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '转让ID',
    transfer_no VARCHAR(50) NOT NULL COMMENT '转让编号（TR+时间戳）',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    old_owner_id BIGINT NOT NULL COMMENT '原业主ID',
    old_owner_name VARCHAR(50) COMMENT '原业主姓名（冗余）',
    new_owner_id BIGINT NOT NULL COMMENT '新业主ID',
    new_owner_name VARCHAR(50) COMMENT '新业主姓名（冗余）',
    transfer_type TINYINT NOT NULL COMMENT '转让方式：1-买卖，2-赠与，3-继承',
    transfer_price DECIMAL(15, 2) COMMENT '转让价格（万元，买卖时必填）',
    transfer_date DATE NOT NULL COMMENT '转让日期',
    new_property_cert_no VARCHAR(100) COMMENT '新产权证号',
    transfer_agreement_url VARCHAR(500) COMMENT '转让协议扫描件URL',
    new_cert_url VARCHAR(500) COMMENT '新产权证扫描件URL',
    audit_status TINYINT DEFAULT 1 COMMENT '审核状态：1-待审核，2-审核通过，3-审核拒绝',
    audit_opinion VARCHAR(500) COMMENT '审核意见',
    auditor_id BIGINT COMMENT '审核人ID',
    auditor_name VARCHAR(50) COMMENT '审核人姓名',
    audit_time DATETIME COMMENT '审核时间',
    apply_operator_id BIGINT COMMENT '申请人ID',
    apply_operator_name VARCHAR(50) COMMENT '申请人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX uk_transfer_no (transfer_no),
    INDEX idx_property_id (property_id),
    INDEX idx_old_owner_id (old_owner_id),
    INDEX idx_new_owner_id (new_owner_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产转让申请表';

-- =============================================
-- 房产标签库表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_color VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_tag_name (tag_name),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产标签库表';

-- =============================================
-- 房产标签关联表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_tag_rel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE INDEX uk_property_tag (property_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产标签关联表';

-- =============================================
-- 空置记录表（记录每次空置的起止时间）
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_vacancy_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    start_date DATE NOT NULL COMMENT '空置开始日期',
    end_date DATE COMMENT '空置结束日期',
    vacancy_days INT COMMENT '空置天数',
    vacancy_reason VARCHAR(50) COMMENT '空置原因',
    vacancy_reason_remark VARCHAR(200) COMMENT '空置原因备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_property_id (property_id),
    INDEX idx_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产空置记录表';

-- =============================================
-- 初始化标签数据
-- =============================================
INSERT INTO park_property_tag (tag_name, tag_color, sort_order) VALUES
('学区房', '#F56C6C', 1),
('地铁房', '#67C23A', 2),
('精装修', '#E6A23C', 3),
('采光好', '#409EFF', 4),
('楼王', '#909399', 5),
('景观房', '#8e44ad', 6),
('南北通透', '#16a085', 7),
('满五唯一', '#d35400', 8);

-- =============================================
-- 新增菜单和权限
-- =============================================

-- 批量操作相关按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(231, '批量修改状态', 200, 9, '', NULL, 'park:property:batchStatus', 2, 1, 1, NULL),
(232, '批量修改属性', 200, 10, '', NULL, 'park:property:batchAttr', 2, 1, 1, NULL),
(233, '批量操作日志', 200, 11, '', NULL, 'park:property:batchLog', 2, 1, 1, NULL),
(234, '房产对比', 200, 12, '', NULL, 'park:property:compare', 2, 1, 1, NULL),
(235, '设置标签', 200, 13, '', NULL, 'park:property:setTag', 2, 1, 1, NULL),
(236, '记录空置原因', 200, 14, '', NULL, 'park:property:setVacancy', 2, 1, 1, NULL);

-- 转让相关菜单和权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(400, '房产转让', 100, 9, '/park/transfer', 'park/TransferList', 'park:transfer:list', 1, 1, 1, 'Switch'),
(401, '转让审核', 100, 10, '/park/transfer/audit', 'park/TransferAudit', 'park:transfer:audit', 1, 1, 1, 'CircleCheck');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(411, '转让查询', 400, 1, '', NULL, 'park:transfer:query', 2, 1, 1, NULL),
(412, '发起转让', 400, 2, '', NULL, 'park:transfer:apply', 2, 1, 1, NULL),
(413, '转让审核查询', 401, 1, '', NULL, 'park:transfer:auditQuery', 2, 1, 1, NULL),
(414, '审核通过', 401, 2, '', NULL, 'park:transfer:auditPass', 2, 1, 1, NULL),
(415, '审核拒绝', 401, 3, '', NULL, 'park:transfer:auditReject', 2, 1, 1, NULL);

-- 标签管理菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(500, '标签管理', 100, 11, '/park/tag', 'park/TagManage', 'park:tag:list', 1, 1, 1, 'PriceTag');

INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(511, '标签查询', 500, 1, '', NULL, 'park:tag:query', 2, 1, 1, NULL),
(512, '标签新增', 500, 2, '', NULL, 'park:tag:add', 2, 1, 1, NULL),
(513, '标签修改', 500, 3, '', NULL, 'park:tag:edit', 2, 1, 1, NULL),
(514, '标签删除', 500, 4, '', NULL, 'park:tag:delete', 2, 1, 1, NULL);

-- 空置房分析（在房产统计中已存在，增加空置分析按钮权限）
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(222, '空置分析', 201, 2, '', NULL, 'park:property:vacancyStats', 2, 1, 1, NULL),
(223, '导出空置清单', 201, 3, '', NULL, 'park:property:vacancyExport', 2, 1, 1, NULL);

-- 对比页面路由
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(202, '房产对比', 100, 12, '/park/property/compare', 'park/PropertyCompare', 'park:property:compare', 1, 0, 1, 'ScaleToOriginal');

-- =============================================
-- 角色权限分配
-- =============================================

-- 超级管理员(1)和物业管理员(3)：所有新权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 231), (1, 232), (1, 233), (1, 234), (1, 235), (1, 236),
(1, 222), (1, 223), (1, 202),
(1, 400), (1, 401), (1, 411), (1, 412), (1, 413), (1, 414), (1, 415),
(1, 500), (1, 511), (1, 512), (1, 513), (1, 514),
(3, 231), (3, 232), (3, 233), (3, 234), (3, 235), (3, 236),
(3, 222), (3, 223), (3, 202),
(3, 400), (3, 401), (3, 411), (3, 412), (3, 413), (3, 414), (3, 415),
(3, 500), (3, 511), (3, 512), (3, 513), (3, 514);

-- 物业经理(4)：批量修改状态/属性、审核转让、查看空置统计、打标签、记录空置原因
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 231), (4, 232), (4, 233), (4, 234), (4, 235), (4, 236),
(4, 222), (4, 223),
(4, 400), (4, 401), (4, 411), (4, 412), (4, 413), (4, 414), (4, 415),
(4, 511);

-- 客服人员(5)：发起转让、记录空置原因、打标签、查看
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 234), (5, 235), (5, 236),
(5, 222),
(5, 400), (5, 411), (5, 412),
(5, 511);

-- 财务人员(6)：查看房产信息和转让历史、导出空置清单
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 234),
(6, 222), (6, 223),
(6, 400), (6, 411),
(6, 511);
