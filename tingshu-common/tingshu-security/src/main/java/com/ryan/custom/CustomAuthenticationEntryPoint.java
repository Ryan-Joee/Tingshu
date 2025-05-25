package com.ryan.custom;

import com.ryan.result.ResultCodeEnum;
import com.ryan.result.RetVal;
import com.ryan.util.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {

        ResponseUtil.out(response, RetVal.build(Optional.ofNullable(null), ResultCodeEnum.ACCOUNT_ERROR));
    }
}
