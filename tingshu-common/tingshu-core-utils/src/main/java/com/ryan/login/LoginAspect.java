package com.ryan.login;

import com.ryan.constant.RedisConstant;
import com.ryan.entity.UserInfo;
import com.ryan.exception.TingshuException;
import com.ryan.result.ResultCodeEnum;
import com.ryan.util.AuthContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class LoginAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    // 表示只要有TingshuLogin注解就进行切面逻辑
    @Around("@annotation(com.ryan.login.TingshuLogin)")
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        // 拿到请求中带的token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String token = request.getHeader("token");

        // 先拿到目标方法，再拿到目标方法上的注解
        // 需要通过切点来获取目标方法。 方法上有@TingshuLogin注解的即为切点
        MethodSignature signature = (MethodSignature) joinPoint.getSignature(); // 先获取当前被调用的方法的签名
        Method targetMethod = signature.getMethod(); // 获取到方法对象targetMethod
        TingshuLogin tingshuLogin = targetMethod.getAnnotation(TingshuLogin.class); // 获取这个方法上的@TingshuLogin注解。

        if (tingshuLogin.required()) {
            if (StringUtils.isEmpty(token)) { // 这里判断token为null，为null就是没登录
                // 需要登录 TODO 后面详细解释
                throw new TingshuException(ResultCodeEnum.UN_LOGIN);
            }
            // token过期 -> 登录过期了 -> 需要重新登录
            UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
            if (userInfo == null) {
                throw new TingshuException(ResultCodeEnum.UN_LOGIN);
            }
        }

        // 看redis里面是否有登录信息
        if (!StringUtils.isEmpty(token)) {
            UserInfo userInfo = (UserInfo) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX + token);
            if (userInfo != null) {
                // 这是一个线程独享的一个区域
                AuthContextHolder.setUserId(userInfo.getId());
                AuthContextHolder.setUsername(userInfo.getNickname());
            }
        }
        // 执行目标方法，表示继续执行被拦截的方法。如果不调用这个方法，原方法就不会被执行。
        return joinPoint.proceed();
    }
}
