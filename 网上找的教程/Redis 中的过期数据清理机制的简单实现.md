# [Redis 中的过期数据清理机制的简单实现](https://www.cnblogs.com/xtyuns/p/15467763.html)

目前常见的过期清理机制有: 惰性清理、定时清理、定期清理
在 Redis 中采用: 定期清理 + 惰性清理机制来删除过期数据

## 惰性清理机制[#](https://www.cnblogs.com/xtyuns/p/15467763.html#惰性清理机制)

```java
package com.xtyuns.redisclean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 惰性清理机制
 * 优点: 无需额外操作, 仅在取值前判断是否过期即可
 * 缺点: 如果过期的数据不再进行取值操作就会一直存在, 浪费内存空间, 因此在 Redis 中需要配合其他策略来使用
 */
public class ALazyClean {
    private static final Map<String, String> redisMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();

    /**
     * 向容器中添加数据
     * @param key 键
     * @param value 值
     * @param expire 设置有效时间, null 表示不自动过期
     */
    public static void set(String key, String value, Long expire) {
        redisMap.put(key, value);
        if (null != expire) {
            expireMap.put(key, System.currentTimeMillis() + expire);
        }
    }

    /**
     * 惰性清理, 当取值时才进行数据清理
     * @param key 所取数据的键
     * @return 返回指定数据, 数据失效时返回 null
     */
    public static String get(String key) {
        Long end = expireMap.get(key);
        if (null != end && System.currentTimeMillis() > end) {
            redisMap.remove(key);
            expireMap.remove(key);
        }

        return redisMap.get(key);
    }

    public static void main(String[] args) throws InterruptedException {
        set("k1", "v1", null);
        set("k2", "v2", 2000L);

        TimeUnit.SECONDS.sleep(3);

        System.out.println(get("k1"));
        System.out.println(get("k2"));
    }
}
```

## 定时清理机制[#](https://www.cnblogs.com/xtyuns/p/15467763.html#定时清理机制)

```java
package com.xtyuns.redisclean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 定时清理机制
 * 优点: 实时删除, 内存中不存在已过期的数据
 * 缺点: 当存在大量定时数据时, 需要巨额的线程开销, 因此在 Redis 中没有采用这种策略
 */
public class BOnTimeClean {
    private static final Map<String, String> redisMap = new HashMap<>();
    private static final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(
            10,
            20,
            30,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     * 向容器中添加数据, 当该数据设置有效时间时为该数据启动一条清理线程, 在数据有效期结束时删除该数据
     * @param key 键
     * @param value 值
     * @param expire 设置有效时间, null 表示不自动过期
     */
    public static void set(String key, String value, Long expire) {
        redisMap.put(key, value);
        if (null != expire) {
            poolExecutor.submit(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(expire);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                redisMap.remove(key);
            });
        }
    }

    /**
     * 获取指定数据
     * @param key 所取数据的键
     * @return 返回指定数据, 数据失效时返回 null
     */
    public static String get(String key) {
        return redisMap.get(key);
    }

    public static void main(String[] args) throws InterruptedException {
        set("k1", "v1", null);
        set("k2", "v2", 2000L);

        TimeUnit.SECONDS.sleep(3);

        System.out.println(get("k1"));
        System.out.println(get("k2"));
    }
}
```

## 周期清理机制[#](https://www.cnblogs.com/xtyuns/p/15467763.html#周期清理机制)

```java
package com.xtyuns.redisclean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 周期清理机制
 * 优点: 避免了使用大量线程的开销, 同时周期性的清除掉了过期数据
 * 缺点: 在清理线程执行间隔对已过期的数据取值时会出现脏读现象, 因此在 Redis 中需要配合其他策略来使用
 */
public class CScheduleClean {
    private static final Map<String, String> redisMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();

    // 在程序开始时启用一条清理线程执行清理任务, 每次任务的时间间隔是 10 秒
    static {
        Thread doClean = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 这里不能通过 forEach 来删除, 因为会触发 ConcurrentModificationException
                Iterator<Map.Entry<String, Long>> iterator = expireMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> next = iterator.next();
                    if (System.currentTimeMillis() > next.getValue()) {
                        iterator.remove();
                        redisMap.remove(next.getKey());
                    }
                }
            }
        });
        doClean.setDaemon(true);
        doClean.start();
    }

    /**
     * 向容器中添加数据
     * @param key 键
     * @param value 值
     * @param expire 设置有效时间, null 表示不自动过期
     */
    public static void set(String key, String value, Long expire) {
        redisMap.put(key, value);
        if (null != expire) {
            expireMap.put(key, System.currentTimeMillis() + expire);
        }
    }

    /**
     * 获取指定数据
     * @param key 所取数据的键
     * @return 返回指定数据, 数据失效时返回 null
     */
    public static String get(String key) {
        return redisMap.get(key);
    }

    public static void main(String[] args) throws InterruptedException {
        set("k1", "v1", null);
        set("k2", "v2", 2000L);

        // 3 秒内清理线程还未执行, 因此出现脏读现象
        TimeUnit.SECONDS.sleep(3);

        System.out.println(get("k1"));
        System.out.println(get("k2"));
    }
}
```

## 周期 + 惰性清理机制[#](https://www.cnblogs.com/xtyuns/p/15467763.html#周期--惰性清理机制)

```java
package com.xtyuns.redisclean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 周期 + 惰性清理机制
 * 优点: 既保证了内存中不会永久存储过期数据, 也解决了脏读问题
 * 缺点: 双重机制运行, 相较于惰性清理机制增加了清理线程的开销, 但是总体影响不大, Redis 中采用这种策略来清除过期数据
 *
 * tip: 在 Redis 的周期清理线程中并不是每次都遍历所有数据, 而是每次随机取出一定的数据进行遍历
 */
public class DScheduleLazyClean {
    private static final Map<String, String> redisMap = new HashMap<>();
    private static final Map<String, Long> expireMap = new HashMap<>();

    // 在程序开始时启用一条清理线程执行清理任务, 每次任务的时间间隔是 10 秒
    static {
        Thread doClean = new Thread(() -> {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 这里不能通过 forEach 来删除, 因为会触发 ConcurrentModificationException
                Iterator<Map.Entry<String, Long>> iterator = expireMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, Long> next = iterator.next();
                    if (System.currentTimeMillis() > next.getValue()) {
                        iterator.remove();
                        redisMap.remove(next.getKey());
                    }
                }
            }
        });
        doClean.setDaemon(true);
        doClean.start();
    }

    /**
     * 向容器中添加数据
     * @param key 键
     * @param value 值
     * @param expire 设置有效时间, null 表示不自动过期
     */
    public static void set(String key, String value, Long expire) {
        redisMap.put(key, value);
        if (null != expire) {
            expireMap.put(key, System.currentTimeMillis() + expire);
        }
    }

    /**
     * 周期 + 惰性清理, 当取值时触发二次数据清理机制
     * @param key 所取数据的键
     * @return 返回指定数据, 数据失效时返回 null
     */
    public static String get(String key) {
        Long end = expireMap.get(key);
        if (null != end && System.currentTimeMillis() > end) {
            redisMap.remove(key);
            expireMap.remove(key);
        }

        return redisMap.get(key);
    }

    public static void main(String[] args) throws InterruptedException {
        set("k1", "v1", null);
        set("k2", "v2", 2000L);

        TimeUnit.SECONDS.sleep(3);

        System.out.println(get("k1"));
        System.out.println(get("k2"));
    }
}
```

