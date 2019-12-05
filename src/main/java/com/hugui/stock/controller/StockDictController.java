package com.hugui.stock.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.hugui.stock.entity.StockDict;
import com.hugui.stock.entity.StockKline;
import com.hugui.stock.entity.StockMarket;
import com.hugui.stock.service.IStockDictService;
import com.hugui.stock.service.IStockKlineService;
import com.hugui.stock.service.IStockMarketService;
import com.hugui.stock.util.HttpClientUtil;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author hugui123
 * @since 2019-07-10
 */
@Controller
@RequestMapping("/stockDict")
public class StockDictController {

	private Logger logger = LoggerFactory.getLogger(StockDictController.class);

	@Autowired
	private IStockDictService stockDictService;

	@Autowired
	private IStockMarketService stockMarketService;

	@Autowired
	private IStockKlineService stockKlineService;

	// @PostConstruct
	public void init() {

		int count = stockDictService.selectCount(null);
		if (count > 0) {
			return;
		}

		try (FileInputStream fis = new FileInputStream(new File("./data/stockcode.txt"))) {

			InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null) {
				String[] stockInfos = line.split("\\)");
				if (stockInfos.length > 0) {
					List<StockDict> stockList = new ArrayList<>();
					for (String stockInfo : stockInfos) {

						String[] stock = stockInfo.split("\\(");

						String code = stock[1];
						String base = null;
						if (code.startsWith("6")) {
							base = "sh";
						} else {
							base = "sz";
						}
						stockList.add(StockDict.builder().code(stock[1]).name(stock[0]).base(base).build());

					}

					if (!stockList.isEmpty()) {
						stockDictService.insertBatch(stockList);
					}
				}
			}
		} catch (IOException e) {
		}
	}

	// @Scheduled(cron = "0 02 16 * * *")
	// @PostConstruct
	@GetMapping("/collect/{stockCode}")
	public void collectStockMarketPerDay(@PathVariable("stockCode") String stockCode)
			throws ParseException, InterruptedException {
		logger.info("begin collect stock market data per day ==> ");

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = df.format(new Date());

		Long collectStockId = 0L;
		if (!stockCode.equals("0")) {
			StockDict stock = stockDictService
					.selectOne(new EntityWrapper<StockDict>(StockDict.builder().code(stockCode).build()));
			collectStockId = stock.getId();
		}
		List<StockDict> stockDictList = stockDictService
				.selectList(new EntityWrapper<StockDict>().addFilter(" id > {0}", collectStockId));
		if (stockDictList.isEmpty()) {
			return;
		}

		int counter = 0;

		for (StockDict stock : stockDictList) {
			List<StockMarket> stockMarketList = new ArrayList<>();

			if (counter > 10 && counter % (8 * 60) == 0) {
				Thread.sleep(100_000L);
			} else {
				Thread.sleep(1000L);
			}

			counter++;

			String jsonResult = HttpClientUtil.get(
					"http://money.finance.sina.com.cn/quotes_service/api/json_v2.php/CN_MarketData.getKLineData?symbol="
							+ stock.getBase() + stock.getCode() + "&scale=240&ma=no&datalen=30");

			if (jsonResult == null || jsonResult.equals("null")) {
				continue;
			}

			JSONArray jsonArr = (JSONArray) JSON.parse(jsonResult);
			BigDecimal k5 = new BigDecimal(0);
			BigDecimal k10 = new BigDecimal(0);
			BigDecimal k20 = new BigDecimal(0);
			BigDecimal k30 = new BigDecimal(0);

			boolean isRecord = false;
			for (int i = 0; i < jsonArr.size(); i++) {
				JSONObject jsonObject = jsonArr.getJSONObject(i);

				Double price = jsonObject.getDouble("close");
				k30 = k30.add(BigDecimal.valueOf(price));

				if (i > 9) {
					k20 = k20.add(BigDecimal.valueOf(price));
				}
				if (i > 19) {
					k10 = k10.add(BigDecimal.valueOf(price));
				}
				if (i > 24) {
					k5 = k5.add(BigDecimal.valueOf(price));
				}

				// 保存最后一最新的當天的數據
				// 如果是休市則不需要記錄，進程單跑而已不記錄任何數據
				String date = jsonObject.getString("day");
				if (i == 29 && currentDate.equals(date)) {
					isRecord = true;
					stockMarketList.add(StockMarket.builder().stockCode(stock.getCode()).tradingDate(df.parse(date))
							.open(BigDecimal.valueOf(jsonObject.getDouble("open")))
							.high(BigDecimal.valueOf(jsonObject.getDouble("high")))
							.low(BigDecimal.valueOf(jsonObject.getDouble("low"))).close(BigDecimal.valueOf(price))
							.tradingVolume(jsonObject.getLong("volume")).build());
				}
			}

			// 如果是休市則不需要記錄，進程單跑而已不記錄任何數據
			if (isRecord) {
				k30 = k30.divide(new BigDecimal(30), 3, BigDecimal.ROUND_HALF_DOWN);
				k20 = k20.divide(new BigDecimal(20), 3, BigDecimal.ROUND_HALF_DOWN);
				k10 = k10.divide(new BigDecimal(10), 3, BigDecimal.ROUND_HALF_DOWN);
				k5 = k5.divide(new BigDecimal(5), 3, BigDecimal.ROUND_HALF_DOWN);

				stockKlineService.insert(StockKline.builder().stockCode(stock.getCode()).k5(k5).k10(k10).k20(k20)
						.k30(k30).tradingDate(new Date()).build());
			}

			if (!stockMarketList.isEmpty()) {
				stockMarketService.insertBatch(stockMarketList);
			}
		}
	}
}
