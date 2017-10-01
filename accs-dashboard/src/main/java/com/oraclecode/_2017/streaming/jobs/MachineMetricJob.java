package com.oraclecode._2017.streaming.jobs;

import java.util.Date;
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

@Singleton
@Startup
public class MachineMetricJob {

    @Resource
    private TimerService ts;
    private Timer timer;

    @PostConstruct
    public void init() {
        /**
         * fires 5 secs after creation interval = 5 secs non-persistent
         * no-additional (custom) info
         */
        timer = ts.createIntervalTimer(5000, 5000, new TimerConfig(null, false)); //trigger every 5 seconds
        System.out.println("MachineMetricJob initiated");

    }
    
    @Inject
    MachineMetricAsyncTask task;
    
    @Timeout
    public void timeout(Timer timer) {
        System.out.println("MachineMetricJob Timer fired on "+ new Date());
        String machine = null;
        
        for (int i = 1; i <= 10; i++) {
            machine = "machine-" + i;
            task.processAndBroadcastMachinMetric(machine);
        }

    }

    @PreDestroy
    public void close() {
        timer.cancel();
         System.out.println("Application shutting down. Timer purged");
    }
}
