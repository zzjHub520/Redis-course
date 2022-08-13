import org.junit.Test;
import redis.clients.jedis.Jedis;

public class Learning {

    //    Jedis jedis = JedisUtils.getJedis();
    Jedis jedis = new Jedis("localhost", 6379);
    @Test
    public void aa(){
        jedis.flushDB();
        System.out.println("===========添加一个list===========");
        jedis.lpush("collections", "ArrayList", "Vector", "Stack", "HashMap", "WeakHashMap", "LinkedHashMap");
        System.out.println("collections的内容：" + jedis.lrange("collections", 0, -1));
        //[start,stop]闭区间 -1代表倒数第一个元素，-2代表倒数第二个元素,end为-1表示查询全部
        System.out.println("collections区间0-3的元素：" + jedis.lrange("collections", 0, 3));
        System.out.println("===============================");
    }
}
