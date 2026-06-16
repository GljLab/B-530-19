SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
-- =============================================
-- 业主测试数据
-- =============================================

-- 个人业主测试数据（8个）
INSERT INTO park_owner (owner_code, owner_type, name, gender, id_card, birth_date, age, nation, marital_status,
    phone, backup_phone, email, wechat, household_address, current_address, work_unit, occupation,
    family_count, emergency_contact, emergency_relation, emergency_phone,
    occupancy_status, auth_status, owner_tags, remark) VALUES
('YZ001', 1, '张伟', 1, '110101198501151234', '1985-01-15', 41, '汉族', 2,
    '13800000001', '13900000001', 'zhangwei@example.com', 'zhangwei_wx', '北京市海淀区中关村大街1号', '阳光花园1号楼1单元101',
    '北京科技有限公司', '高级工程师', 3, '李娜', '配偶', '13800000002',
    1, 2, 'VIP业主,友好邻居', '优质业主，积极配合物业工作'),
('YZ002', 1, '李娜', 2, '110101198703202345', '1987-03-20', 39, '汉族', 2,
    '13800000002', NULL, 'lina@example.com', 'lina_wx', '北京市海淀区中关村大街1号', '阳光花园1号楼1单元101',
    '北京教育机构', '教师', 3, '张伟', '配偶', '13800000001',
    1, 2, '友好邻居', '张伟的配偶'),
('YZ003', 1, '王芳', 2, '110102199005103456', '1990-05-10', 36, '汉族', 1,
    '13800000003', '13900000003', 'wangfang@example.com', 'wangfang_wx', '北京市西城区金融街10号', '阳光花园2号楼2单元502',
    '北京金融公司', '金融分析师', 1, '王建国', '父亲', '13800000010',
    1, 1, NULL, '年轻业主，单身'),
('YZ004', 1, '刘强', 1, '110105197808124567', '1978-08-12', 47, '汉族', 2,
    '13800000004', '13900000004', 'liuqiang@example.com', 'liuqiang_wx', '北京市朝阳区建国路88号', '阳光花园3号楼3单元1501',
    '北京贸易公司', '总经理', 4, '赵敏', '配偶', '13800000005',
    1, 2, 'VIP业主', '多套房产持有者'),
('YZ005', 1, '赵敏', 2, '110105198009155678', '1980-09-15', 45, '回族', 2,
    '13800000005', NULL, 'zhaomin@example.com', 'zhaomin_wx', '北京市朝阳区建国路88号', '阳光花园3号楼3单元1501',
    '北京医疗集团', '医生', 4, '刘强', '配偶', '13800000004',
    1, 2, '友好邻居', '回族，注意民族习俗'),
('YZ006', 1, '陈刚', 1, '110106199211206789', '1992-11-20', 33, '汉族', 1,
    '13800000006', NULL, 'chengang@example.com', 'chengang_wx', '北京市丰台区南三环西路1号', '阳光花园1号楼2单元803',
    '北京互联网公司', '产品经理', 1, '陈父', '父亲', '13800000011',
    2, 1, '投诉较多', '对物业服务要求较高，多次投诉'),
('YZ007', 1, '杨丽', 2, '110108198802287890', '1988-02-28', 37, '满族', 2,
    '13800000007', '13900000007', 'yangli@example.com', 'yangli_wx', '北京市海淀区学院路15号', '阳光花园2号楼1单元302',
    '北京高校', '行政人员', 3, '周明', '配偶', '13800000008',
    3, 2, '长期欠费', '物业费已拖欠3个月'),
('YZ008', 1, '周明', 1, '110108198604108901', '1986-04-10', 39, '汉族', 2,
    '13800000008', NULL, 'zhouming@example.com', 'zhouming_wx', '北京市海淀区学院路15号', '阳光花园2号楼1单元302',
    '北京设计公司', '设计师', 3, '杨丽', '配偶', '13800000007',
    3, 2, '长期欠费', '杨丽的配偶，房屋出租中');

-- 企业业主测试数据（2个）
INSERT INTO park_owner (owner_code, owner_type, name, phone, email, occupancy_status, auth_status, owner_tags, remark,
    enterprise_credit_code, enterprise_type, legal_person_name, legal_person_id_card,
    contact_person, contact_position, contact_phone, enterprise_address, registered_capital, business_license_url) VALUES
('YZ009', 2, '北京阳光科技发展有限公司', '13800000009', 'sunnytech@example.com', 1, 2, 'VIP业主', '企业客户，购买了整层办公楼',
    '91110108MA01234567', '有限责任公司', '孙磊', '110101197505051234',
    '孙磊', '总经理', '13800000009', '北京市海淀区中关村软件园', 5000.00, 'https://example.com/license1.jpg'),
('YZ010', 2, '北京创新投资有限公司', '13800000010', 'innovest@example.com', 3, 3, '投资业主', '投资购房用于出租',
    '91110105MA07654321', '投资公司', '钱进', '11010219701010234',
    '钱进', '董事长', '13800000010', '北京市朝阳区CBD', 10000.00, 'https://example.com/license2.jpg');

-- =============================================
-- 业主-房产关联测试数据
-- =============================================

-- 张伟关联1号楼1单元101（完全产权）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(1, 'YZ001', 1, '1-101', 1, 100.00, '京房权证海字第2020001号', 1, '2020-06-01', 500.00, '2020-12-01', 1, 1);

