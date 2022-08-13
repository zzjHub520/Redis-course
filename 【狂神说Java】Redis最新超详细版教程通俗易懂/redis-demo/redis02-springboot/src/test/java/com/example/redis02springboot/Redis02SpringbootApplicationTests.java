package com.example.redis02springboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class Redis02SpringbootApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void contextLoads() {

        // redisTemplate  操作不同的数据类型，api和我们的指令是一样的
        // opsForValue
        // opsForList
        // opsForSet
        // opsForHash
        // opsForZSet

//        获取Redis的连接对象
//        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
//        connection.flushAll();
//        connection.flushDb();


        redisTemplate.opsForValue().set("key1", "kuangshenshuo");
        System.out.println(redisTemplate.opsForValue().get("key1"));
    }

}
