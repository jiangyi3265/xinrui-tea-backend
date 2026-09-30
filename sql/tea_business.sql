-- Additive migration. Run only after the RuoYi schema exists.
-- Never import ry_20250522.sql into an existing database (it drops tables).
CREATE TABLE IF NOT EXISTS tea_business_state (
  id BIGINT PRIMARY KEY,
  state_json LONGTEXT NOT NULL,
  revision BIGINT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CHECK (JSON_VALID(state_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO tea_business_state(id,state_json) VALUES
(1,'{"users":[],"sessions":{},"nextId":10000,"catalog":[],"auctions":[],"auctionBids":[],"adminNotices":[],"content":{}}');
CREATE TABLE IF NOT EXISTS tea_business_audit (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  revision BIGINT NOT NULL,
  actor VARCHAR(64) NOT NULL,
  route VARCHAR(200) NOT NULL,
  method VARCHAR(10) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY tea_audit_revision(revision)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS tea_voucher_claim (
  sha256 CHAR(64) PRIMARY KEY,
  business_owner VARCHAR(160) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,menu_type,perms,icon)
VALUES (8700,'茶叶业务',0,1,'tea',NULL,'M','','shopping');



INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8701,'经营概览',8700,1,'dashboard','tea/dashboard','{"resource":"dashboard"}','TeaDashboard','C','tea:dashboard:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8800,'经营概览add',8701,0,'','F','tea:dashboard:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8801,'经营概览edit',8701,1,'','F','tea:dashboard:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8802,'经营概览remove',8701,2,'','F','tea:dashboard:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8702,'商品管理',8700,2,'products','tea/products','{"resource":"products"}','TeaProducts','C','tea:products:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8803,'商品管理add',8702,0,'','F','tea:products:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8804,'商品管理edit',8702,1,'','F','tea:products:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8805,'商品管理remove',8702,2,'','F','tea:products:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8703,'商城订单',8700,3,'orders','tea/orders','{"resource":"orders"}','TeaOrders','C','tea:orders:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8806,'商城订单add',8703,0,'','F','tea:orders:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8807,'商城订单edit',8703,1,'','F','tea:orders:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8808,'商城订单remove',8703,2,'','F','tea:orders:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8704,'拍卖管理',8700,4,'auctions','tea/auctions','{"resource":"auctions"}','TeaAuctions','C','tea:auctions:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8809,'拍卖管理add',8704,0,'','F','tea:auctions:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8810,'拍卖管理edit',8704,1,'','F','tea:auctions:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8811,'拍卖管理remove',8704,2,'','F','tea:auctions:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8705,'会员管理',8700,5,'members','tea/members','{"resource":"members"}','TeaMembers','C','tea:members:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8812,'会员管理add',8705,0,'','F','tea:members:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8813,'会员管理edit',8705,1,'','F','tea:members:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8814,'会员管理remove',8705,2,'','F','tea:members:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8706,'公告管理',8700,6,'notices','tea/notices','{"resource":"notices"}','TeaNotices','C','tea:notices:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8815,'公告管理add',8706,0,'','F','tea:notices:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8816,'公告管理edit',8706,1,'','F','tea:notices:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8817,'公告管理remove',8706,2,'','F','tea:notices:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8707,'拍卖仓库',8700,7,'warehouse','tea/records','{"resource":"warehouse"}','TeaWarehouse','C','tea:warehouse:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8818,'拍卖仓库add',8707,0,'','F','tea:warehouse:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8819,'拍卖仓库edit',8707,1,'','F','tea:warehouse:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8820,'拍卖仓库remove',8707,2,'','F','tea:warehouse:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8708,'付款审核',8700,8,'settlements','tea/records','{"resource":"settlements"}','TeaSettlements','C','tea:settlements:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8821,'付款审核add',8708,0,'','F','tea:settlements:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8822,'付款审核edit',8708,1,'','F','tea:settlements:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8823,'付款审核remove',8708,2,'','F','tea:settlements:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8709,'寄卖服务费',8700,9,'fees','tea/records','{"resource":"fees"}','TeaFees','C','tea:fees:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8824,'寄卖服务费add',8709,0,'','F','tea:fees:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8825,'寄卖服务费edit',8709,1,'','F','tea:fees:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8826,'寄卖服务费remove',8709,2,'','F','tea:fees:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8710,'充值审核',8700,10,'recharges','tea/records','{"resource":"recharges"}','TeaRecharges','C','tea:recharges:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8827,'充值审核add',8710,0,'','F','tea:recharges:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8828,'充值审核edit',8710,1,'','F','tea:recharges:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8829,'充值审核remove',8710,2,'','F','tea:recharges:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8711,'提现记录',8700,11,'withdrawals','tea/records','{"resource":"withdrawals"}','TeaWithdrawals','C','tea:withdrawals:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8830,'提现记录add',8711,0,'','F','tea:withdrawals:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8831,'提现记录edit',8711,1,'','F','tea:withdrawals:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8832,'提现记录remove',8711,2,'','F','tea:withdrawals:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8712,'投诉处理',8700,12,'reports','tea/records','{"resource":"reports"}','TeaReports','C','tea:reports:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8833,'投诉处理add',8712,0,'','F','tea:reports:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8834,'投诉处理edit',8712,1,'','F','tea:reports:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8835,'投诉处理remove',8712,2,'','F','tea:reports:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8713,'账户流水',8700,13,'ledger','tea/records','{"resource":"ledger"}','TeaLedger','C','tea:ledger:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8836,'账户流水add',8713,0,'','F','tea:ledger:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8837,'账户流水edit',8713,1,'','F','tea:ledger:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8838,'账户流水remove',8713,2,'','F','tea:ledger:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8714,'出价记录',8700,14,'bids','tea/records','{"resource":"bids"}','TeaBids','C','tea:bids:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8839,'出价记录add',8714,0,'','F','tea:bids:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8840,'出价记录edit',8714,1,'','F','tea:bids:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8841,'出价记录remove',8714,2,'','F','tea:bids:remove');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,menu_type,perms,icon) VALUES (8715,'商城内容',8700,15,'content','tea/records','{"resource":"content"}','TeaContent','C','tea:content:list','list');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8842,'商城内容add',8715,0,'','F','tea:content:add');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8843,'商城内容edit',8715,1,'','F','tea:content:edit');
INSERT IGNORE INTO sys_menu(menu_id,menu_name,parent_id,order_num,path,menu_type,perms) VALUES (8844,'商城内容remove',8715,2,'','F','tea:content:remove');
-- Permission templates only: deliberately do not assign existing users.
INSERT INTO sys_role(role_name,role_key,role_sort,status) SELECT '茶叶运营','tea_operator',50,'0' WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key='tea_operator');
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT r.role_id,m.menu_id FROM sys_role r CROSS JOIN sys_menu m WHERE r.role_key='tea_operator' AND (m.menu_id=8700 OR m.perms LIKE 'tea:%' AND SUBSTRING_INDEX(SUBSTRING_INDEX(m.perms,':',2),':',-1) IN ('dashboard','products','orders','auctions','members','warehouse','bids'));
INSERT INTO sys_role(role_name,role_key,role_sort,status) SELECT '茶叶财务','tea_finance',50,'0' WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key='tea_finance');
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT r.role_id,m.menu_id FROM sys_role r CROSS JOIN sys_menu m WHERE r.role_key='tea_finance' AND (m.menu_id=8700 OR m.perms LIKE 'tea:%' AND SUBSTRING_INDEX(SUBSTRING_INDEX(m.perms,':',2),':',-1) IN ('dashboard','settlements','fees','recharges','withdrawals','ledger'));
INSERT INTO sys_role(role_name,role_key,role_sort,status) SELECT '茶叶内容','tea_content',50,'0' WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key='tea_content');
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT r.role_id,m.menu_id FROM sys_role r CROSS JOIN sys_menu m WHERE r.role_key='tea_content' AND (m.menu_id=8700 OR m.perms LIKE 'tea:%' AND SUBSTRING_INDEX(SUBSTRING_INDEX(m.perms,':',2),':',-1) IN ('notices','content'));
INSERT INTO sys_role(role_name,role_key,role_sort,status) SELECT '茶叶仓管','tea_warehouse',50,'0' WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key='tea_warehouse');
INSERT IGNORE INTO sys_role_menu(role_id,menu_id) SELECT r.role_id,m.menu_id FROM sys_role r CROSS JOIN sys_menu m WHERE r.role_key='tea_warehouse' AND (m.menu_id=8700 OR m.perms LIKE 'tea:%' AND SUBSTRING_INDEX(SUBSTRING_INDEX(m.perms,':',2),':',-1) IN ('orders','warehouse','reports'));
