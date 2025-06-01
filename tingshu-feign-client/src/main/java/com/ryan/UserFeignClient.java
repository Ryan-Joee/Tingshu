package com.ryan;

import com.ryan.result.RetVal;
import com.ryan.vo.UserInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "tingshu-user")
public interface UserFeignClient {
    @GetMapping("/api/user/userInfo/getUserById/{userId}")
    RetVal<UserInfoVo> getUserById(@PathVariable Long userId);
}
