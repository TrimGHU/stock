package com.hugui.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

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

@SpringBootApplication
@EnableScheduling
public class StockApplication {

	@Autowired
	public static void main(String[] args) {
		SpringApplication.run(StockApplication.class, args);
	}

}
