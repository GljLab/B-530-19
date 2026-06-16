
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 房产测试数据
-- =============================================
-- 注意：执行前请确保已执行 property_init.sql 创建了表结构和菜单权限
-- =============================================

-- =============================================
-- 1号楼房产数据（高层住宅楼，1-18层，每层4套）
-- =============================================
-- 1层 (floor_id需要根据实际数据调整，假设1层1号楼是第3条记录，id=3)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('1-101', 1, 3, '1层', 1, '普通住宅', 98.50, 78.80, 19.70, '两室', '南北', '毛坯', '商品房', 70, 'FDC-2024-0001', '2024-06-30', 3, '学区房', '业主自住，精装修'),
('1-102', 1, 3, '1层', 1, '普通住宅', 85.60, 68.50, 17.10, '两室', '南', '简装', '商品房', 70, 'FDC-2024-0002', '2024-06-30', 4, '', '出租中，租金2500元/月'),
('1-103', 1, 3, '1层', 1, '普通住宅', 102.30, 82.00, 20.30, '三室', '南北', '精装', '商品房', 70, 'FDC-2024-0003', '2024-06-30', 5, '', '空置中，待出租'),
('1-104', 1, 3, '1层', 1, '普通住宅', 75.20, 60.20, 15.00, '一室', '东', '毛坯', '商品房', 70, 'FDC-2024-0004', '2024-06-30', 1, '人才房', '待售，人才公寓');

-- 2层 (floor_id=4)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('1-201', 1, 4, '2层', 1, '普通住宅', 98.50, 78.80, 19.70, '两室', '南北', '精装', '商品房', 70, 'FDC-2024-0005', '2024-06-30', 3, '学区房', ''),
('1-202', 1, 4, '2层', 1, '普通住宅', 85.60, 68.50, 17.10, '两室', '南', '豪装', '商品房', 70, 'FDC-2024-0006', '2024-06-30', 3, '', '豪华装修，业主自住'),
('1-203', 1, 4, '2层', 1, '普通住宅', 102.30, 82.00, 20.30, '三室', '南北', '简装', '经济适用房', 70, 'FDC-2024-0007', '2024-06-30', 5, '拆迁房', '空置，拆迁安置房'),
('1-204', 1, 4, '2层', 1, '公寓', 65.00, 52.00, 13.00, '一室', '西', '精装', '商品房', 70, 'FDC-2024-0008', '2024-06-30', 4, '', '出租给白领');

-- 5层 (floor_id=7)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('1-501', 1, 7, '5层', 1, '普通住宅', 120.80, 96.60, 24.20, '三室', '南北', '精装', '商品房', 70, 'FDC-2024-0009', '2024-06-30', 3, '学区房', '黄金楼层，学区房'),
('1-502', 1, 7, '5层', 1, '普通住宅', 95.50, 76.40, 19.10, '两室', '南', '毛坯', '商品房', 70, 'FDC-2024-0010', '2024-06-30', 2, '', '已售未交付'),
('1-503', 1, 7, '5层', 1, '普通住宅', 118.00, 94.40, 23.60, '三室', '东南', '简装', '商品房', 70, 'FDC-2024-0011', '2024-06-30', 6, '', '正在装修中'),
('1-504', 1, 7, '5层', 1, '复式', 156.00, 125.00, 31.00, '四室及以上', '南北', '豪装', '商品房', 70, 'FDC-2024-0012', '2024-06-30', 3, '学区房', '顶层复式，豪华装修');

-- 10层 (floor_id=12)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('1-1001', 1, 12, '10层', 1, '普通住宅', 98.50, 78.80, 19.70, '两室', '南', '精装', '商品房', 70, 'FDC-2024-0013', '2024-06-30', 3, '', ''),
('1-1002', 1, 12, '10层', 1, '普通住宅', 85.60, 68.50, 17.10, '两室', '西南', '简装', '公租房', 70, NULL, '2024-06-30', 4, '', '公租房出租中'),
('1-1003', 1, 12, '10层', 1, '普通住宅', 102.30, 82.00, 20.30, '三室', '南北', '毛坯', '商品房', 70, 'FDC-2024-0014', '2024-06-30', 1, '', '待售'),
('1-1004', 1, 12, '10层', 1, '普通住宅', 75.20, 60.20, 15.00, '一室', '东北', '简装', '商品房', 70, 'FDC-2024-0015', '2024-06-30', 7, '', '司法查封');

-- 15层 (floor_id=17)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('1-1501', 1, 17, '15层', 1, '普通住宅', 120.80, 96.60, 24.20, '三室', '南北', '豪装', '商品房', 70, 'FDC-2024-0016', '2024-06-30', 3, '学区房', '高楼层，视野好'),
('1-1502', 1, 17, '15层', 1, '公寓', 55.00, 44.00, 11.00, '一室', '南', '精装', '商品房', 70, 'FDC-2024-0017', '2024-06-30', 4, '', '酒店式公寓出租'),
('1-1503', 1, 17, '15层', 1, '普通住宅', 102.30, 82.00, 20.30, '三室', '东南', '毛坯', '商品房', 70, NULL, '2024-06-30', 2, '人才房', '已售未交付，人才房');

