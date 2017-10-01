package com.oracle.cloud.accs.ehcs.producer;

import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Runs in a dedicated thread. Contains core logic for producing event.
 */
public class Producer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Producer.class.getName());
    //private static final String TOPIC_NAME = "cpu-metrics-topic";
    private KafkaProducer<String, String> kafkaProducer = null;
    //private final String KAFKA_CLUSTER_ENV_VAR_NAME = "KAFKA_CLUSTER";
    private final String topicName;

    public Producer() {
        LOGGER.log(Level.INFO, "Kafka Producer running in thread {0}", Thread.currentThread().getName());

        this.topicName = System.getenv().get("OEHCS_TOPIC");
        LOGGER.log(Level.INFO, "Kafka topic {0}", topicName);

        Properties kafkaProps = new Properties();

        String kafkaCluster = System.getenv().get("OEHCS_EXTERNAL_CONNECT_STRING");
        LOGGER.log(Level.INFO, "Kafka cluster {0}", kafkaCluster);

        kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaCluster);
        kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put(ProducerConfig.ACKS_CONFIG, "0");

        this.kafkaProducer = new KafkaProducer<>(kafkaProps);

    }

    @Override
    public void run() {
        try {
            produce();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
    
    /**
     * produce messages
     *
     * @throws Exception
     */
    private void produce() throws Exception {
        ProducerRecord<String, String> record = null;

        try {
            Random rnd = new Random();
            while (true) {

                for (int i = 1; i <= 10; i++) {
                    String key = "machine-" + i;
                    //String value = rnd.nextBoolean() ? String.valueOf(rnd.nextInt(85) + 5) : "98";
                    String value = null;
                    int max = 99;
                    int min = 1;
                    if(key.equals("machine-3") || key.equals("machine-5") || key.equals("machine-8")
                                || key.equals("machine-9")){
                        min = 10;
                        max = 25; //b/w 10 (min) to 25 (max)
                    }else if(key.equals("machine-7") || key.equals("machine-1") || key.equals("machine-4")){
                        min = 76; // b/w 76 (min) to 99 (max)
                    }
                    //value = String.valueOf(rnd.nextInt(min) + 5);
                    value = String.valueOf(rnd.nextInt((max - min) + 1) + min);
                    record = new ProducerRecord<>(topicName, key, value);

                    kafkaProducer.send(record, new Callback() {
                        @Override
                        public void onCompletion(RecordMetadata rm, Exception excptn) {
                            if (excptn != null) {
                                LOGGER.log(Level.WARNING, "Error sending message with key {0}\n{1}", new Object[]{key, excptn.getMessage()});
                            } else {
                                //LOGGER.log(Level.INFO, "Partition for current record {2}", new Object[]{rm.partition()});
                            }

                        }
                    });

                }

                /**
                 * wait before sending next message. this has been done on
                 * purpose
                 */
                Thread.sleep(200);

            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Producer thread was interrupted");
        } finally {
            kafkaProducer.close();

            LOGGER.log(Level.INFO, "Producer closed");
        }

    }


}
