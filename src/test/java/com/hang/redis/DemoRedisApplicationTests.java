package com.hang.redis;

import com.hang.redis.utils.RedisUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoRedisApplicationTests {

	@Autowired
	private RedisUtils redisUtils;

	@Test
	public void set() {
		redisUtils.set("name", "wanghang");
	}

	@Test
	public void setnx() {
		redisUtils.setnx("hang", "wanghang");
	}

}
