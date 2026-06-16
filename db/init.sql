-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS permission_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE permission_system;

-- =============================================
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 角色表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_role_name (role_name),
    UNIQUE INDEX idx_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- =============================================
-- 菜单表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(200) COMMENT '组件路径',
    perms VARCHAR(100) COMMENT '权限标识',
    menu_type TINYINT DEFAULT 1 COMMENT '菜单类型：0-目录，1-菜单，2-按钮',
    visible TINYINT DEFAULT 1 COMMENT '是否显示：0-隐藏，1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    icon VARCHAR(100) COMMENT '菜单图标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单表';

-- =============================================
-- 用户角色关联表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 角色菜单关联表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- =============================================
-- 数据权限表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_data_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    scope_type TINYINT DEFAULT 1 COMMENT '权限范围：1-全部，2-自定义，3-本部门，4-本部门及以下，5-仅本人',
    custom_dept_ids VARCHAR(500) COMMENT '自定义部门ID（逗号分隔）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(1, '超级管理员', 'admin', 1, 1, '拥有系统所有权限'),
(2, '普通用户', 'user', 2, 1, '普通用户角色');

-- 初始化用户（密码: 123456，BCrypt加密）
-- 注意：实际密码会在应用启动时由DataInitializer重新加密
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '超级管理员', 'admin@example.com', '13800138000', 1),
(2, 'test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '测试用户', 'test@example.com', '13800138001', 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2);

-- 初始化菜单
-- 一级菜单：系统管理
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(1, '系统管理', 0, 1, '/system', NULL, NULL, 0, 1, 1, 'Setting'),
(2, '用户管理', 1, 1, '/system/user', 'system/User', 'system:user:list', 1, 1, 1, 'User'),
(3, '角色管理', 1, 2, '/system/role', 'system/Role', 'system:role:list', 1, 1, 1, 'Avatar'),
(4, '菜单管理', 1, 3, '/system/menu', 'system/Menu', 'system:menu:list', 1, 1, 1, 'Menu'),
(5, '数据权限', 1, 4, '/system/dataPerm', 'system/DataPermission', 'system:dataPerm:list', 1, 1, 1, 'Lock');

-- 用户管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(21, '用户查询', 2, 1, '', NULL, 'system:user:query', 2, 1, 1, NULL),
(22, '用户新增', 2, 2, '', NULL, 'system:user:add', 2, 1, 1, NULL),
(23, '用户修改', 2, 3, '', NULL, 'system:user:edit', 2, 1, 1, NULL),
(24, '用户删除', 2, 4, '', NULL, 'system:user:delete', 2, 1, 1, NULL),
(25, '重置密码', 2, 5, '', NULL, 'system:user:resetPwd', 2, 1, 1, NULL);

-- 角色管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(31, '角色查询', 3, 1, '', NULL, 'system:role:query', 2, 1, 1, NULL),
(32, '角色新增', 3, 2, '', NULL, 'system:role:add', 2, 1, 1, NULL),
(33, '角色修改', 3, 3, '', NULL, 'system:role:edit', 2, 1, 1, NULL),
(34, '角色删除', 3, 4, '', NULL, 'system:role:delete', 2, 1, 1, NULL);

-- 菜单管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(41, '菜单查询', 4, 1, '', NULL, 'system:menu:query', 2, 1, 1, NULL),
(42, '菜单新增', 4, 2, '', NULL, 'system:menu:add', 2, 1, 1, NULL),
(43, '菜单修改', 4, 3, '', NULL, 'system:menu:edit', 2, 1, 1, NULL),
(44, '菜单删除', 4, 4, '', NULL, 'system:menu:delete', 2, 1, 1, NULL);

-- 数据权限按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(51, '数据权限查询', 5, 1, '', NULL, 'system:dataPerm:query', 2, 1, 1, NULL),
(52, '数据权限编辑', 5, 2, '', NULL, 'system:dataPerm:edit', 2, 1, 1, NULL),
(53, '数据权限删除', 5, 3, '', NULL, 'system:dataPerm:delete', 2, 1, 1, NULL);

-- 超级管理员角色拥有所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5),
(1, 21), (1, 22), (1, 23), (1, 24), (1, 25),
(1, 31), (1, 32), (1, 33), (1, 34),
(1, 41), (1, 42), (1, 43), (1, 44),
(1, 51), (1, 52), (1, 53);

-- 普通用户只有查看权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), (2, 2), (2, 3),
(2, 21), (2, 31);

