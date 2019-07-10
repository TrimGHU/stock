package com.hugui.stock.configuration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.plugins.PerformanceInterceptor;

/**
 * 
 * Copyright © 2018 Obexx. All rights reserved.
 * 
 * @Title: MybatisPlusConfig.java
 * @Prject: obexx-ai-box
 * @Package: com.obexx.aibox.configuration
 * @Description: MybatisPlus配置类
 * @author: HuGui
 * @date: 2018年12月24日 下午5:09:06
 * @version: V1.0
 */

@Configuration
@MapperScan({"com.hugui.stock.mapper*",})
public class MybatisPlusConfig {

    @Bean
    public PerformanceInterceptor performanceInterceptor() {
        return new PerformanceInterceptor();
    }
    
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}
