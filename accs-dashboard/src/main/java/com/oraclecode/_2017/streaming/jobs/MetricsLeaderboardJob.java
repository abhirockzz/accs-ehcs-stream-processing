package com.oraclecode._2017.streaming.jobs;

import com.oraclecode._2017.streaming.sse.LeaderboardSSEChannel;
import com.oraclecode._2017.streaming.model.Leaderboard;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Date;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

@Singleton
@Startup
public class MetricsLeaderboardJob {

    @Resource
    private TimerService ts;
    private Timer timer;

    @Inject
    private Jedis jedis;

    private String redisMetricsLeaderBoardSet;
    @PostConstruct
    public void init() {
        /**
         * fires 5 secs after creation interval = 5 secs non-persistent
         * no-additional (custom) info
         */
        timer = ts.createIntervalTimer(5000, 5000, new TimerConfig(null, false)); //trigger every 5 seconds
        Logger.getLogger(MetricsLeaderboardJob.class.getName()).log(Level.INFO, "MetricsLeaderboardJob initiated");

        redisMetricsLeaderBoardSet = System.getenv().getOrDefault("REDIS_METRICS_SORTED_SET", "cpu-leaderboard");
        System.out.println("Redis metrics leaderborad sorted set name "+ redisMetricsLeaderBoardSet);
    }

    private final ObjectMapper mapper = new ObjectMapper();    

    @Inject
    LeaderboardSSEChannel channel;
    
    @Timeout
    public void timeout(Timer timer) {
        Logger.getLogger(MetricsLeaderboardJob.class.getName()).log(Level.INFO, "Timer fired at {0}", new Date());

        //Set<Tuple> zrangeByScoreWithScores = jedis.zrevrangeByScoreWithScores(REDIS_SORTED_SET, Integer.MAX_VALUE, 1, 0, 10);
        //Set<Tuple> zrangeByScoreWithScores = jedis.zrevrangeByScoreWithScores(REDIS_SORTED_SET, Integer.MAX_VALUE, 1);
        Set<Tuple> zrangeByScoreWithScores = jedis.zrevrangeByScoreWithScores(redisMetricsLeaderBoardSet, Integer.MAX_VALUE, 1);

        Leaderboard lb = new Leaderboard();

        for (Tuple tuple : zrangeByScoreWithScores) {
            long roundedMetric = Math.round(tuple.getScore());
            lb.add(tuple.getElement(), String.valueOf(roundedMetric));
        }
        String json = null;
        try {
            json = mapper.writeValueAsString(lb);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MetricsLeaderboardJob.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        
        Logger.getLogger(MetricsLeaderboardJob.class.getName()).log(Level.INFO, " {0}", new Date());
        channel.broadcast("leaderboard_payload", json);
    }

    @PreDestroy
    public void close() {
        timer.cancel();
        System.out.println("Application shutting down. Timer purged");
    }
}