-- =============================================
-- 2号楼房产数据（超高层住宅楼，25层，每层6套）
-- 只插入部分楼层示例数据
-- =============================================
-- 1层 (floor_id=21)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('2-101', 2, 21, '1层', 1, '普通住宅', 110.00, 88.00, 22.00, '三室', '南北', '简装', '商品房', 70, 'FDC-2024-0101', '2025-12-31', 1, '', '待售'),
('2-102', 2, 21, '1层', 1, '普通住宅', 90.50, 72.40, 18.10, '两室', '南', '毛坯', '商品房', 70, 'FDC-2024-0102', '2025-12-31', 1, '', '待售'),
('2-103', 2, 21, '1层', 1, '普通住宅', 95.00, 76.00, 19.00, '两室', '东南', '毛坯', '商品房', 70, 'FDC-2024-0103', '2025-12-31', 2, '', '已售未交付');

-- 10层 (floor_id=24)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('2-1001', 2, 24, '10层', 1, '普通住宅', 125.00, 100.00, 25.00, '三室', '南北', '精装', '商品房', 70, 'FDC-2024-0104', '2025-12-31', 3, '学区房', ''),
('2-1002', 2, 24, '10层', 1, '普通住宅', 95.00, 76.00, 19.00, '两室', '南', '简装', '商品房', 70, 'FDC-2024-0105', '2025-12-31', 4, '', '出租中'),
('2-1003', 2, 24, '10层', 1, '普通住宅', 88.00, 70.40, 17.60, '两室', '西南', '毛坯', '经济适用房', 70, 'FDC-2024-0106', '2025-12-31', 5, '拆迁房', '空置');

-- 20层 (floor_id=25)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('2-2001', 2, 25, '20层', 1, 'loft', 80.00, 64.00, 16.00, '两室', '南北', '豪装', '商品房', 70, 'FDC-2024-0107', '2025-12-31', 3, '', 'LOFT户型，挑高设计'),
('2-2002', 2, 25, '20层', 1, '普通住宅', 130.00, 104.00, 26.00, '四室及以上', '南北', '精装', '商品房', 70, 'FDC-2024-0108', '2025-12-31', 3, '学区房', '大户型四居'),
('2-2003', 2, 25, '20层', 1, '普通住宅', 95.00, 76.00, 19.00, '两室', '东', '简装', '商品房', 70, 'FDC-2024-0109', '2025-12-31', 6, '', '装修中');

-- =============================================
-- 3号楼房产数据（商住两用，1-3层商业，4层以上住宅）
-- =============================================
-- 1层商业 (floor_id=28)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('3-101', 3, 28, '1层', 2, '商铺', 120.00, 96.00, 24.00, NULL, '南', '简装', '商铺产权', 40, 'SY-2024-0001', '2024-12-31', 4, '', '底商商铺，出租给便利店'),
('3-102', 3, 28, '1层', 2, '商铺', 85.00, 68.00, 17.00, NULL, '南', '精装', '商铺产权', 40, 'SY-2024-0002', '2024-12-31', 4, '', '餐饮店铺出租中'),
('3-103', 3, 28, '1层', 2, '底商', 65.00, 52.00, 13.00, NULL, '东南', '毛坯', '商铺产权', 40, 'SY-2024-0003', '2024-12-31', 5, '', '空置商铺'),
('3-105', 3, 28, '1层', 2, '商铺', 200.00, 160.00, 40.00, NULL, '南', '豪装', '商铺产权', 40, 'SY-2024-0005', '2024-12-31', 3, '', '大型超市，业主自营');

-- 4层住宅 (floor_id=31)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('3-401', 3, 31, '4层', 1, '普通住宅', 105.00, 84.00, 21.00, '三室', '南北', '精装', '商品房', 70, 'FDC-2024-0201', '2024-12-31', 3, '', '商住两用楼住宅'),
('3-402', 3, 31, '4层', 1, '公寓', 55.00, 44.00, 11.00, '一室', '南', '简装', '商品房', 70, 'FDC-2024-0202', '2024-12-31', 4, '', '公寓出租');

-- =============================================
-- 4号楼房产数据（纯商业楼，1-3层商业）
-- =============================================
-- 1层商业 (floor_id=34)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('4-101', 4, 34, '1层', 2, '商铺', 150.00, 120.00, 30.00, NULL, '南', '精装', '商铺产权', 40, 'SY-2024-0101', '2023-12-31', 4, '', '品牌服装店'),
('4-102', 4, 34, '1层', 2, '商铺', 200.00, 160.00, 40.00, NULL, '南', '豪装', '商铺产权', 40, 'SY-2024-0102', '2023-12-31', 4, '', '连锁餐饮店'),
('4-103', 4, 34, '1层', 2, '底商', 80.00, 64.00, 16.00, NULL, '东', '简装', '商铺产权', 40, 'SY-2024-0103', '2023-12-31', 5, '', '空置旺铺');