-- 李娜关联1号楼1单元101（共有产权，50%）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(2, 'YZ002', 1, '1-101', 2, 50.00, '京房权证海字第2020001号', 1, '2020-06-01', 250.00, '2020-12-01', 1, 1);

-- 王芳关联2号楼2单元502（完全产权）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(3, 'YZ003', 10, '2-502', 1, 100.00, '京房权证海字第2021001号', 1, '2021-03-15', 480.00, '2021-06-01', 1, 1);

-- 刘强关联3号楼3单元1501（完全产权）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(4, 'YZ004', 20, '3-1501', 1, 100.00, '京房权证朝字第2019001号', 1, '2019-08-10', 650.00, '2020-01-15', 1, 1);

-- 刘强关联3号楼3单元1502（投资，第二套）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(4, 'YZ004', 21, '3-1502', 1, 100.00, '京房权证朝字第2020002号', 1, '2020-05-20', 620.00, NULL, 0, 1);

-- 赵敏关联3号楼3单元1501（共有产权，50%）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(5, 'YZ005', 20, '3-1501', 2, 50.00, '京房权证朝字第2019001号', 1, '2019-08-10', 325.00, '2020-01-15', 1, 1);

-- 陈刚关联1号楼2单元803（使用权）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(6, 'YZ006', 5, '1-803', 3, 100.00, NULL, 1, '2022-01-10', 450.00, NULL, 1, 1);

-- 杨丽关联2号楼1单元302（完全产权，出租中）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(7, 'YZ007', 8, '2-302', 1, 100.00, '京房权证海字第2020005号', 1, '2020-09-01', 420.00, '2021-02-01', 0, 1);

-- 周明关联2号楼1单元302（共有产权，50%，出租中）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(8, 'YZ008', 8, '2-302', 2, 50.00, '京房权证海字第2020005号', 1, '2020-09-01', 210.00, '2021-02-01', 0, 1);

-- 北京阳光科技发展有限公司关联商业中心1层多套（整层购买）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(9, 'YZ009', 50, '4-101', 1, 100.00, '京房权证海字第2022001号', 1, '2022-03-01', 1200.00, '2022-06-01', 1, 1),
(9, 'YZ009', 51, '4-102', 1, 100.00, '京房权证海字第2022002号', 1, '2022-03-01', 1100.00, '2022-06-01', 1, 1),
(9, 'YZ009', 52, '4-103', 1, 100.00, '京房权证海字第2022003号', 1, '2022-03-01', 1000.00, '2022-06-01', 1, 1);

-- 北京创新投资有限公司关联多套房产用于出租（投资）
INSERT INTO park_owner_property (owner_id, owner_code, property_id, property_code, property_right_type, property_right_ratio,
    property_cert_no, acquire_type, acquire_date, purchase_price, move_in_date, is_self_occupy, relation_status) VALUES
(10, 'YZ010', 15, '2-1001', 1, 100.00, '京房权证朝字第2021010号', 1, '2021-08-15', 520.00, '2021-11-01', 0, 1),
(10, 'YZ010', 16, '2-1002', 1, 100.00, '京房权证朝字第2021011号', 1, '2021-08-15', 510.00, '2021-11-01', 0, 1),
(10, 'YZ010', 25, '3-2001', 1, 100.00, '京房权证朝字第2021012号', 1, '2021-09-20', 680.00, '2021-12-01', 0, 1);

-- =============================================
-- 认证审核日志测试数据
-- =============================================

-- YZ001 张伟 - 认证通过
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(1, 'YZ001', '张伟', 2, '资料齐全，身份信息核实无误，房产关联正确', 3, '物业管理员', '2024-01-15 10:30:00');

-- YZ002 李娜 - 认证通过
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(2, 'YZ002', '李娜', 2, '身份信息核实无误，共有产权证明齐全', 3, '物业管理员', '2024-01-15 11:00:00');

-- YZ003 王芳 - 未认证

-- YZ004 刘强 - 认证通过
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(4, 'YZ004', '刘强', 2, '多套房产信息核实无误，资料齐全', 3, '物业管理员', '2024-02-20 09:15:00');

-- YZ005 赵敏 - 认证通过
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(5, 'YZ005', '赵敏', 2, '身份信息核实无误，注意回民饮食禁忌', 3, '物业管理员', '2024-02-20 09:45:00');

-- YZ006 陈刚 - 未认证

-- YZ007 杨丽 - 认证通过（欠费不影响认证）
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(7, 'YZ007', '杨丽', 2, '身份信息核实无误，房屋目前出租中', 3, '物业管理员', '2024-03-10 14:20:00');

-- YZ008 周明 - 认证通过
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(8, 'YZ008', '周明', 2, '身份信息核实无误，房屋目前出租中', 3, '物业管理员', '2024-03-10 14:50:00');

-- YZ009 北京阳光科技发展有限公司 - 认证通过
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, operator_id, operator_name, create_time) VALUES
(9, 'YZ009', '北京阳光科技发展有限公司', 2, '企业营业执照、法人身份证明齐全，企业资质核实无误', 3, '物业管理员', '2024-04-05 10:00:00');

-- YZ010 北京创新投资有限公司 - 认证失败
INSERT INTO park_owner_audit_log (owner_id, owner_code, owner_name, audit_result, audit_opinion, reject_reason, operator_id, operator_name, create_time) VALUES
(10, 'YZ010', '北京创新投资有限公司', 3, '资料审核', '营业执照扫描件不清晰，法定代表人身份证明缺失，请重新提交', 3, '物业管理员', '2024-05-12 16:30:00');
