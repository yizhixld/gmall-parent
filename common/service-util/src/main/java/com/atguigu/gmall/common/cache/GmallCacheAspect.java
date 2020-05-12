package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author yizhixld
 * @create 2020-04-24-23:01
 */
@Component
@Aspect
public class GmallCacheAspect {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point){
        Object result = null ;
        // 获得传递的参数
        Object[] args = point.getArgs();
        //获得方案签名
        MethodSignature signature = (MethodSignature) point.getSignature();
        //得到注解
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);
        String prefix = gmallCache.prefix();
        String key = prefix + Arrays.asList(args).toString();
        // 正常先查询缓存
        result = cacheHit(signature,key);
        if(result == null ){
            RLock lock = redissonClient.getLock(key + ":lock");
            try {
                boolean tryLock = lock.tryLock(100, 10, TimeUnit.SECONDS);
                if(tryLock){
                    // 获取业务数据：得到带注解的方体执行结果，执行相关业务代码
                    result = point.proceed(point.getArgs());
                    if(result == null ){
                        Object o = new Object();
                        redisTemplate.opsForValue().set(key,JSONObject.toJSONString(o), RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        return o;
                    }else{
                        redisTemplate.opsForValue().set(key,JSONObject.toJSONString(result),RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        return result;
                    }
                }else {
                    Thread.sleep(500);
                    return cacheHit(signature,key);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }finally {
                lock.unlock();
            }
        }return result;
    }

    private Object cacheHit(MethodSignature signature,String key) {
        String result = (String) redisTemplate.opsForValue().get(key);
        if(StringUtils.isEmpty(result)){
            return null;
        }else{
            Class returnType = signature.getReturnType();
            return JSONObject.parseObject(result,returnType);
        }
    }
}
