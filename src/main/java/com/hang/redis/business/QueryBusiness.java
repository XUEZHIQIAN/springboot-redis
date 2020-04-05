package com.hang.redis.business;

/**
 * Business api
 *
 * @author Hang W
 */
public interface QueryBusiness {

    String query(String key);

    int getStock(String key);

}
