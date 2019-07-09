package com.hugui.stock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Copyright © 2019 Obexx. All rights reserved.
 * 
 * @Title: StockApplication.java
 * @Prject: stock
 * @Package: com.hugui.stock
 * @Description: TODO
 * @author: HuGui
 * @date: 2019年7月9日 下午7:29:26
 * @version: V1.0
 */

public class StockApplication {

	@Autowired
	public static void main(String[] args) {
		try (FileInputStream fis = new FileInputStream(new File("./data/stockcode.txt"))) {

			InputStreamReader reader = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null) {
				String[] stockInfos = line.split("\\)");
				if(stockInfos.length > 0) {
					for(String stockInfo : stockInfos) {
						String[] stock = stockInfo.split("\\(");
						System.out.println(stock[0]);
						System.out.println(stock[1]);
					}
				}
				
				//浦发银行(600000)邯郸钢铁(600001)
	        }
		} catch (IOException e) {
		}
	}

}
