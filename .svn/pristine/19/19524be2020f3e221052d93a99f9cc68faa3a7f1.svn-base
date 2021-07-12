package com.kkl.kklplus.b2b.joyoung.feign;

import com.kkl.kklplus.b2b.joyoung.feign.fallback.MSDictFeignFallbackFactory;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.common.MSPage;
import com.kkl.kklplus.entity.sys.SysDict;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "provider-sys", fallbackFactory = MSDictFeignFallbackFactory.class)
public interface MSDictFeign {

    //region 查询字典项

    @GetMapping("/dict/getListByType/{type}")
    MSResponse<List<SysDict>> getListByType(@PathVariable("type") String type);

    //endregion

}
