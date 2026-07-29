package com.sky.config;

import com.sky.utils.AliOssUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里配置类
 */
@Configuration
public class AliOssConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "aliyun.oss")
    public AliOssUtil aliOssUtil() {
        return new AliOssUtil(
                null,
                null,
                null,
                null
        );
    }
}
