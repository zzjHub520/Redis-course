import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ListPosition;

import java.util.Set;

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

    @Test
    public void testKey() {
        System.out.println("按索引查询：" + jedis.select(5));
        System.out.println("清空数据：" + jedis.flushDB());
        System.out.println("判断某个键是否存在：" + jedis.exists("username"));
        System.out.println("新增<'username','zzh'>的键值对：" + jedis.set("username", "zzh"));
        System.out.println("新增<'password','password'>的键值对：" + jedis.set("password", "password"));
        System.out.println("新增<'username1','zzh'>的键值对：" + jedis.set("username1", "zzh"));
        System.out.println("新增<'password1','password'>的键值对：" + jedis.set("password1", "password"));
        System.out.println("新增<'username2','zzh'>的键值对：" + jedis.set("username2", "zzh"));
        System.out.println("新增<'password2','password'>的键值对：" + jedis.set("password2", "password"));
        System.out.print("系统中所有的键如下：");
        Set<String> keys = jedis.keys("passwor*");
        System.out.println(keys);

    }

    @Test
    public void bb(){
        jedis.flushDB();
        System.out.println("===========添加一个list===========");
//        jedis.lpush("collections", "ArrayList", "Vector", "Stack", "HashMap", "WeakHashMap", "LinkedHashMap");
        jedis.linsert("collect", ListPosition.BEFORE,  "-1","WeakHashMap0");
//        jedis.lpush("collections", "ArrayList");
//        jedis.lpush("collections", "Vector");
//        jedis.lpush("collections", "Stack");
//        jedis.lpush("collections", "HashMap");
//        jedis.lpush("collections", "WeakHashMap");
//        jedis.lpush("collections", "LinkedHashMap");
    }



}
