package com.kkl.kklplus.b2b.joyoung.feign.fallback;

import com.kkl.kklplus.b2b.joyoung.feign.MSDictFeign;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.sys.SysDict;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MSDictFeignFallbackFactory implements FallbackFactory<MSDictFeign> {

    private static String errorMsg = "操作超时";

    @Override
    public MSDictFeign create(Throwable throwable) {
        log.error("====msDict==== {}",throwable.getMessage());
        return new MSDictFeign() {
            @Override
            public MSResponse<List<SysDict>> getListByType(String type) {
                return new MSResponse<>(MSErrorCode.newInstance(MSErrorCode.FAILURE, errorMsg));
            }
        };
    }
}
