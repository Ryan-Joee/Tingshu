package com.ryan.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TingshuCache {
    // 定义一个属性
    String value() default "cache";

    // 是否使用布隆过滤器--默认使用
    boolean enableBloom() default true;
}
