package com.hugui.stock.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.hugui.stock.entity.StockDict;
import com.hugui.stock.entity.StockKline;
import com.hugui.stock.entity.StockMarket;
import com.hugui.stock.service.IStockDictService;
import com.hugui.stock.service.IStockKlineService;
import com.hugui.stock.service.IStockMarketService;

/**
 * Copyright © 2019 Obexx. All rights reserved.
 *
 * @Title: StockAnalyseController.java
 * @Prject: stock
 * @Package: com.hugui.stock.controller
 * @Description: TODO
 * @author: HuGui
 * @date: 2019年7月11日 下午7:30:06
 * @version: V1.0
 */

@RestController
@RequestMapping("/analyse")
public class StockAnalyseController {

    @Autowired
    private IStockDictService stockDictService;

    @Autowired
    private IStockKlineService stockKlineService;

    @Autowired
    private IStockMarketService stockMarketService;

    // @Scheduled(cron = "0 30 19 * * *")
    // @PostConstruct
    @GetMapping("/perDay")
    public void analyseStockMarketPerDay() throws IOException {
        List<StockDict> stockDictList = stockDictService.selectList(null);
        if (stockDictList.isEmpty()) {
            return;
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = df.format(new Date());

        List<String> good5To10StockList = new ArrayList<>();
        StringBuilder good5To10StockCodeList = new StringBuilder();
        List<String> good5To20StockList = new ArrayList<>();
        StringBuilder good5To20StockCodeList = new StringBuilder();
        List<String> good10To20StockList = new ArrayList<>();
        StringBuilder good10To20StockCodeList = new StringBuilder();
        for (StockDict stock : stockDictList) {
            List<StockKline> stockKlineList = stockKlineService
                    .selectList(new EntityWrapper<StockKline>(StockKline.builder().stockCode(stock.getCode()).build())
                            .orderBy("trading_date desc").last("limit 2"));

            // 停牌
            if (stockKlineList.size() < 2 || !df.format(stockKlineList.get(0).getTradingDate()).equals(currentDate)) {
                continue;
            }

            StockKline todayKline = stockKlineList.get(0);
            StockKline yesterdayKline = stockKlineList.get(1);

            // 5k上穿10k綫
            if (yesterdayKline.getK5().compareTo(yesterdayKline.getK10()) <= 0
                    && todayKline.getK5().compareTo(todayKline.getK10()) >= 0
                    && todayKline.getK5().compareTo(yesterdayKline.getK5()) >= 0) {
                good5To10StockList.add(stock.getCode() + stock.getName());
                good5To10StockCodeList.append(stock.getCode() + ",");
            }

            // 5k上穿20k綫
            if (yesterdayKline.getK5().compareTo(yesterdayKline.getK20()) <= 0
                    && todayKline.getK5().compareTo(todayKline.getK20()) >= 0
                    && todayKline.getK5().compareTo(yesterdayKline.getK5()) >= 0) {
                good5To20StockList.add(stock.getCode() + stock.getName());
                good5To20StockCodeList.append(stock.getCode() + ",");
            }

            // 10k上穿20k綫
            if (yesterdayKline.getK10().compareTo(yesterdayKline.getK20()) <= 0
                    && todayKline.getK10().compareTo(todayKline.getK20()) >= 0
                    && todayKline.getK5().compareTo(yesterdayKline.getK10()) >= 0
                    && todayKline.getK5().compareTo(yesterdayKline.getK5()) >= 0) {
                good10To20StockList.add(stock.getCode() + stock.getName());
                good10To20StockCodeList.append(stock.getCode() + ",");
            }
        }

        try (FileOutputStream fos5to10 = new FileOutputStream(
                new File("C:/workstation/sts-bundle/sts-3.9.6.RELEASE/workspace/stock/result/5to10-" + currentDate + ".txt"))) {
            fos5to10.write(good5To10StockCodeList.toString().getBytes());
            fos5to10.flush();
        }
        try (FileOutputStream fos5to20 = new FileOutputStream(new File(
                "C:/workstation/sts-bundle/sts-3.9.6.RELEASE/workspace/stock/result/5to20-" + currentDate + ".txt"))) {
            fos5to20.write(good5To20StockCodeList.toString().getBytes());
            fos5to20.flush();
        }
        try (FileOutputStream fos10to20 = new FileOutputStream(new File(
                "C:/workstation/sts-bundle/sts-3.9.6.RELEASE/workspace/stock/result/10to20-" + currentDate + ".txt"))) {
            fos10to20.write(good10To20StockCodeList.toString().getBytes());
            fos10to20.flush();
        }

        System.out.println("5 ---  10 " + good5To10StockList.size() + good5To10StockList);
        System.out.println("5 ---  20 " + good5To20StockList.size() + good5To20StockList);
        System.out.println("10 ---  20 " + good10To20StockList.size() + good10To20StockList);
    }

    @GetMapping("/perSecondDay")
    public void analyseStockMarketSecondDay() throws IOException {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = df.format(new Date());
        String yesterDate = df.format(new Date(new Date().toInstant().minusSeconds(24 * 60 * 60).toEpochMilli()));

        File file5to10 = new File("C:/workstation/sts-bundle/sts-3.9.6.RELEASE/workspace/stock/result/result/5to10-" + yesterDate + ".txt");
        if (!file5to10.exists()) {
            yesterDate = df.format(new Date(new Date().toInstant().minusSeconds(3 * 24 * 60 * 60).toEpochMilli()));
            file5to10 = new File("C:/workstation/sts-bundle/sts-3.9.6.RELEASE/workspace/stock/result/result/5to10-" + yesterDate + ".txt");

            if (!file5to10.exists()) {
                return;
            }
        }

        List<String> codes = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file5to10)) {
            InputStreamReader reader = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String line;
            while ((line = br.readLine()) != null) {
                String[] stockInfos = line.split(",");
                if (stockInfos != null && stockInfos.length > 0) {
                    codes.addAll(Arrays.asList(stockInfos));
                }
            }
        }

