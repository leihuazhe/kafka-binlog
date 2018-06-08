package com.today.data.transfer.util;

/**
 * 描述: com.today.data.transfer.util
 *
 * @author hz.lei
 * @date 2018年05月07日 下午7:38
 */
public class SysEnvUtil {

    private static final String KEY_CANAL_DESTINATION = "canal.destination";

    private static final String KEY_CANAL_SERVER_IP = "canal.canalServerIp";

    private static final String KEY_CANAL_SERVER_PORT = "canal.canalServerPort";

    private static final String KEY_CANAL_KAFKA_TOPIC = "kafka.topic";

    private static final String KEY_CANAL_USERNAME = "canal.username";
    private static final String KEY_CANAL_PASSWORD = "canal.password";
    private static final String KEY_CANAL_KAFKA_HOST = "canal.kafka.host";


    public static final String CANAL_DESTINATION = get(KEY_CANAL_DESTINATION, "today");

    public static final String CANAL_SERVER_IP = get(KEY_CANAL_SERVER_IP, "127.0.0.1");

    public static final String CANAL_SERVER_PORT = get(KEY_CANAL_SERVER_PORT, "11111");

    public static final String CANAL_KAFKA_TOPIC = get(KEY_CANAL_KAFKA_TOPIC, "Binlog");

    public static final String CANAL_USERNAME = get(KEY_CANAL_USERNAME, "");
    public static final String CANAL_PASSWORD = get(KEY_CANAL_PASSWORD, "");
    public static final String CANAL_KAFKA_HOST = get(KEY_CANAL_KAFKA_HOST, "kafka-host:9092");


    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));

        if (envValue == null) {
            return System.getProperty(key, defaultValue);
        }

        return envValue;
    }


}
