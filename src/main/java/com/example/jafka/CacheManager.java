package com.example.jafka;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.time.DateUtils;

import java.util.Date;

 class CacheManager {

    private static MemCachedClient mc;


    static {
        mc = new MemCachedClient();
        PropertiesConfiguration properties = new PropertiesConfiguration();
        properties.setEncoding("UTF-8");
        try {
            properties.load("memcache.properties");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        String[] serverlist = properties.getStringArray("cacheServerList");
        String[] weightlist = properties.getStringArray("cacheServerWeights");
        Integer[] weights = new Integer[weightlist.length];
        for (int i = 0; i < weightlist.length; i++) {
            String s = weightlist[i];
            weights[i] = Integer.parseInt(s);
        }
// initialize the pool for memcache servers
        SockIOPool pool = SockIOPool.getInstance();
        pool.setServers(serverlist);
        pool.setWeights(weights);
        pool.setInitConn(properties.getInt("initialConnections"));
        pool.setMinConn(properties.getInt("minSpareConnections"));
        pool.setMaxConn(properties.getInt("maxSpareConnections"));
        pool.setMaxIdle(properties.getInt("maxIdleTime"));
        pool.setMaxBusyTime(properties.getInt("maxBusyTime"));
        pool.setMaintSleep(properties.getInt("maintThreadSleep"));
        pool.setSocketTO(properties.getInt("socketTimeOut"));
        pool.setSocketConnectTO(properties.getInt("socketConnectTO"));
        pool.setNagle(properties.getBoolean("nagleAlg"));
        pool.setHashingAlg(SockIOPool.NEW_COMPAT_HASH);
        pool.setFailback(properties.getBoolean("failback"));
        pool.setFailover(properties.getBoolean("failover"));
        pool.initialize();
        System.out.println(mc);
    }

    /**
     * 把对象放入缓存中
     *
     * @param key 键
     * @param obj 对象
     */
    public static void put(String key, Object obj) {

        mc.set(key, obj);
    }

    /**
     * 把对象放入缓存中，并且在指定时间后删除（秒）
     *
     * @param key     键
     * @param obj     对象
     * @param seconds 指定秒数
     */
    public static void put(String key, Object obj, int seconds) {
        Date time = DateUtils.addSeconds(new Date(), seconds);
        mc.set(key, obj, time);

    }

    /**
     * 删除
     */
    public static void remove(String key) {
        mc.delete(key);
    }

    /**
     * 得到
     */
    public static Object get(String key) {
        return mc.get(key);
    }

    /**
     * 判断是否存在
     */
    public static boolean exist(String key) {
        return mc.keyExists(key);
    }

    public static void flushAll() {
        mc.flushAll();
    }

    public static void flush(String key, Object value) {
        mc.replace(key, value);
    }

    public static void flush(String key, Object value, int seconds) {
        Date time = DateUtils.addSeconds(new Date(), seconds);
        mc.replace(key, value, time);
    }
public static void main(String[] args) {
	for (int i = 100; i < 1000; i++) {
		CacheManager.put("key"+i, String.valueOf(i));
	}
	for (int i = 0; i < 100; i++) {
		//System.out.println(CacheManager.get("key"+i));
	}
}
}
