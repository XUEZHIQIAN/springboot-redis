package com.hang.redis.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * String
 *
 * @author Hang W
 */
public class StringUtils {

    private final static Logger logger = LoggerFactory.getLogger(StringUtils.class);

    /**
     * 获取请求id
     *
     * @return
     */
    public static String getRequestId() {
        String requestId = UUID.randomUUID().toString().replace("-", "");
        logger.info("StringUtils.getRequestId requestId: {}", requestId);
        return requestId;
    }

}
