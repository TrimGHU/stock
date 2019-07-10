package com.hugui.stock.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 
 * </p>
 *
 * @author hugui123
 * @since 2019-07-10
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stock_market")
public class StockMarket extends Model<StockMarket> {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	/**
	 * 交易代码
	 */
	@TableField("stock_code")
	private String stockCode;
	/**
	 * 交易日期
	 */
	@TableField("trading_date")
	private Date tradingDate;
	/**
	 * 交易量
	 */
	@TableField("trading_volume")
	private Long tradingVolume;
	/**
	 * 开盘价
	 */
	@TableField("OPEN")
	private BigDecimal open;
	/**
	 * 最高价
	 */
	private BigDecimal high;
	/**
	 * 最低价
	 */
	private BigDecimal low;
	/**
	 * 收盘价
	 */
	@TableField("CLOSE")
	private BigDecimal close;

	@Override
	protected Serializable pkVal() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StockMarket{" + "id=" + id + ", tradingDate=" + tradingDate + ", tradingVolume=" + tradingVolume
				+ ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close + "}";
	}
}
