package com.hang.redis.controller;

import com.hang.redis.business.QueryBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller
 *
 * @author Hang W
 */
@RestController
@RequestMapping("query")
@SuppressWarnings("all")
public class QueryController {

    @Autowired
    private QueryBusiness queryBusiness;

    /**
     * 根据key查询value
     *
     * @param key
     * @return
     */
    @GetMapping("name/{key}")
    public String query(@PathVariable String key) {

        return queryBusiness.query(key);

    }

    /**
     * redis分布式锁
     *
     * @param key
     * @return
     */
    @GetMapping("stock/{key}")
    public int getStock(@PathVariable String key) {

        return queryBusiness.getStock(key);

    }

}