        List<String> result = new ArrayList<>();
        List<String> special = new ArrayList<>();
        int up = 0;
        int down = 0;
        int nothing = 0;
        for (String code : codes) {
            List<StockMarket> stockMarketList = stockMarketService
                    .selectList(new EntityWrapper<StockMarket>(StockMarket.builder().stockCode(code).build())
                            .orderBy("trading_date desc").last("limit 2"));

            if (stockMarketList.size() < 2 || !df.format(stockMarketList.get(0).getTradingDate()).equals(currentDate)) {
                continue;
            }

            StockMarket todayMarket = stockMarketList.get(0);
            StockMarket yesterdayMarket = stockMarketList.get(1);

            BigDecimal per100 = (todayMarket.getClose().subtract(yesterdayMarket.getClose()))
                    .divide(yesterdayMarket.getClose(), 3, BigDecimal.ROUND_HALF_DOWN);

            if (per100.abs().compareTo(new BigDecimal("0.04")) > 0) {
                special.add("\n \t" + code + " 幅度 " + per100);
            }

            if (todayMarket.getClose().compareTo(yesterdayMarket.getClose()) > 0) {
                up++;
                result.add("\n \t" + code + " 漲 " + per100 + " 金額為 "
                        + todayMarket.getClose().subtract(yesterdayMarket.getClose()));
            } else if (todayMarket.getClose().compareTo(yesterdayMarket.getClose()) == 0) {
                nothing++;
                result.add("\n \t" + code + " 平  金額為 " + todayMarket.getClose().subtract(yesterdayMarket.getClose()));
            } else {
                down++;
                result.add("\n \t" + code + " 跌 " + per100 + " 金額為 "
                        + todayMarket.getClose().subtract(yesterdayMarket.getClose()));
            }
        }

