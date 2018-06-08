package com.today.data.transfer.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.Message;
import com.today.data.transfer.listener.CanalBinaryListener;
import com.today.data.transfer.listener.CanalGsonListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 描述: Canal 客户端,监听处理 逻辑
 *
 * @author hz.lei
 * @date 2018年03月06日 下午8:21
 */
public class CanalClient implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CanalClient.class);

    private String hostname;
    private int port;
    private String destination;
    private String username;
    private String password;


    private CanalConnector connector;

    private final static int BatchSize = 1000;
    private final static long Sleep = 1000;
    private boolean runing = false;

    private List<CanalGsonListener> gsonListeners = new ArrayList<>();
    private List<CanalBinaryListener> binaryListeners = new ArrayList<>();


    /**
     * 构造函数
     *
     * @param hostname    canal服务端的ip
     * @param port        canal服务端 port
     * @param destination canal 实例地址
     * @param username    canal用户名
     * @param password    canal密码
     */
    public CanalClient(String hostname, int port, String destination, String username, String password) {
        this.hostname = hostname;
        this.port = port;
        this.destination = destination;
        this.username = username;
        this.password = password;
        init();
    }

    public void init() {
        try {
            logger.info(new StringBuffer("[Canal实例信息 CanalClient] [start] ")
                    .append("hostname: (").append(hostname)
                    .append("), port: (").append(port)
                    .append("), destination: (").append(destination)
                    .append("), username: (").append(username)
                    .append("), password: (").append(password).append(")").toString());

            connector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostname, port), destination, username, password);

            connector.connect();
            connector.subscribe(".*\\..*");
        } catch (Exception e) {
            logger.error("[CanalClient] [init] " + e.getMessage(), e);
        }
    }


    public void registerBinlogListener(CanalBinaryListener listener) {
        if (listener != null) {
            binaryListeners.add(listener);
        }
    }

    public void unregisterBinlogListener(CanalBinaryListener listener) {
        if (listener != null) {
            binaryListeners.remove(listener);
        }
    }

    @Override
    public void run() {

        logger.info("[CanalClient] [run] ");

        runing = true;

        work();
    }

    /**
     * 处理工作 work
     */
    private void work() {

        try {
            while (runing) {

                Message message = connector.getWithoutAck(BatchSize);

                long batchId = message.getId();
                int size = message.getEntries().size();

                if (batchId == -1 || size == 0) {
                    try {
                        Thread.sleep(Sleep);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }

                } else {
                    logger.debug("读取binlog日志 batchId: {}, size: {}, name: {}, offsets:{}", batchId, size,
                            message.getEntries().get(0).getHeader().getLogfileName(),
                            message.getEntries().get(0).getHeader().getLogfileOffset());
                    //处理消息
                    process(message.getEntries());
                }
                // 提交确认
                connector.ack(batchId);
            }

        } catch (Exception e) {
            connector.disconnect();
            logger.error("[CanalClient] [run] " + e.getMessage(), e);
        } finally {
            reconnect();
        }
    }

    /**
     * 重连策略
     */
    private void reconnect() {
        logger.info("[CanalClient reconnect] 重新连接 ...");

        runing = false;

        while (!runing) {
            try {
                connector = CanalConnectors.newSingleConnector(new InetSocketAddress(hostname, port), destination, username, password);
                connector.connect();
                connector.subscribe(".*\\..*");
                connector.rollback();

                runing = true;
            } catch (Exception e) {
                connector.disconnect();
                logger.error("[CanalClient] [reconnect] " + e.getMessage(), e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        }
        logger.info("[CanalClient reconnect] 重新连接成功！");
        work();
    }


    private void process(List<Entry> entries) {
        try {
            for (Entry entry : entries) {
                logger.debug("mysql binlog : " + entry.getHeader().getLogfileName() + "=>" + entry.getHeader().getLogfileOffset());
                /**
                 * 忽略 事务开启 、结束 ,query 的 binlog 内容
                 */
                if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND || entry.getHeader().getEventType() == EventType.QUERY) {
                    continue;
                }
                logger.info("解析偏移量:" + entry.getHeader().getLogfileName() + "=>" + entry.getHeader().getLogfileOffset() + " ," +
                        "操作表[" + entry.getHeader().getSchemaName() + "." + entry.getHeader().getTableName() + "]," +
                        "变更类型[" + entry.getHeader().getEventType() + "]," +
                        "执行时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(entry.getHeader().getExecuteTime())));


                RowChange rowChange;
                try {
                    rowChange = RowChange.parseFrom(entry.getStoreValue());
                } catch (Exception e) {
                    logger.error("[CanalClient] [process] 解析RowChange事件错误: " + e.getMessage(), entry.toString());
                    continue;
                }

                log(entry.getHeader(), rowChange);

                if (gsonListeners.size() > 0) {
                    GsonEntry binlog = new GsonEntry(entry.getHeader(), rowChange);

                    for (CanalGsonListener listener : gsonListeners) {
                        listener.onBinlog(binlog);
                    }
                }

                if (binaryListeners.size() > 0) {
                    for (CanalBinaryListener listener : binaryListeners) {
                        listener.onBinlog(entry);
                    }
                }

            }
        } catch (Exception e) {
            logger.error("[CanalClient] [process] " + e.getMessage(), e);
        }

    }

    private void log(Header header, RowChange rowChange) {
        EventType eventType = rowChange.getEventType();

        logger.debug(String.format("binlog[%s:%s], name[%s,%s], eventType : %s",
                header.getLogfileName(), header.getLogfileOffset(),
                header.getSchemaName(), header.getTableName(),
                eventType));

        for (RowData rowData : rowChange.getRowDatasList()) {
            if (eventType == EventType.DELETE) {
                log(rowData.getBeforeColumnsList());
            } else if (eventType == EventType.INSERT) {
                log(rowData.getAfterColumnsList());
            } else {
                log(rowData.getBeforeColumnsList());
                log(rowData.getAfterColumnsList());
            }
        }
    }

    private void log(List<Column> columns) {
        for (Column column : columns) {
            logger.debug(new StringBuffer()
                    .append(column.getName()).append(" = ").append(column.getValue())
                    .append(" update[").append(column.getUpdated()).append("]").toString());
        }
    }
}
