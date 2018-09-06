package com.today.data.transfer

import java.util.concurrent.Executors

import com.today.data.transfer.UTIL._
import com.today.data.transfer.canal.CanalClient
import com.today.data.transfer.kafka.BinlogKafkaProducer
import com.today.data.transfer.util.SysEnvUtil
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

/**
  *
  * 描述: binlogService server 启动端
  *
  * @author hz.lei
  * @since 2018年03月07日 上午1:08
  */
object BinLogServer {
  val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {
    startServer()
  }

  /**
    * 以Java 环境变量模式启动
    */
  def startServer(): Unit = {
    logger.info(s"启动服务 binlogServer...")

    val producerBrokerHost = SysEnvUtil.CANAL_KAFKA_HOST
    val topic = SysEnvUtil.CANAL_KAFKA_TOPIC

    val canalServerIp = SysEnvUtil.CANAL_SERVER_IP
    val canalServerPort = SysEnvUtil.CANAL_SERVER_PORT.toInt

    val destination = SysEnvUtil.CANAL_DESTINATION
    val username = SysEnvUtil.CANAL_USERNAME
    val password = SysEnvUtil.CANAL_PASSWORD

    val kafkaProducer = new BinlogKafkaProducer(producerBrokerHost, topic)
    kafkaProducer.init()


    val canalClient = new CanalClient(canalServerIp, canalServerPort, destination, username, password);
    canalClient.registerBinlogListener(kafkaProducer)

    val executorService = Executors.newFixedThreadPool(1)

    executorService.execute(canalClient)

    logger.info("启动服务 binlogService 成功...")


  }

  def startServerWithScala(): Unit = {
    logger.info(s"启动服务 binlogServer...")

    val config = ConfigFactory.load()

    val producerBrokerHost = config.getStringProxy("kafka.producerBrokerHost")
    val topic = config.getStringProxy("kafka.topic")

    val canalServerIp = config.getStringProxy("canal.canalServerIp")
    val canalServerPort = config.getStringProxy("canal.canalServerPort").toInt
    val destination = config.getStringProxy("canal.destination")
    val username = config.getStringProxy("canal.username")
    val password = config.getStringProxy("canal.password")

    val kafkaProducer = new BinlogKafkaProducer(producerBrokerHost, topic)
    kafkaProducer.init()


    val canalClient = new CanalClient(canalServerIp, canalServerPort, destination, username, password);
    canalClient.registerBinlogListener(kafkaProducer)

    val executorService = Executors.newFixedThreadPool(1)

    executorService.execute(canalClient)

    logger.info("启动服务 binlogService 成功...")
  }

}
