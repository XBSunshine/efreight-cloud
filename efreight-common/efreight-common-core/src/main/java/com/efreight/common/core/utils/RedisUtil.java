package com.efreight.common.core.utils;


import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @program:efreight-cloud
 * @description:
 * @author:shihongkai
 * @create:2021-01-13
 **/
@Component
public class RedisUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    @Autowired
    static private RedisTemplate redisTemplate;

    static private  RedisTemplate getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = ApplicationContextHolder.popBean("redisTemplate",RedisTemplate.class);
        }
        return redisTemplate;
    }


    /**
     * Clears this cache instance
     */
    public static void clear() {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.execute((RedisCallback) connection -> {
            connection.flushDb();
            return null;
        });
    }

    /**
     * Remove cached query result from redis
     *
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Object removeObject(Object key) {
        try {
            RedisTemplate redisTemplate = getRedisTemplate();
            redisTemplate.delete(key);
            logger.debug("Remove cached query result from redis");
        }
        catch (Exception t) {
            logger.error("Redis remove failed", t);
        }
        return null;
    }


    /**
     * Put query result to redis
     *
     * @param key
     * @param value
     */
    @SuppressWarnings("unchecked")
    public static void putObject(Object key, Object value, long EXPIRE_TIME, TimeUnit timeUnit) {
        try {
            RedisTemplate redisTemplate = getRedisTemplate();
            ValueOperations opsForValue = redisTemplate.opsForValue();
            opsForValue.set(key, value, EXPIRE_TIME, timeUnit);
            logger.debug("Put query result to redis");
        }
        catch (Exception t) {
            logger.error("Redis put failed", t);
        }
    }

    /**
     * Put geo result to redis
     *
     * @param key
     * @param m
     */
    @SuppressWarnings("unchecked")
    public static Long geoAdd(Object key, Point point, Object m) {
        try {
            RedisTemplate redisTemplate = getRedisTemplate();
            GeoOperations geoValue = redisTemplate.opsForGeo();
            logger.debug("geoAdd result to redis");
            return geoValue.geoAdd(key,point,m);

        }
        catch (Exception t) {
            logger.error("geoAdd failed", t);
        }
        return null;
    }

    /**
     * geo dist result to redis
     *
     * @param key
     * @param m1,m2
     */
    @SuppressWarnings("unchecked")
    public static Distance geoDist(Object key, Object m1, Object m2) {
        try {
            RedisTemplate redisTemplate = getRedisTemplate();
            GeoOperations geoValue = redisTemplate.opsForGeo();
            logger.debug("geoAdd result to redis");
            return  geoValue.geoDist(key,m1,m1);
        }
        catch (Exception t) {
            logger.error("geoAdd failed", t);
        }
        return null;
    }

    /**
     * nearDist
     * @param key
     * @param circle
     * @param args
     * @return
     */
    public static GeoResults<RedisGeoCommands.GeoLocation> nearDist(Object key, Circle circle, RedisGeoCommands.GeoRadiusCommandArgs args) {
        try {
            RedisTemplate redisTemplate = getRedisTemplate();
            GeoOperations geoValue = redisTemplate.opsForGeo();
            logger.debug("geoAdd result to redis");
            return  geoValue.geoRadius(key,circle,args);
        }
        catch (Exception t) {
            logger.error("geoAdd failed", t);
        }
        return null;
    }



    public static double zscore(String key, Object member) {
        double value = 0;
        RedisTemplate redisTemplate = getRedisTemplate();
        value = redisTemplate.opsForZSet().score(key,member);
        return value;
    }

    /***
     * <p>
     * Description: 得到值
     * </p>
     *
     * @param key
     */
    public static Object get(String key) {
        Object value = null;
        RedisTemplate redisTemplate = getRedisTemplate();
        value = redisTemplate.opsForValue().get(key);
        return value;
    }

    /***
     * <p>
     * Description: 设置键值
     * </p>
     * @param key
     *            value
     */
    public static void set(String key, Object value) {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.opsForValue().set(key,value);
    }

    /***
     * <p>
     * Description: 设置键值 并同时设置有效期
     * </p>
     * @param key
     *            seconds秒数 value
     */
    public static void setex(String key, String value) {
        RedisTemplate redisTemplate = getRedisTemplate();
        redisTemplate.opsForValue().setIfAbsent(key,value);
    }

    /**
     *判断redis中是否已存在key对应的value
     * @param key
     * @return true/false
     *
     */
    public static boolean isExistValue(String key){
        if (StringUtils.isBlank(key)){
            return false;
        }

        // 根据key获取redis的value
        RedisTemplate redisTemplate = getRedisTemplate();
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null){
            return false;
        }
        return true;
    }

    /**
     *判断redis中是否已存在key
     * @param key
     * @return true/false
     *
     */
    public static boolean isExistKey(String key){
        if (StringUtils.isBlank(key)){
            return false;
        }
        RedisTemplate redisTemplate = getRedisTemplate();
        return redisTemplate.hasKey(key);
    }
}
