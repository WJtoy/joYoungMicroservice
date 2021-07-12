package com.kkl.kklplus.b2b.joyoung.http.config;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "toone")
public class B2BTooneProperties {

    @Getter
    private final OkHttpProperties okhttp = new OkHttpProperties();

    @Getter
    @Setter
    private String appKey;
    @Getter
    @Setter
    private String appSecret;

    @Getter
    @Setter
    private String[] methods;

    public static class OkHttpProperties {
        /**
         * 设置连接超时
         */
        @Getter
        @Setter
        private Integer connectTimeout = 10;

        /**
         * 设置读超时
         */
        @Getter
        @Setter
        private Integer writeTimeout = 10;

        /**
         * 设置写超时
         */
        @Getter
        @Setter
        private Integer readTimeout = 10;

        /**
         * 是否自动重连
         */
        @Getter
        @Setter
        private Boolean retryOnConnectionFailure = true;

        /**
         * 设置ping检测网络连通性的间隔
         */
        @Getter
        @Setter
        private Integer pingInterval = 0;
    }

    /**
     * 数据源配置
     */
    @Getter
    private final DataSourceConfig dataSourceConfig = new DataSourceConfig();

    public static class DataSourceConfig {
        @Getter
        @Setter
        private String requestMainUrl;

        @Getter
        @Setter
        private String targetNamespace;

        @Getter
        @Setter
        private String operation;

        @Getter
        @Setter
        private String soapAction;

        @Getter
        @Setter
        private String userPwd;

        @Getter
        @Setter
        private String userName;

        @Getter
        @Setter
        private String serviceVersion;

        @Getter
        @Setter
        private String appKey;

        @Getter
        @Setter
        private String appSecret;

        @Getter
        @Setter
        private String companyName;

        @Getter
        @Setter
        private Boolean scheduleEnabled;

        @Getter
        @Setter
        private Boolean orderMqEnabled;
    }
}
