package com.oraclecode._2017.streaming.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "machine",
    "metrics"
})
public class MachineMetric {

    @JsonProperty("machine")
    private String machine;
    
    @JsonProperty("metrics")
    private List<String> metrics = null;

    public MachineMetric() {
    //    metrics = new ArrayList<>();
    }

    public MachineMetric(String machine, List<String> metrics) {
        this.machine = machine;
        this.metrics = metrics;
    }
    
    @JsonProperty("machine")
    public String getMachine() {
        return machine;
    }

    @JsonProperty("metrics")
    public List<String> getMetrics() {
        return metrics;
    }

   
}
