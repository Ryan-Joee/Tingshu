package com.ryan.service;

import com.ryan.SystemFeignClient;
import com.ryan.custom.CustomUser;
import com.ryan.entity.SysUser;
import com.ryan.result.RetVal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SystemFeignClient systemFeignClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RetVal<SysUser> sysUserRetVal = systemFeignClient.getByUsername(username);
        SysUser sysUser = sysUserRetVal.getData();
        if(null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }

        if(!"admin".equals(sysUser.getUsername()) && sysUser.getStatus().intValue() == 0) {
            throw new RuntimeException("账号已停用");
        }
        RetVal<List<String>> userPermsListRetVal = systemFeignClient.findUserPermsList(sysUser.getId());
        List<String> userPermsList = userPermsListRetVal.getData();
        sysUser.setUserPermsList(userPermsList);
        return new CustomUser(sysUser);
    }
}