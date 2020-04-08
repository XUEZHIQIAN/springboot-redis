package com.hang.redis.business.impl;

import com.hang.redis.business.QueryBusiness;
import com.hang.redis.utils.RedisUtils;
import com.hang.redis.utils.impl.RedisUtilsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Business
 *
 * @author Hang W
 */
@Component
@SuppressWarnings("all")
public class QueryBusinessImpl implements QueryBusiness {

    private final static Logger logger = LoggerFactory.getLogger(RedisUtilsImpl.class);

    private final static String STOCK = "noodles";

    @Autowired
    private RedisUtils redisUtils;

    /**
     * 根据key查询value
     *
     * @param key
     * @return
     */
    @Override
    public String query(String key) {
        return redisUtils.get(key);
    }

    /**
     * 业务场景
     * 从数据库查询库存，如果存在则减少库存
     *
     * @return
     */
    @Override
    public int getStock(String key) {

        // lock
        boolean flag = redisUtils.lock(STOCK, com.hang.redis.utils.StringUtils.getRequestId(), 120000); // time 120000

        int count = 0;
        try {
            if(flag) {
                logger.info("QueryBusinessImpl.getStock Key: {}", key);
                String value = redisUtils.get(key);
                logger.info("QueryBusinessImpl.getStock Value: {}", value);
                if(!StringUtils.isEmpty(value)) {
                    count = Integer.parseInt(value);
                    if(0 < count) {
                        count--;
                        redisUtils.set(key, "" + count);
                    } else {
                        return count;
                    }
                } else {
                    return count;
                }

                // nulock
                redisUtils.unlock(STOCK);
            }
        } catch (Exception e) {
            logger.info("QueryBusinessImpl.getStock Exception: {}", e);
            redisUtils.unlock(STOCK);
        } finally {
            redisUtils.unlock(STOCK);
        }
        return count;
    }

}
