package com.oraclecode._2017;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * CDI Producer and destroyer for (Binary) Jedis objects. 
 * This is a singleton (@ApplicationScoped) CDI bean
 */
@ApplicationScoped
public class PooledJedisProducer {
    private JedisPool pool = null; 
    
    /**
     * sets up the pool.. invoked only once since this is a singleton
     */
    @PostConstruct
    public void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        config.setMaxWaitMillis(5000);
        config.setMaxTotal(15);
        
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "192.168.99.100");
        String redisPort = System.getenv().getOrDefault("REDIS_PORT", "6379");
        
        pool = new JedisPool(config, redisHost, Integer.valueOf(redisPort), 60000 ,"M@y@2016");
        //pool = new JedisPool(config, redisHost, Integer.valueOf(redisPort), 60000);
        System.out.println("Jedis Pool initialized");
    }
    
    @Dependent
    @Produces
    public Jedis get(InjectionPoint ip){
        System.out.println("injecting Jedis from pool into " + ip.getMember().getDeclaringClass().getSimpleName());

        Jedis jedis = pool.getResource();
        System.out.println("Got resource from Jedis pool");
        return jedis;
    }
    
    public void returnB(@Disposes Jedis jedis){
        pool.returnResource(jedis);
        System.out.println("Returned resource to Jedis pool");
    }
    
    @PreDestroy
    public void close(){
        pool.close();
        System.out.println("Jedis pool shutdown");
    }
}