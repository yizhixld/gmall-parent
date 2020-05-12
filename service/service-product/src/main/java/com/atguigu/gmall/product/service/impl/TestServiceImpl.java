package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author yizhixld
 * @create 2020-04-23-10:41
 */
@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void testLock() {
        // 使用redis+lua脚本实现分布式锁
//        String uuid = UUID.randomUUID().toString();
//        String skuId = "1";
//        String lockKey = "lock" + skuId;
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid,10, TimeUnit.SECONDS);
//        if(lock){
//            String num = (String)redisTemplate.opsForValue().get("num");
//            if(StringUtils.isEmpty(num)){
//                return;
//            }
//            int i = Integer.parseInt(num);
//            redisTemplate.opsForValue().set("num",String.valueOf(++i));
//            String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            DefaultRedisScript<Long> redisScript = new DefaultRedisScript();
//            redisScript.setScriptText(script);
//            redisScript.setResultType(Long.class);
//              redisTemplate.execute(redisScript,Arrays.asList(lockKey),uuid);
//        }else {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            this.testLock();
//        }
        // 使用redission实现分布式锁
        String skuId = "1";
        String lockKey = "lock:" + skuId;
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        String num = redisTemplate.opsForValue().get("num");
        if(StringUtils.isEmpty(num)){
            redisTemplate.opsForValue().set("num","1");
        }else{
            int i = Integer.parseInt(num);
            redisTemplate.opsForValue().set("num", String.valueOf(++i));
        }
        lock.unlock();
    }
}
