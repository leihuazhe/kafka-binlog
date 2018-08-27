package com.today.data.transfer.kafka;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.today.data.transfer.listener.CanalBinaryListener;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.Properties;

/**
 * 描述: 将收到的cannal 消息 发送到kafka
 *
 * @author hz.lei
 * @date 2018年03月07日 上午12:44
 */
public class BinlogKafkaProducer implements CanalBinaryListener {
    private static Logger logger = LoggerFactory.getLogger(BinlogKafkaProducer.class);
    private String topic;
    private String host;

    protected Producer<Integer, byte[]> producer;

    public BinlogKafkaProducer(String kafkaHost, String topic) {
        this.topic = topic;
        this.host = kafkaHost;
    }

    public void init() {
        logger.info("[KafkaStringProducer] [init] " +
                ") broker-list(" + host + " )");

        Properties properties = KafkaConfigBuilder.defaultProducer().bootstrapServers(host)
                .withKeySerializer(IntegerSerializer.class)
                .withValueSerializer(ByteArraySerializer.class)
                .build();

        producer = new KafkaProducer<>(properties);
    }

    /**
     * 异步回调模式发送消息
     *
     * @param topic
     * @param message
     */
    public void send(String topic, byte[] message) {
        producer.send(new ProducerRecord<>(topic, message), (metadata, e) -> {
            if (e != null) {
                logger.error("[" + getClass().getSimpleName() + "]: 消息发送失败,cause: " + e.getMessage(), e);
            }
            logger.info("[binlog]:消息发送成功,topic:{}, offset:{}, partition:{}, time:{}",
                    metadata.topic(), metadata.offset(), metadata.partition(), metadata.timestamp());

        });
    }


    @Override
    public void onBinlog(CanalEntry.Entry entry) {
        send(topic, entry.toByteArray());
    }
}
