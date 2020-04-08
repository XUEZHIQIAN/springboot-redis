package com.hang.redis.utils.impl;

import com.hang.redis.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis 实现类
 *
 * @author Hang W 
 */
@Component
@SuppressWarnings("all")
public class RedisUtilsImpl implements RedisUtils {

    private final static Logger logger = LoggerFactory.getLogger(RedisUtilsImpl.class);

    private ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<Map<String, Integer>>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void set(String key, String value) {
        logger.info("RedisUtilsImpl.set Key: {}, Value: {}", key, value);
        redisTemplate.boundValueOps(key).set(value);

    }

    @Override
    public void setnx(String key, String value) {
        logger.info("RedisUtilsImpl.set Key: {}, Value: {}", key, value);
        redisTemplate.boundValueOps(key).setIfAbsent(value, 10000, TimeUnit.MILLISECONDS);

    }

    @Override
    public String get(String key) {
        String value;
        try{
            logger.info("RedisUtilsImpl.get Key: {}", key);
            value = redisTemplate.boundValueOps(key).get();
            logger.info("RedisUtilsImpl.get Value: {}", value);
        } catch (Exception e) {
            logger.info("RedisUtilsImpl.get Exception: {}", e);
            return "";
        }
        return value;
    }

    /**
     * 分布式锁
     * 1.全局key
     * 2.唯一请求id —— 请求次数（可重入）
     * 3.过期时间
     * 4.非阻塞
     * 5.看门狗
     *
     * @param lock
     * @param requestId
     * @param time
     * @return
     */
    @Override
    public boolean lock(String lock, String requestId, long time) {
        Boolean flag = false;
        try{
            Map<String, Integer> map = threadLocal.get();
            logger.info("RedisUtilsImpl.lock ThreadLocal: {}", map);
            if(CollectionUtils.isEmpty(map)) {
                map = new HashMap<>();
                map.put(requestId, 1);
                threadLocal.set(map);

                logger.info("RedisUtilsImpl.lock Key: {}, Value: {}, Time: {}", lock, requestId, time);
                flag = redisTemplate.boundValueOps(lock).setIfAbsent(requestId, time, TimeUnit.SECONDS);
                logger.info("RedisUtilsImpl.lock Return: {}", flag);

                // 非阻塞
                if(!flag) {
                    new Thread(() -> {
                        while(true) {
                            Boolean f = redisTemplate.boundValueOps(lock).setIfAbsent(requestId, time, TimeUnit.SECONDS);
                            logger.info("RedisUtilsImpl.lock new Thread: {}", f);
                            if(f)
                                break;
                        }
                    }).start();
                }

                /**
                 * 看门狗
                 * 锁默认失效时间为30秒
                 *
                 * 1.先查看requestId是否被替换.
                 * 2.如果没有被替换，查看ttl，还是多少时间，20s <= time 临界值时，就再继续延迟30s.
                 * 3.如果已经被替换，表示逻辑已经执行完毕.
                 */
                new Thread(() -> {
                    while(true) {
                        String getRequestId = redisTemplate.boundValueOps(lock).get();
                        logger.info("RedisUtilsImpl.lock GetRequestId: {}", getRequestId);
                        if(StringUtils.isEmpty(getRequestId)) {
                            break;
                        } else if(getRequestId.equals(requestId)) {
                            Long expireTime = redisTemplate.getExpire(lock);
                            logger.info("RedisUtilsImpl.lock ExpireTime: {}", expireTime);
                            if(20 <= expireTime) {
                                redisTemplate.expire(lock, time, TimeUnit.MILLISECONDS);
                            }
                        } else {
                            break;
                        }
                    }
                }).start();

            } else {
                Integer count = map.get(requestId);
                map.put(requestId, count++);
                threadLocal.set(map);
            }
        } catch (Exception e) {
            logger.info("RedisUtilsImpl.lock Exception: {}", e);
            redisTemplate.delete(lock);
        }
        return flag;
    }

    /**
     * 业务代码执行完毕删除key，后续请求可以获取key
     *
     * @param lock
     */
    @Override
    public void unlock(String lock) {
        try {
            Map<String, Integer> map = threadLocal.get();
            logger.info("RedisUtilsImpl.unlock ThreadLocal: {}", map);
            if(!CollectionUtils.isEmpty(map)) {
                String requestId = (String) map.keySet().toArray()[0];
                Integer count = (Integer) map.values().toArray()[0];
                String getRequestId = redisTemplate.boundValueOps(lock).get();
                if(requestId.equals(getRequestId) && 1 < count) {
                    logger.info("RedisUtilsImpl.unlock Key: {}, Count(?): {}", lock, count);
                    map.put(requestId, count--);
                    threadLocal.set(map);
                } else if (requestId.equals(getRequestId) && 1 == count) {
                    logger.info("RedisUtilsImpl.unlock Key: {}, Count(=1): {}", lock, count);
                    redisTemplate.delete(lock);
                    threadLocal.remove();
                }
            }
        } catch (Exception e) {
            logger.info("RedisUtilsImpl.unlock Exception: {}", e);
            redisTemplate.delete(lock);
        } finally {
            redisTemplate.delete(lock);
        }
    }

}
