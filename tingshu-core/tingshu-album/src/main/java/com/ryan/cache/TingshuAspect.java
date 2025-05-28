package com.ryan.cache;

import com.ryan.util.SleepUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class TingshuAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter bloomFilter;

    // 2. 切面编程 + Redisson + 分布式锁

    @Around("@annotation(com.ryan.cache.TingshuCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 拿到目标方法上的参数
        Object[] methodParams = joinPoint.getArgs();
        // 2. 拿到目标方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        // 3. 拿到目标方法上的注解@TingshuCache
        TingshuCache tingShuCache = targetMethod.getAnnotation(TingshuCache.class);
        // 4. 拿到注解上的值
        String prefix = tingShuCache.value();
        Object firstParam = methodParams[0];

        String cacheKey = prefix + ":" + firstParam;
        Object redisObject = redisTemplate.opsForValue().get(cacheKey);
        String lockKey="lock-" + firstParam;
        RLock lock = redissonClient.getLock(lockKey);
        if (redisObject == null) {
            lock.lock();
            try {
                // 先判断布隆过滤器中是否存在albumId
                // 布隆里没有那数据库绝对没有；布隆里有那数据库可能有-> 查数据库
                boolean flag = bloomFilter.contains(firstParam);
                if (flag) {
                    Object objectDb = joinPoint.proceed();
                    redisTemplate.opsForValue().set(cacheKey, objectDb);
                    return objectDb;
                }
            } finally {
                lock.unlock();
            }
        }
        return redisObject;
    }

    // 1. 切面编程 + redis + ThreadLocal + 分布式锁
    ThreadLocal<String> threadLocal = new ThreadLocal<>();
//    @Around("@annotation(com.ryan.cache.TingshuCache)")
    public Object cacheAroundAdviceFirst(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 拿到目标方法上的参数
        Object[] methodParams = joinPoint.getArgs();
        // 2. 拿到目标方法
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        // 3. 拿到目标方法上的注解@TingshuCache
        TingshuCache tingShuCache = targetMethod.getAnnotation(TingshuCache.class);
        // 4. 拿到注解上的值
        String prefix = tingShuCache.value();

        Object firstParam = methodParams[0];

        String cacheKey = prefix + ":" + firstParam;
        Object redisObject = redisTemplate.opsForValue().get(cacheKey);
        //锁的粒度太大
        String lockKey="lock-"+ firstParam;
        if (redisObject == null) {
            String token = threadLocal.get();
            boolean accquireLock = false;
            if (!StringUtils.isEmpty(token)) {
                //已经拿到过锁了
                accquireLock = true;
            } else {
                token = UUID.randomUUID().toString();
                accquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 3, TimeUnit.SECONDS);
            }
            if (accquireLock) {
                // 执行目标方法
                Object objectDb = joinPoint.proceed();
                redisTemplate.opsForValue().set(cacheKey, objectDb);
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptText(luaScript);
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Arrays.asList(lockKey), token);
                //擦屁股
                threadLocal.remove();
                return objectDb;
            } else {
                while (true) {
                    SleepUtils.millis(50);
                    boolean retryAccquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 3, TimeUnit.SECONDS);
                    if (retryAccquireLock) {
                        threadLocal.set(token);
                        break;
                    }
                }
                return cacheAroundAdvice(joinPoint);
            }
        }
        return redisObject;

    }
}
