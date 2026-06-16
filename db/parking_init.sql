SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 车位表
-- =============================================
CREATE TABLE IF NOT EXISTS park_parking_space (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '车位ID',
    space_code VARCHAR(50) NOT NULL COMMENT '车位编号（区域-序号，全园区唯一）',
    area VARCHAR(100) NOT NULL COMMENT '所属区域：地上露天A区/地上访客区/地下B1层/地下B2层/独立车库',
    space_type VARCHAR(50) NOT NULL COMMENT '车位类型：标准车位/大型车位/微型车位/充电车位/无障碍车位',
    location_desc VARCHAR(300) COMMENT '位置描述',
    orientation VARCHAR(50) COMMENT '朝向：室内/室外/向阳/背阴',
    length DECIMAL(10,2) COMMENT '长度（米）',
    width DECIMAL(10,2) COMMENT '宽度（米）',
    property_right VARCHAR(50) NOT NULL DEFAULT '开发商' COMMENT '产权归属：开发商/业主/公共',
    owner_id BIGINT COMMENT '产权人ID（关联业主）',
    property_cert_no VARCHAR(100) COMMENT '产权证号',
    purchase_price DECIMAL(15,2) COMMENT '购买价格',
    purchase_date DATE COMMENT '购买日期',
    status TINYINT DEFAULT 1 COMMENT '使用状态：1-空闲，2-业主自用，3-已出租，4-临时占用，5-维护中，6-禁用',
    monthly_rent DECIMAL(12,2) DEFAULT 0 COMMENT '月租金标准（元）',
    hourly_fee DECIMAL(10,2) DEFAULT 0 COMMENT '临时收费（元/小时）',
    deposit DECIMAL(12,2) DEFAULT 0 COMMENT '押金金额（元）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE INDEX idx_space_code (space_code),
    INDEX idx_area (area),
    INDEX idx_space_type (space_type),
    INDEX idx_status (status),
    INDEX idx_property_right (property_right),
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车位表';

-- =============================================
-- 车位状态变更日志表
-- =============================================
CREATE TABLE IF NOT EXISTS park_parking_space_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    space_id BIGINT NOT NULL COMMENT '车位ID',
    space_code VARCHAR(50) NOT NULL COMMENT '车位编号（冗余）',
    old_status TINYINT COMMENT '原状态',
    new_status TINYINT NOT NULL COMMENT '新状态',
    change_reason VARCHAR(500) COMMENT '变更理由',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(100) COMMENT '操作人姓名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    INDEX idx_space_id (space_id),
    INDEX idx_space_code (space_code),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车位状态变更日志表';

-- =============================================
-- 车位管理菜单（ID: 600-617）
-- =============================================
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(600, '车位管理', 100, 13, '/park/parking', 'park/ParkingList', 'park:parking:list', 1, 1, 1, 'Van'),
(601, '车位统计', 100, 14, '/park/parking/stats', 'park/ParkingStats', 'park:parking:stats', 1, 1, 1, 'DataLine');

-- 车位管理按钮权限
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, perms, menu_type, visible, status, icon) VALUES
(610, '车位查询', 600, 1, '', NULL, 'park:parking:query', 2, 1, 1, NULL),
(611, '车位新增', 600, 2, '', NULL, 'park:parking:add', 2, 1, 1, NULL),
(612, '车位修改', 600, 3, '', NULL, 'park:parking:edit', 2, 1, 1, NULL),
(613, '车位删除', 600, 4, '', NULL, 'park:parking:delete', 2, 1, 1, NULL),
(614, '车位批量创建', 600, 5, '', NULL, 'park:parking:batchAdd', 2, 1, 1, NULL),
(615, '车位导入', 600, 6, '', NULL, 'park:parking:import', 2, 1, 1, NULL),
(616, '车位导出', 600, 7, '', NULL, 'park:parking:export', 2, 1, 1, NULL),
(617, '车位状态变更', 600, 8, '', NULL, 'park:parking:changeStatus', 2, 1, 1, NULL);

-- 超级管理员拥有车位管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 600), (1, 601),
(1, 610), (1, 611), (1, 612), (1, 613), (1, 614), (1, 615), (1, 616), (1, 617);

-- 物业管理员拥有车位管理所有权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 600), (3, 601),
(3, 610), (3, 611), (3, 612), (3, 613), (3, 614), (3, 615), (3, 616), (3, 617);

-- 物业经理：浏览所有车位、更新状态与属性、批量创建、导出数据，不可删除车位、不可修改产权证号与购买价格
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 600), (4, 601),
(4, 610), (4, 612), (4, 614), (4, 616), (4, 617);

-- 客服人员：浏览车位、搜索筛选、更新备注
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5, 600), (5, 601),
(5, 610);

-- 财务人员：查看车位收费信息、产权与租金数据、导出财务数据
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(6, 600), (6, 601),
(6, 610), (6, 616);

-- =============================================
-- 初始化测试车位数据
-- =============================================
-- 地上A区标准车位
INSERT INTO park_parking_space (space_code, area, space_type, location_desc, orientation, length, width, property_right, status, monthly_rent, hourly_fee, deposit, remark) VALUES
('A-001', '地上露天A区', '标准车位', 'A区入口处第1位', '室外', 5.5, 2.5, '开发商', 1, 300.00, 5.00, 500.00, '测试车位1'),
('A-002', '地上露天A区', '标准车位', 'A区入口处第2位', '室外', 5.5, 2.5, '开发商', 2, 300.00, 5.00, 500.00, '业主自用'),
('A-003', '地上露天A区', '标准车位', 'A区入口处第3位', '室外', 5.5, 2.5, '业主', 3, 300.00, 5.00, 500.00, '已出租给张先生'),
('A-004', '地上露天A区', '充电车位', 'A区充电桩区域', '室外', 5.5, 2.5, '公共', 1, 400.00, 6.00, 500.00, '配备充电桩'),
('A-005', '地上露天A区', '大型车位', 'A区大型车区域', '室外', 6.0, 3.0, '开发商', 1, 400.00, 6.00, 600.00, '适合SUV和大型车'),
('A-010', '地上露天A区', '标准车位', 'A区中部', '室外', 5.5, 2.5, '开发商', 5, 300.00, 5.00, 500.00, '维护中，地面维修');

-- 地下B1层车位
INSERT INTO park_parking_space (space_code, area, space_type, location_desc, orientation, length, width, property_right, status, monthly_rent, hourly_fee, deposit, remark) VALUES
('B1-001', '地下B1层', '标准车位', 'B1层电梯口附近', '室内', 5.5, 2.5, '开发商', 1, 500.00, 8.00, 800.00, '地下车位，位置优越'),
('B1-002', '地下B1层', '标准车位', 'B1层北区', '室内', 5.5, 2.5, '业主', 2, 500.00, 8.00, 800.00, '业主李女士购买'),
('B1-003', '地下B1层', '充电车位', 'B1层充电区', '室内', 5.5, 2.5, '公共', 4, 600.00, 10.00, 800.00, '临时占用中'),
('B1-004', '地下B1层', '微型车位', 'B1层微型车区', '室内', 5.0, 2.0, '开发商', 1, 350.00, 5.00, 500.00, '适合小型车'),
('B1-005', '地下B1层', '无障碍车位', 'B1层电梯口旁', '室内', 5.5, 3.5, '公共', 1, 0.00, 0.00, 0.00, '无障碍专用车位'),
('B1-010', '地下B1层', '标准车位', 'B1层南区', '室内', 5.5, 2.5, '开发商', 6, 500.00, 8.00, 800.00, '已禁用，待整改');