-- 数据权限初始化
INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(1, 1, NULL),
(2, 5, NULL);

-- =============================================
-- 园区信息表
-- =============================================
CREATE TABLE IF NOT EXISTS park_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '园区ID',
    park_name VARCHAR(100) NOT NULL COMMENT '园区名称',
    park_type TINYINT NOT NULL COMMENT '园区类型：1-住宅小区，2-商业写字楼，3-产业园区，4-综合体',
    address VARCHAR(300) COMMENT '详细地址',
    longitude DECIMAL(10, 7) COMMENT '经度',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    property_company VARCHAR(100) COMMENT '物业公司名称',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    service_hotline VARCHAR(20) COMMENT '客服热线',
    built_time DATE COMMENT '建成时间',
    land_area DECIMAL(12, 2) COMMENT '占地面积（平方米）',
    building_area DECIMAL(12, 2) COMMENT '建筑面积（平方米）',
    green_rate DECIMAL(5, 2) COMMENT '绿化率（%）',
    description TEXT COMMENT '园区简介',
    main_image VARCHAR(500) COMMENT '主图URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='园区信息表';

-- =============================================
-- 配套设施表
-- =============================================
CREATE TABLE IF NOT EXISTS park_facility (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '设施ID',
    park_id BIGINT NOT NULL COMMENT '园区ID',
    facility_name VARCHAR(50) NOT NULL COMMENT '设施名称',
    facility_type VARCHAR(50) COMMENT '设施类型',
    location VARCHAR(200) COMMENT '位置',
    open_time VARCHAR(100) COMMENT '开放时间',
    remark VARCHAR(500) COMMENT '备注',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_park_id (park_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配套设施表';

-- =============================================
-- 园区图片表
-- =============================================
CREATE TABLE IF NOT EXISTS park_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    park_id BIGINT NOT NULL COMMENT '园区ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    image_type VARCHAR(50) COMMENT '图片类型：全景图、大门、景观、配套设施',
    image_name VARCHAR(100) COMMENT '图片名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_main TINYINT DEFAULT 0 COMMENT '是否主图：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_park_id (park_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='园区图片表';

-- =============================================
-- 楼宇表
-- =============================================
CREATE TABLE IF NOT EXISTS park_building (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼宇ID',
    park_id BIGINT NOT NULL COMMENT '园区ID',
    building_name VARCHAR(50) NOT NULL COMMENT '楼宇名称',
    building_code VARCHAR(50) NOT NULL COMMENT '楼宇编号',
    building_type TINYINT NOT NULL COMMENT '楼宇类型：1-住宅楼，2-商业楼，3-综合楼',
    building_sub_type VARCHAR(50) COMMENT '楼宇子类型：普通住宅/高层住宅/别墅/写字楼/商铺/底商/商住两用',
    total_floors INT DEFAULT 0 COMMENT '地上楼层数',
    underground_floors INT DEFAULT 0 COMMENT '地下楼层数',
    structure_type VARCHAR(50) COMMENT '建筑结构：框架/砖混/钢结构',
    built_year INT COMMENT '建成年份',
    passenger_elevators INT DEFAULT 0 COMMENT '客梯数量',
    freight_elevators INT DEFAULT 0 COMMENT '货梯数量',
    status TINYINT DEFAULT 1 COMMENT '楼宇状态：1-正常使用，2-装修中，3-待交付',
    description TEXT COMMENT '楼宇简介',
    main_image VARCHAR(500) COMMENT '主图URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_building_code (building_code),
    INDEX idx_park_id (park_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼宇表';

-- =============================================
-- 楼宇图片表
-- =============================================
CREATE TABLE IF NOT EXISTS park_building_image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '图片ID',
    building_id BIGINT NOT NULL COMMENT '楼宇ID',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    image_name VARCHAR(100) COMMENT '图片名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_main TINYINT DEFAULT 0 COMMENT '是否主图：0-否，1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_building_id (building_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼宇图片表';

-- =============================================
-- 楼层表
-- =============================================
CREATE TABLE IF NOT EXISTS park_floor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '楼层ID',
    building_id BIGINT NOT NULL COMMENT '楼宇ID',
    floor_number VARCHAR(20) NOT NULL COMMENT '楼层号',
    floor_type TINYINT NOT NULL COMMENT '楼层类型：1-住宅楼层，2-商业楼层，3-停车楼层，4-设备楼层',
    floor_purpose VARCHAR(50) COMMENT '楼层用途：居住、办公、商业、停车、设备间、架空层',
    property_count INT DEFAULT 0 COMMENT '房产数量',
    floor_plan VARCHAR(500) COMMENT '楼层平面图URL',
    status TINYINT DEFAULT 1 COMMENT '楼层状态：1-正常，2-改造中，3-封闭',
    remark VARCHAR(500) COMMENT '备注',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_building_id (building_id),
    UNIQUE KEY uk_building_floor (building_id, floor_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='楼层表';

-- =============================================
-- 新增物业相关角色
-- =============================================
INSERT INTO sys_role (id, role_name, role_key, order_num, status, remark) VALUES
(3, '物业管理员', 'property_admin', 3, 1, '物业管理员，拥有园区管理全部权限'),
(4, '物业经理', 'property_manager', 4, 1, '物业经理，拥有查看和部分修改权限'),
(5, '客服人员', 'customer_service', 5, 1, '客服人员，拥有查看权限'),
(6, '财务人员', 'finance_staff', 6, 1, '财务人员，拥有财务相关查看权限');

-- =============================================
-- 新增物业相关测试用户
-- =============================================
INSERT INTO sys_user (id, username, password, nickname, email, phone, status) VALUES
(3, 'property_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '物业管理员', 'property_admin@example.com', '13800138002', 1),
(4, 'property_manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '物业经理', 'property_manager@example.com', '13800138003', 1),
(5, 'customer_service', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '客服人员', 'customer_service@example.com', '13800138004', 1),
(6, 'finance_staff', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtE/ETXmB5nNiHxqHnHfgVd5GK6', '财务人员', 'finance_staff@example.com', '13800138005', 1);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(3, 3),
(4, 4),
(5, 5),
(6, 6);

-- =============================================
-- 新增园区管理菜单
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(100, '园区管理', 0, 2, '/park', NULL, NULL, 0, 1, 1, 'OfficeBuilding'),
(101, '园区概览', 100, 1, '/park/overview', 'park/Overview', 'park:overview:list', 1, 1, 1, 'HomeFilled'),
(102, '楼宇管理', 100, 2, '/park/building', 'park/Building', 'park:building:list', 1, 1, 1, 'OfficeBuilding'),
(103, '楼层管理', 100, 3, '/park/floor', 'park/Floor', 'park:floor:list', 1, 1, 1, 'Grid'),
(104, '统计看板', 100, 4, '/park/dashboard', 'park/Dashboard', 'park:dashboard:list', 1, 1, 1, 'DataAnalysis');

-- 园区概览按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(111, '园区查询', 101, 1, '', NULL, 'park:overview:query', 2, 1, 1, NULL),
(112, '园区新增', 101, 2, '', NULL, 'park:overview:add', 2, 1, 1, NULL),
(113, '园区修改', 101, 3, '', NULL, 'park:overview:edit', 2, 1, 1, NULL),
(114, '园区删除', 101, 4, '', NULL, 'park:overview:delete', 2, 1, 1, NULL);

-- 楼宇管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(121, '楼宇查询', 102, 1, '', NULL, 'park:building:query', 2, 1, 1, NULL),
(122, '楼宇新增', 102, 2, '', NULL, 'park:building:add', 2, 1, 1, NULL),
(123, '楼宇修改', 102, 3, '', NULL, 'park:building:edit', 2, 1, 1, NULL),
(124, '楼宇删除', 102, 4, '', NULL, 'park:building:delete', 2, 1, 1, NULL);

-- 楼层管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(131, '楼层查询', 103, 1, '', NULL, 'park:floor:query', 2, 1, 1, NULL),
(132, '楼层新增', 103, 2, '', NULL, 'park:floor:add', 2, 1, 1, NULL),
(133, '楼层修改', 103, 3, '', NULL, 'park:floor:edit', 2, 1, 1, NULL),
(134, '楼层删除', 103, 4, '', NULL, 'park:floor:delete', 2, 1, 1, NULL);

-- 统计看板按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(141, '统计查询', 104, 1, '', NULL, 'park:dashboard:query', 2, 1, 1, NULL);

-- =============================================
-- 超级管理员拥有所有菜单权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 100), (1, 101), (1, 102), (1, 103), (1, 104),
(1, 111), (1, 112), (1, 113), (1, 114),
(1, 121), (1, 122), (1, 123), (1, 124),
(1, 131), (1, 132), (1, 133), (1, 134),
(1, 141);

-- =============================================
-- 物业管理员权限
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 100), (3, 101), (3, 102), (3, 103), (3, 104),
(3, 111), (3, 112), (3, 113), (3, 114),
(3, 121), (3, 122), (3, 123), (3, 124),
(3, 131), (3, 132), (3, 133), (3, 134),
(3, 141);

-- =============================================
-- 物业经理权限（查看+部分修改，不能删除楼宇和楼层）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 100), (4, 101), (4, 102), (4, 103), (4, 104),
(4, 111), (4, 113),
(4, 121), (4, 123),
(4, 131), (4, 133),
(4, 141);

-- =============================================
-- 客服人员权限（只能查看，不能修改基础数据）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 100), (5, 101), (5, 102), (5, 103), (5, 104),
(5, 111),
(5, 121),
(5, 131),
(5, 141);

-- =============================================
-- 财务人员权限（查看房产和车位信息）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 100), (6, 101), (6, 102), (6, 103), (6, 104),
(6, 111),
(6, 121),
(6, 131),
(6, 141);

-- =============================================
-- 数据权限初始化（物业角色）
-- =============================================
INSERT INTO sys_data_permission (role_id, scope_type, custom_dept_ids) VALUES
(3, 1, NULL),
(4, 1, NULL),
(5, 1, NULL),
(6, 1, NULL);

-- =============================================
-- 初始化示例园区数据
-- =============================================
INSERT INTO park_info (id, park_name, park_type, address, longitude, latitude, property_company, contact_phone, service_hotline, built_time, land_area, building_area, green_rate, description, main_image) VALUES
(1, '阳光花园小区', 1, '北京市朝阳区阳光路100号', 116.4074000, 39.9042000, '阳光物业服务有限公司', '010-88888888', '400-888-8888', '2020-06-01', 50000.00, 120000.00, 35.50, '阳光花园是一个高品质住宅小区，环境优美，配套完善。', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800');

-- 初始化配套设施
INSERT INTO park_facility (park_id, facility_name, facility_type, location, open_time, remark, sort_order) VALUES
(1, '地下停车场', '停车场', '小区地下一层', '24小时', '共500个车位', 1),
(1, '健身中心', '健身房', '小区会所一层', '06:00-22:00', '配备专业健身器材', 2),
(1, '游泳池', '游泳池', '小区会所二层', '09:00-21:00', '标准游泳池', 3),
(1, '社区超市', '超市', '小区东门', '07:00-22:00', '日常生活用品齐全', 4),
(1, '幼儿园', '幼儿园', '小区西北角', '07:30-17:30', '双语幼儿园', 5),
(1, '物业服务中心', '物业服务中心', '小区南门', '08:30-18:00', '办理物业相关业务', 6);

-- 初始化园区图片
INSERT INTO park_image (park_id, image_url, image_type, image_name, sort_order, is_main) VALUES
(1, 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=800', '全景图', '小区全景', 1, 1),
(1, 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800', '大门', '小区正门', 2, 0),
(1, 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=800', '景观', '中心花园', 3, 0);

-- 初始化楼宇数据
INSERT INTO park_building (id, park_id, building_name, building_code, building_type, building_sub_type, total_floors, underground_floors, structure_type, built_year, passenger_elevators, freight_elevators, status, description, main_image) VALUES
(1, 1, '1号楼', 'BLD-001', 1, '普通住宅', 18, 2, '框架', 2020, 2, 0, 1, '1号楼为高层住宅楼，南北通透，采光良好。', 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=600'),
(2, 1, '2号楼', 'BLD-002', 1, '高层住宅', 25, 2, '框架', 2020, 3, 0, 1, '2号楼为超高层住宅楼，视野开阔。', 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600'),
(3, 1, '3号楼', 'BLD-003', 3, '商住两用', 15, 1, '框架', 2021, 2, 1, 1, '3号楼为综合楼，1-3层为商业，4-15层为住宅。', 'https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=600'),
(4, 1, '商业中心', 'BLD-004', 2, '商铺', 3, 0, '钢结构', 2021, 2, 1, 1, '小区配套商业中心，集购物、餐饮、娱乐于一体。', 'https://images.unsplash.com/photo-1519999482648-25049ddd37b1?w=600');

-- 初始化楼宇图片
INSERT INTO park_building_image (building_id, image_url, image_name, sort_order, is_main) VALUES
(1, 'https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?w=600', '1号楼外观', 1, 1),
(2, 'https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?w=600', '2号楼外观', 1, 1),
(3, 'https://images.unsplash.com/photo-1541888946425-d81bb19240f5?w=600', '3号楼外观', 1, 1),
(4, 'https://images.unsplash.com/photo-1519999482648-25049ddd37b1?w=600', '商业中心', 1, 1);

-- 初始化楼层数据
-- 1号楼
INSERT INTO park_floor (building_id, floor_number, floor_type, floor_purpose, property_count, status, remark, sort_order) VALUES
(1, '-2层', 3, '停车', 0, 1, '地下二层停车场', -2),
(1, '-1层', 3, '停车', 0, 1, '地下一层停车场', -1),
(1, '1层', 1, '居住', 4, 1, '住宅楼层', 1),
(1, '2层', 1, '居住', 4, 1, '住宅楼层', 2),
(1, '3层', 1, '居住', 4, 1, '住宅楼层', 3),
(1, '4层', 1, '居住', 4, 1, '住宅楼层', 4),
(1, '5层', 1, '居住', 4, 1, '住宅楼层', 5),
(1, '6层', 1, '居住', 4, 1, '住宅楼层', 6),
(1, '7层', 1, '居住', 4, 1, '住宅楼层', 7),
(1, '8层', 1, '居住', 4, 1, '住宅楼层', 8),
(1, '9层', 1, '居住', 4, 1, '住宅楼层', 9),
(1, '10层', 1, '居住', 4, 1, '住宅楼层', 10),
(1, '11层', 1, '居住', 4, 1, '住宅楼层', 11),
(1, '12层', 1, '居住', 4, 1, '住宅楼层', 12),
(1, '13层', 1, '居住', 4, 1, '住宅楼层', 13),
(1, '14层', 1, '居住', 4, 1, '住宅楼层', 14),
(1, '15层', 1, '居住', 4, 1, '住宅楼层', 15),
(1, '16层', 1, '居住', 4, 1, '住宅楼层', 16),
(1, '17层', 1, '居住', 4, 1, '住宅楼层', 17),
(1, '18层', 1, '居住', 4, 1, '顶层住宅楼层', 18);

-- 2号楼
INSERT INTO park_floor (building_id, floor_number, floor_type, floor_purpose, property_count, status, remark, sort_order) VALUES
(2, '-2层', 3, '停车', 0, 1, '地下二层停车场', -2),
(2, '-1层', 3, '停车', 0, 1, '地下一层停车场', -1),
(2, '1层', 1, '居住', 6, 1, '住宅楼层', 1),
(2, '2层', 1, '居住', 6, 1, '住宅楼层', 2),
(2, '3层', 1, '居住', 6, 1, '住宅楼层', 3),
(2, '4层', 1, '居住', 6, 1, '住宅楼层', 4),
(2, '5层', 1, '居住', 6, 1, '住宅楼层', 5),
(2, '10层', 1, '居住', 6, 1, '住宅楼层', 10),
(2, '20层', 1, '居住', 6, 1, '住宅楼层', 20),
(2, '25层', 1, '居住', 6, 1, '顶层住宅楼层', 25);

-- 3号楼（商住两用）
INSERT INTO park_floor (building_id, floor_number, floor_type, floor_purpose, property_count, status, remark, sort_order) VALUES
(3, '-1层', 3, '停车', 0, 1, '地下一层停车场', -1),
(3, '1层', 2, '商业', 10, 1, '底商楼层', 1),
(3, '2层', 2, '商业', 10, 1, '商业楼层', 2),
(3, '3层', 2, '商业', 8, 1, '商业楼层', 3),
(3, '4层', 1, '居住', 4, 1, '住宅楼层', 4),
(3, '5层', 1, '居住', 4, 1, '住宅楼层', 5),
(3, '10层', 1, '居住', 4, 1, '住宅楼层', 10),
(3, '15层', 1, '居住', 4, 1, '顶层住宅楼层', 15);

-- 4号楼（商业楼）
INSERT INTO park_floor (building_id, floor_number, floor_type, floor_purpose, property_count, status, remark, sort_order) VALUES
(4, '1层', 2, '商业', 15, 1, '底商楼层，主要为餐饮、零售', 1),
(4, '2层', 2, '商业', 12, 1, '二层商业，主要为服装、配饰', 2),
(4, '3层', 2, '商业', 8, 1, '三层商业，主要为娱乐、亲子', 3);
