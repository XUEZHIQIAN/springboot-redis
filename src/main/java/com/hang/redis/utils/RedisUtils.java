package com.hang.redis.utils;

/**
 * redis api
 *
 * @author Hang W
 */
public interface RedisUtils {

    void set(String key, String value);

    void setnx(String key, String value);

    String get(String key);

    boolean lock(String lock, String requestId, long time);

    void unlock(String lock);

}
