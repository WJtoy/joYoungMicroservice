package com.kkl.kklplus.b2b.joyoung.config;

import com.kkl.kklplus.b2b.joyoung.handler.SecurityHandler;
import com.kkl.kklplus.b2b.joyoung.http.config.B2BTooneProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 *
 * @author: Jeff.Zhao
 * @date: 2018/8/20 10:08
 */
@Configuration
public class ApiConfig extends WebMvcConfigurerAdapter {
    @Autowired
    SecurityHandler securityHandler;
    @Autowired
    B2BTooneProperties tooneProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(tooneProperties.getMethods() != null) {
            registry.addInterceptor(securityHandler).addPathPatterns(tooneProperties.getMethods());
            super.addInterceptors(registry);
        }
    }
}