-- 2层商业 (floor_id=35)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('4-201', 4, 35, '2层', 2, '写字间', 180.00, 144.00, 36.00, NULL, '南北', '精装', '商铺产权', 50, 'SY-2024-0201', '2023-12-31', 4, '', '写字楼出租，科技公司'),
('4-202', 4, 35, '2层', 2, '写字间', 120.00, 96.00, 24.00, NULL, '南', '简装', '商铺产权', 50, 'SY-2024-0202', '2023-12-31', 5, '', '空置写字楼'),
('4-203', 4, 35, '2层', 2, '商铺', 250.00, 200.00, 50.00, NULL, '南北', '豪装', '商铺产权', 50, 'SY-2024-0203', '2023-12-31', 3, '', '大型餐饮，业主自营');

-- 3层商业 (floor_id=36)
INSERT INTO park_property (property_code, building_id, floor_id, floor_number, property_type, property_sub_type, building_area, inside_area, shared_area, house_type, orientation, decoration_status, property_right_type, property_right_years, property_cert_no, delivery_date, status, special_tags, remark) VALUES
('4-301', 4, 36, '3层', 2, '写字间', 300.00, 240.00, 60.00, NULL, '南北', '精装', '商铺产权', 50, 'SY-2024-0301', '2023-12-31', 4, '', '整层办公，金融公司'),
('4-302', 4, 36, '3层', 3, '仓库', 500.00, 450.00, 50.00, NULL, '北', '毛坯', '商铺产权', 50, 'SY-2024-0302', '2023-12-31', 4, '', '仓储出租'),
('4-303', 4, 36, '3层', 3, '设备间', 100.00, 90.00, 10.00, NULL, '北', '毛坯', '商铺产权', 50, NULL, '2023-12-31', 5, '', '设备间，公共设施');

-- =============================================
-- 状态变更日志示例数据
-- =============================================
INSERT INTO park_property_status_log (property_id, property_code, old_status, new_status, change_reason, operator_id, operator_name, create_time) VALUES
(1, '1-101', 1, 2, '房屋售出', 1, '超级管理员', '2024-03-15 10:30:00'),
(1, '1-101', 2, 3, '业主收房入住', 1, '超级管理员', '2024-07-01 09:00:00'),
(2, '1-102', 1, 2, '房屋售出', 1, '超级管理员', '2024-04-20 14:00:00'),
(2, '1-102', 2, 3, '业主收房', 1, '超级管理员', '2024-07-05 10:00:00'),
(2, '1-102', 3, 4, '业主出租', 1, '超级管理员', '2024-08-01 09:30:00'),
(9, '1-502', 1, 2, '房屋售出', 1, '超级管理员', '2025-01-10 15:00:00'),
(11, '1-504', 1, 3, '业主自住', 1, '超级管理员', '2024-10-01 11:00:00'),
(16, '1-1002', 1, 4, '公租房配租', 1, '超级管理员', '2024-09-01 08:00:00'),
(24, '2-1001', 1, 2, '房屋售出', 1, '超级管理员', '2024-12-01 10:00:00'),
(24, '2-1001', 2, 3, '业主收房入住', 1, '超级管理员', '2025-01-15 14:00:00'),
(30, '3-101', 1, 4, '商铺出租', 1, '超级管理员', '2025-01-01 09:00:00'),
(33, '3-401', 1, 3, '业主自住', 1, '超级管理员', '2025-02-01 10:00:00');

-- =============================================
-- 更新楼层房产数量统计
-- =============================================
UPDATE park_floor SET property_count = 4 WHERE building_id = 1 AND floor_number = '1层';
UPDATE park_floor SET property_count = 4 WHERE building_id = 1 AND floor_number = '2层';
UPDATE park_floor SET property_count = 4 WHERE building_id = 1 AND floor_number = '5层';
UPDATE park_floor SET property_count = 4 WHERE building_id = 1 AND floor_number = '10层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 1 AND floor_number = '15层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 2 AND floor_number = '1层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 2 AND floor_number = '10层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 2 AND floor_number = '20层';
UPDATE park_floor SET property_count = 4 WHERE building_id = 3 AND floor_number = '1层';
UPDATE park_floor SET property_count = 2 WHERE building_id = 3 AND floor_number = '4层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 4 AND floor_number = '1层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 4 AND floor_number = '2层';
UPDATE park_floor SET property_count = 3 WHERE building_id = 4 AND floor_number = '3层';