        System.out.println("前一天的結果： " + result);
        System.out.println("前一天的結果： " + special);
        System.out.println("前一天的結果： Up: " + up + " Down: " + down + " Nothing: " + nothing);
    }

    @GetMapping("/uplus")
    public void uplus() {
        List<StockDict> stockDictList = stockDictService.selectList(null);
        if (stockDictList.isEmpty()) {
            return;
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = df.format(new Date());

        List<String> upPer40 = new ArrayList<>();
        List<String> upPer60 = new ArrayList<>();
        List<String> upPer80 = new ArrayList<>();
        List<String> upPer100 = new ArrayList<>();
        List<String> upPer100N = new ArrayList<>();
        for (StockDict stock : stockDictList) {
            List<StockMarket> stockMarketList = stockMarketService
                    .selectList(new EntityWrapper<StockMarket>(StockMarket.builder().stockCode(stock.getCode()).build())
                            .orderBy("trading_date desc").last("limit 2"));

            // 停牌
            if (stockMarketList.size() < 2 ||
                    !df.format(stockMarketList.get(0).getTradingDate()).equals(currentDate)) {
                continue;
            }

            StockMarket todayMarket = stockMarketList.get(0);
            StockMarket yesterdayMarket = stockMarketList.get(1);

            if (todayMarket.getTradingVolume() > yesterdayMarket.getTradingVolume()) {
                BigDecimal diffPercent = new BigDecimal(todayMarket.getTradingVolume())
                        .divide(
                                new BigDecimal(yesterdayMarket.getTradingVolume()), 3, BigDecimal.ROUND_HALF_DOWN);


                BigDecimal diffPrice = todayMarket.getClose()
                        .divide(yesterdayMarket.getClose(), 3, BigDecimal.ROUND_HALF_DOWN);

                if (diffPercent.compareTo(new BigDecimal("2")) >= 0) {
                    upPer100.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());

                    if(diffPrice.compareTo(new BigDecimal("1.02")) <= 0 && diffPrice.compareTo(new BigDecimal("1")) >= 0){
                        upPer100N.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                    }
                } else if (diffPercent.compareTo(new BigDecimal("1.8")) >= 0) {
                    upPer80.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                } else if (diffPercent.compareTo(new BigDecimal("1.6")) >= 0) {
                    upPer60.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                } else if (diffPercent.compareTo(new BigDecimal("1.4")) >= 0) {
                    upPer40.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                }
            }

        }

        System.out.println("增量40%的結果： " + upPer40.size() + "--" +upPer40);
        System.out.println("增量60%的結果： " + upPer60.size() + "--" +upPer60);
        System.out.println("增量80%的結果： " + upPer80.size() + "--" +upPer80);
        System.out.println("增量100%的結果： " + upPer100.size() + "--" +upPer100);
        System.out.println("增量N-100%的結果： " + upPer100N.size() + "--" +upPer100N);
    }

    @GetMapping("/dfall")
    public void decreaseFall() {
        List<StockDict> stockDictList = stockDictService.selectList(null);
        if (stockDictList.isEmpty()) {
            return;
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = df.format(new Date());

        List<String> diffPer90 = new ArrayList<>();
        List<String> diffPer70 = new ArrayList<>();
        List<String> diffPer50 = new ArrayList<>();
        List<String> diffPer30 = new ArrayList<>();
        List<String> diffPer10 = new ArrayList<>();
        for (StockDict stock : stockDictList) {
            List<StockMarket> stockMarketList = stockMarketService
                    .selectList(new EntityWrapper<StockMarket>(StockMarket.builder().stockCode(stock.getCode()).build())
                            .orderBy("trading_date desc").last("limit 3"));

            // 停牌
            if (stockMarketList.size() < 3 ||
                    !df.format(stockMarketList.get(0).getTradingDate()).equals(currentDate)) {
                continue;
            }

            StockMarket todayMarket = stockMarketList.get(0);
            StockMarket yesterdayMarket = stockMarketList.get(1);
            StockMarket beforeYesterdayMarket = stockMarketList.get(2);

            if (todayMarket.getClose().compareTo(yesterdayMarket.getClose()) <= 0
                    && beforeYesterdayMarket.getClose().compareTo(yesterdayMarket.getClose()) <= 0
                    && todayMarket.getTradingVolume() <= yesterdayMarket.getTradingVolume()) {
                BigDecimal diffPercent = new BigDecimal(todayMarket.getTradingVolume())
                        .divide(
                                new BigDecimal(yesterdayMarket.getTradingVolume()), 3, BigDecimal.ROUND_HALF_DOWN);

                if (diffPercent.compareTo(new BigDecimal("0.9")) >= 0) {
                    diffPer90.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                } else if (diffPercent.compareTo(new BigDecimal("0.7")) >= 0) {
                    diffPer70.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                } else if (diffPercent.compareTo(new BigDecimal("0.5")) >= 0) {
                    diffPer50.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                } else if (diffPercent.compareTo(new BigDecimal("0.3")) >= 0) {
                    diffPer30.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                } else {
                    diffPer10.add(diffPercent + "-" + stock.getCode() + "-" + stock.getName());
                }
            }
        }

        System.out.println("縮量跌10%的結果： " + diffPer90);
        System.out.println("縮量跌30%的結果： " + diffPer70);
        System.out.println("縮量跌50%的結果： " + diffPer50);
        System.out.println("縮量跌70%的結果： " + diffPer30);
        System.out.println("縮量跌90%的結果： " + diffPer10);
    }
}
