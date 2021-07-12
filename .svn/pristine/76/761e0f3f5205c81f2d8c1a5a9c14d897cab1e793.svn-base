package com.kkl.kklplus.b2b.joyoung.handler;

import com.kkl.kklplus.b2b.joyoung.http.config.B2BTooneProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *安全验证
 * @author chenxj
 * @date 2019/08/19
 */
@Slf4j
@Configuration
public class SecurityHandler extends HandlerInterceptorAdapter {
    @Autowired
    private B2BTooneProperties tooneProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String appKey = "";
        String appSecret = "";
        try {
            appKey = request.getHeader("appKey") == null ? "" : request.getHeader("appKey");
            appSecret = request.getHeader("appSecret") == null ? "" : request.getHeader("appSecret");
        }catch (Exception e){
            log.error("权限验证", e);
        }
        if (appKey.equals(tooneProperties.getAppKey()) &&
                appSecret.equals(tooneProperties.getAppSecret())){
            return true;
        }
        throw new Exception("非法请求,身份验证失败.");
    }
}
