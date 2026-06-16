
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 房产表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '房产ID',
    property_code VARCHAR(50) NOT NULL COMMENT '房产编号（楼栋-房号，全园区唯一）',
    building_id BIGINT NOT NULL COMMENT '楼宇ID',
    floor_id BIGINT NOT NULL COMMENT '楼层ID',
    floor_number VARCHAR(20) COMMENT '楼层号（冗余字段，方便查询）',
    property_type TINYINT NOT NULL COMMENT '房产类型：1-住宅，2-商业，3-其他',
    property_sub_type VARCHAR(30) COMMENT '房产子类型：普通住宅/公寓/别墅/复式/loft/写字间/商铺/底商/仓库/设备间',
    building_area DECIMAL(10, 2) DEFAULT 0 COMMENT '建筑面积（㎡）',
    inside_area DECIMAL(10, 2) DEFAULT 0 COMMENT '套内面积（㎡）',
    shared_area DECIMAL(10, 2) DEFAULT 0 COMMENT '公摊面积（㎡）',
    house_type VARCHAR(20) COMMENT '户型：一室/两室/三室/四室及以上',
    orientation VARCHAR(20) COMMENT '朝向：东/南/西/北/东南/西南/东北/西北',
    decoration_status VARCHAR(20) COMMENT '装修状况：毛坯/简装/精装/豪装',
    property_right_type VARCHAR(30) COMMENT '产权性质：商品房/经济适用房/公租房/商铺产权',
    property_right_years INT COMMENT '产权年限：40/50/70',
    property_cert_no VARCHAR(100) COMMENT '产权证号',
    delivery_date DATE COMMENT '交付日期',
    status TINYINT DEFAULT 1 COMMENT '房产状态：1-待售，2-已售未交付，3-业主自住，4-业主出租，5-空置，6-装修中，7-查封',
    special_tags VARCHAR(200) COMMENT '特殊标识（逗号分隔）：学区房/人才房/拆迁房',
    remark VARCHAR(500) COMMENT '备注',
    owner_id BIGINT COMMENT '产权人ID（后续关联业主）',
    tenant_id BIGINT COMMENT '租户ID（后续关联租户）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX uk_property_code (property_code),
    INDEX idx_building_id (building_id),
    INDEX idx_floor_id (floor_id),
    INDEX idx_status (status),
    INDEX idx_property_type (property_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产表';

-- =============================================
-- 房产状态变更日志表
-- =============================================
CREATE TABLE IF NOT EXISTS park_property_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    property_id BIGINT NOT NULL COMMENT '房产ID',
    property_code VARCHAR(50) COMMENT '房产编号（冗余）',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    change_reason VARCHAR(500) COMMENT '变更原因',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_property_id (property_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房产状态变更日志表';

-- =============================================
-- 新增房产管理菜单
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(200, '房产管理', 100, 5, '/park/property', 'park/PropertyList', 'park:property:list', 1, 1, 1, 'House'),
(201, '房产统计', 100, 6, '/park/property/stats', 'park/PropertyStats', 'park:property:stats', 1, 1, 1, 'DataLine');

-- 房产管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(211, '房产查询', 200, 1, '', NULL, 'park:property:query', 2, 1, 1, NULL),
(212, '房产新增', 200, 2, '', NULL, 'park:property:add', 2, 1, 1, NULL),
(213, '房产修改', 200, 3, '', NULL, 'park:property:edit', 2, 1, 1, NULL),
(214, '房产删除', 200, 4, '', NULL, 'park:property:delete', 2, 1, 1, NULL),
(215, '批量创建', 200, 5, '', NULL, 'park:property:batchAdd', 2, 1, 1, NULL),
(216, '导入房产', 200, 6, '', NULL, 'park:property:import', 2, 1, 1, NULL),
(217, '导出房产', 200, 7, '', NULL, 'park:property:export', 2, 1, 1, NULL),
(218, '修改状态', 200, 8, '', NULL, 'park:property:changeStatus', 2, 1, 1, NULL);

-- 房产统计按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(221, '统计查询', 201, 1, '', NULL, 'park:property:statsQuery', 2, 1, 1, NULL);

-- 超级管理员拥有房产管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 200), (1, 201),
(1, 211), (1, 212), (1, 213), (1, 214), (1, 215), (1, 216), (1, 217), (1, 218),
(1, 221);

-- 物业管理员拥有房产管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 200), (3, 201),
(3, 211), (3, 212), (3, 213), (3, 214), (3, 215), (3, 216), (3, 217), (3, 218),
(3, 221);

-- 物业经理权限（查看+修改，不能删除，不能改产权证号）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 200), (4, 201),
(4, 211), (4, 213), (4, 215), (4, 217), (4, 218),
(4, 221);

-- 客服人员权限（只能查看，只能改备注）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 200), (5, 201),
(5, 211),
(5, 221);

-- 财务人员权限（查看+导出）
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 200), (6, 201),
(6, 211), (6, 217),
(6, 221);
