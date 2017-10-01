package com.oraclecode._2017.streaming.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oraclecode._2017.streaming.model.MachineMetric;
import com.oraclecode._2017.streaming.sse.MachineMetricSSEChannel;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import redis.clients.jedis.Jedis;

@Stateless
public class MachineMetricAsyncTask {
    
    @Inject
    Jedis jedis;
    
    private static final ObjectMapper mapper = new ObjectMapper();

    @Inject
    MachineMetricSSEChannel channel;
    
    @Asynchronous
    public void processAndBroadcastMachinMetric(String machine) {
        System.out.println("MachineMetricAsyncTask for machine " + machine + " running in thread " + Thread.currentThread().getName());

        List<String> machineMetrics = jedis.lrange(machine, 0, 109); //first 110 elements
        Collections.reverse(machineMetrics);
        
        List<String> roundedMachineMetrics = machineMetrics.stream()
                .map((metric) -> Math.round(Double.valueOf(metric)))
                .map((lMetric) -> String.valueOf(lMetric))
                .collect(Collectors.toList());
        
        //MachineMetric mMetricPOJO = new MachineMetric(machine, machineMetrics);
        MachineMetric mMetricPOJO = new MachineMetric(machine, roundedMachineMetrics);
        String mMetricJSON = null;
        try {
            mMetricJSON = mapper.writeValueAsString(mMetricPOJO);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(MachineMetricJob.class.getName()).log(Level.SEVERE, ex.getMessage());
        }

        channel.broadcast("machine_metrics", mMetricJSON);
    }
}
