package com.oracle.cloud.accs.ehcs.producer.lcm;

import com.oracle.cloud.accs.ehcs.producer.Producer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 *
 * Implements logic to manage Producer thread using ExecutorService. Used internally by ProducerManagerResource
 */
public final class ProducerLifecycleManager {
private static final Logger LOGGER = Logger.getLogger(ProducerLifecycleManager.class.getName());
    private ExecutorService es;
    private static ProducerLifecycleManager INSTANCE = null;
    private final AtomicBoolean RUNNING = new AtomicBoolean(false);
    
    private ProducerLifecycleManager() {
        
        es = Executors.newSingleThreadExecutor();
    }
    
    public static ProducerLifecycleManager getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ProducerLifecycleManager();
        }
        return INSTANCE;
    }
    public void start() throws Exception{
        if(RUNNING.get()){
            throw new IllegalStateException("Service is already running");
        }
        if(es.isShutdown()){
            es = Executors.newSingleThreadExecutor();
            System.out.println("Reinit executor service");
        }
        es.execute(new Producer());
        LOGGER.info("started producer thread");
        RUNNING.set(true);
    }
    
    public void stop() throws Exception{
        if(!RUNNING.get()){
            throw new IllegalStateException("Service is NOT running. Cannot stop");
        }
        es.shutdownNow();
        LOGGER.info("stopped producer thread");
        RUNNING.set(false);
    }
    
}
