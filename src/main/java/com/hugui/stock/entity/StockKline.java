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
@TableName("stock_kline")
public class StockKline extends Model<StockKline> {

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
	 * 5日均线
	 */
	private BigDecimal k5;
	/**
	 * 10日均线
	 */
	private BigDecimal k10;
	/**
	 * 20日均线
	 */
	private BigDecimal k20;
	/**
	 * 30日均线
	 */
	private BigDecimal k30;
	/**
	 * 交易日期
	 */
	@TableField("trading_date")
	private Date tradingDate;

	@Override
	protected Serializable pkVal() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StockKline{" + "id=" + id + ", k5=" + k5 + ", k10=" + k10 + ", k20=" + k20 + ", k30=" + k30
				+ ", tradingDate=" + tradingDate + "}";
	}
}
