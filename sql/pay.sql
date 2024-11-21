-- trade_config definition

CREATE TABLE `trade_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'appId',
  `pay_type` varchar(3) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'payType',
  `u_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户ID',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'code  唯一',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0 禁用 1 启用  默认0',
  `private_key` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '私钥',
  `public_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '公钥',
  `notify_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '异步回调地址',
  `return_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '同步回调地址',
  `key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信v3 key',
  `v2Key` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信v2 key',
  `down_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '下载账单存放地址',
  `mini_app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '小程序AppId',
  `app_auth_token` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付宝使用，解决自调用问题',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `public_key_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '微信支付公钥',
  PRIMARY KEY (`id`),
  UNIQUE KEY `pay_config_UN` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='支付配置表';

-- trade_mappings definition

CREATE TABLE `trade_mappings` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `field_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段名',
  `field_value` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '字段值',
  `code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'code',
  `pay_type` varchar(3) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式',
  `pay_channel` varchar(3) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付渠道',
  `priorities` int NOT NULL DEFAULT '100' COMMENT '优先级',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='支付映射表';

-- trade_cert definition

CREATE TABLE `trade_cert` (
  `id` int NOT NULL AUTO_INCREMENT,
  `config_code` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'pay_config_code',
  `cert_file_content` varchar(5000) COLLATE utf8mb4_general_ci NOT NULL COMMENT '证书文件内容',
  `cert_name` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '证书名',
  `remark` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `pay_cert_config_code_IDX` (`config_code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='支付证书表';

-- trade_flow definition

CREATE TABLE `trade_flow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `doc_id` int DEFAULT NULL COMMENT '实体id',
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '应用id 取自pay_config',
  `u_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号 取自pay_config',
  `doc_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '单据类型  1: 订单',
  `doc_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '我方交易号',
  `trade_no` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '三方交易号',
  `income_expenses` tinyint(1) NOT NULL COMMENT '收/支  1:收入  2:支出',
  `actual_amount` decimal(12,2) NOT NULL COMMENT '实际交易金额 商户收入/支出的金额',
  `pay_type` varchar(3) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式  取自payType',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品描述',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `trade_time` datetime NOT NULL COMMENT '交易时间',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '商户退款标识 标识一次退款请求，同一笔交易多次退款需要保证唯一',
  `out_refund_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '第三方退款单号 支付方式为微信时 微信退款异步返回的单号',
  `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '交易备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `unique_index` (`doc_no`,`refund_no`) USING BTREE,
  KEY `index_doc_no` (`doc_no`) USING BTREE,
  KEY `index_trade_time` (`trade_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='支付流水表';

-- trade_bill definition

CREATE TABLE `trade_bill` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `doc_id` int NOT NULL COMMENT '实体Id',
  `app_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'appId  取自pay_config',
  `u_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号 取自pay_config',
  `doc_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '单据号  商户生成',
  `trade_no` varchar(28) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '第三方流水号',
  `doc_type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '单据类型',
  `income_expenses` tinyint(1) NOT NULL COMMENT '收/支   1:收入  2:支出',
  `actual_amount` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT '实际交易金额 商户收入/支出的金额',
  `pay_type` varchar(3) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式 取自PayType',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品描述',
  `create_time` datetime DEFAULT NULL COMMENT '交易创建时间',
  `trade_time` datetime NOT NULL COMMENT '交易结束时间',
  `refund_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '商户退款标识 标识一次退款请求，同一笔交易多次退款需要保证唯一',
  `out_refund_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '第三方退款单号 支付方式为微信时 微信退款异步返回的单号',
  `remark` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `payment_bill_doc_no_IDX` (`doc_no`,`refund_no`) USING BTREE,
  KEY `doc_no` (`doc_no`) USING BTREE,
  KEY `time` (`trade_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='第三方账单表';
