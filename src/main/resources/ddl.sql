DROP TABLE IF EXISTS stock_dict;
CREATE TABLE stock_dict( 
	id BIGINT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID', 
	code VARCHAR(10) NOT NULL COMMENT '股票代码', 
	name VARCHAR(32) NOT NULL COMMENT '股票名称', 
	base VARCHAR(10) NOT NULL DEFAULT 'sz' COMMENT '股票所在证券所',
	PRIMARY KEY (id) 
) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_bin; 


DROP TABLE IF EXISTS stock_market;
CREATE TABLE stock_market( 
	id BIGINT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID', 
	stock_code VARCHAR(10) NOT NULL COMMENT '股票代码', 
	trading_date DATE NOT NULL COMMENT '交易日期', 
	trading_volume BIGINT(11) NOT NULL COMMENT '交易量', 
	open DECIMAL(19,4) NOT NULL COMMENT '开盘价', 
	high DECIMAL(19,4) NOT NULL COMMENT '最高价', 
	low DECIMAL(19,4) NOT NULL COMMENT '最低价', 
	close DECIMAL(19,4) NOT NULL COMMENT '收盘价', 
	PRIMARY KEY (id) 
) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_bin; 


DROP TABLE IF EXISTS stock_kline;
CREATE TABLE stock_kline( 
	id BIGINT(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID', 
	stock_code VARCHAR(10) NOT NULL COMMENT '股票代码', 
	k5 DECIMAL(19,4) NOT NULL COMMENT '5日均线', 
	k10 DECIMAL(19,4) NOT NULL COMMENT '10日均线', 
	k20 DECIMAL(19,4) NOT NULL COMMENT '20日均线', 
	k30 DECIMAL(19,4) NOT NUL COMMENT '30日均线', 
	trading_date DATE NOT NULL COMMENT '交易日期', 
	PRIMARY KEY (id) 
) ENGINE=INNODB CHARSET=utf8 COLLATE=utf8_bin; 