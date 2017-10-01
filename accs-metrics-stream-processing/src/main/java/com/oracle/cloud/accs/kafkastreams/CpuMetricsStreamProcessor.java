package com.oracle.cloud.accs.kafkastreams;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class CpuMetricsStreamProcessor implements Processor<String, String> {

    private final Jedis redis;
    private final JedisPool pool;
    //private ProcessorContext pc;
    private KeyValueStore<String, Double> machineToAvgCPUUsageStore;
    private KeyValueStore<String, Integer> machineToNumOfRecordsReadStore;
    private final String metricsRedisSortedSet;

    public CpuMetricsStreamProcessor() {
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "192.168.99.100");
        String redisPort = System.getenv().getOrDefault("REDIS_PORT", "6379");
        metricsRedisSortedSet = System.getenv().getOrDefault("REDIS_METRICS_SORTED_SET", "cpu-leaderboard");

        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        config.setMaxWaitMillis(5000);
        config.setMaxTotal(15);
        //redis = new Jedis(redisHost, Integer.valueOf(redisPort), 10000);
        pool = new JedisPool(config, redisHost, Integer.valueOf(redisPort), 60000 ,"M@y@2016");
        redis = pool.getResource();

        System.out.println("Connected to Redis " + redis);

    }

    @Override
    public void init(ProcessorContext pc) {

        System.out.println("Processor initialized in thread " + Thread.currentThread().getName());
        this.machineToAvgCPUUsageStore = (KeyValueStore<String, Double>) pc.getStateStore(CpuMetricsAnalysisJob.AVG_STORE_NAME);
        this.machineToNumOfRecordsReadStore = (KeyValueStore<String, Integer>) pc.getStateStore(CpuMetricsAnalysisJob.NUM_RECORDS_STORE_NAME);
    }

    @Override
    public void process(String machineID, String currentCPUUsage) {

        System.out.println("Machine - " + machineID);
        System.out.println("CPU - " + currentCPUUsage);

        Double currentCPUUsageD = Double.parseDouble(currentCPUUsage);
        Integer recordsReadSoFar = machineToNumOfRecordsReadStore.get(machineID);
        Double latestCumulativeAvg = null;

        if (recordsReadSoFar == null) {
            System.out.println("First record for machine " + machineID);
            machineToNumOfRecordsReadStore.put(machineID, 1);
            latestCumulativeAvg = currentCPUUsageD;
        } else {
            Double cumulativeAvgSoFar = machineToAvgCPUUsageStore.get(machineID);
            System.out.println("CMA so far " + cumulativeAvgSoFar);

            //refer https://en.wikipedia.org/wiki/Moving_average#Cumulative_moving_average for details
            latestCumulativeAvg = (currentCPUUsageD + (recordsReadSoFar * cumulativeAvgSoFar)) / (recordsReadSoFar + 1);

            recordsReadSoFar = recordsReadSoFar + 1;
            machineToNumOfRecordsReadStore.put(machineID, recordsReadSoFar);
            System.out.println("Total records for machine " + machineID + " so far " + recordsReadSoFar);

        }

        machineToAvgCPUUsageStore.put(machineID, latestCumulativeAvg); //store latest CMA in local state store
        
        //1. push current machine CPU usage to list (for individual mahine metric)
        redis.lpush(machineID, currentCPUUsage);
        redis.ltrim(machineID, 0, 109); //110 elements
        
        System.out.println("Updated Machine list in Redis....");
        
        //2. push the cumulative avg to sorted set
        redis.zadd(metricsRedisSortedSet, latestCumulativeAvg, machineID);
        System.out.println("Updated CPU leaderboard in Redis....");

    }

    @Override
    public void punctuate(long l) {
        //no-op in this case
    }

    @Override
    public void close() {
        System.out.println("Closing processor...");
        //redis.close();
        pool.returnResource(redis);
        pool.close();
        System.out.println("Redis connection pool closed..");
    }

}
