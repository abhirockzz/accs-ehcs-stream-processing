package com.oracle.cloud.accs.kafkastreams;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.Stores;

public class CpuMetricsAnalysisJob {

    private static final Logger LOGGER = Logger.getLogger(CpuMetricsAnalysisJob.class.getSimpleName());

    private static final String SOURCE_NAME = "cpu-metrics-source";
    String topicName = System.getenv().getOrDefault("OEHCS_TOPIC", "cpu-metrics");
    private static final String PROCESSOR_NAME = "cpu-metrics-processor";
    
    KafkaStreams streams = null;

    public KafkaStreams startPipeline() {

        Map<String, Object> configurations = new HashMap<>();

        configurations.put(StreamsConfig.APPLICATION_ID_CONFIG, Optional.ofNullable(System.getenv("STREAMS_APP_ID")).orElse("cpu-metrics-stream-app"));

        String kafkaBroker = System.getenv().getOrDefault("OEHCS_EXTERNAL_CONNECT_STRING", "192.168.99.100:9092");
        LOGGER.log(Level.INFO, "Kafa broker {0}", kafkaBroker);
        configurations.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker);

        configurations.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        configurations.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        //configurations.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 5);

        StreamsConfig config = new StreamsConfig(configurations);

        TopologyBuilder builder = processingTopologyBuilder();
        
        boolean connected = false;
        int retries = 0;

        do {
            LOGGER.info("Initiating Kafka Streams");
            try {
                streams = new KafkaStreams(builder, config);
                connected = true;
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during Kafka Stream initialization {0} .. retrying", e.getMessage());
                retries++;
            }

        } while (!connected && retries <= 1); //retry twice

        if (!connected) {
            LOGGER.warning("Unable to initialize Kafka Streams.. exiting");
            System.exit(0);
        }

        streams.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.log(Level.SEVERE, "Uncaught exception in Thread {0} - {1}", new Object[]{t, e.getMessage()});
                e.printStackTrace();
            }
        });

        streams.start();
        System.out.println("Kafka Streams started.......");
        return streams;

    }

    public static final String AVG_STORE_NAME = "in-memory-avg-store";
    public static final String NUM_RECORDS_STORE_NAME = "num-records-store";

    private TopologyBuilder processingTopologyBuilder() {

        StateStoreSupplier machineToAvgCPUUsageStore
                = Stores.create(AVG_STORE_NAME)
                        .withStringKeys()
                        .withDoubleValues()
                        .inMemory()
                        .build();

        StateStoreSupplier machineToNumOfRecordsReadStore
                = Stores.create(NUM_RECORDS_STORE_NAME)
                        .withStringKeys()
                        .withIntegerValues()
                        .inMemory()
                        .build();

        TopologyBuilder builder = new TopologyBuilder();

        builder.addSource(SOURCE_NAME, topicName)
                .addProcessor(PROCESSOR_NAME, new ProcessorSupplier() {
                    @Override
                    public Processor get() {
                        return new CpuMetricsStreamProcessor();
                    }
                }, SOURCE_NAME)
                .addStateStore(machineToAvgCPUUsageStore, PROCESSOR_NAME)
                .addStateStore(machineToNumOfRecordsReadStore, PROCESSOR_NAME);

        LOGGER.info("Kafka streams processing topology ready");

        return builder;
    }

}
