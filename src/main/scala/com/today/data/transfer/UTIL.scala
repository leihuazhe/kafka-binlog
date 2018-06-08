package com.today.data.transfer

import com.typesafe.config.{Config}
import org.slf4j.{LoggerFactory, Logger}

/**
  * Created by caiwb on 15-8-10.
  */
class UtilLogger

object UTIL {

  protected val logger: Logger = LoggerFactory.getLogger(classOf[UtilLogger])

  val ProductPublishMode: Boolean =
    System.getProperty("PUBLISH_MODE") match {
      case s: String => s == "PRODUCT"
      case _ => false
    }

  logger.info(s"[BinlogKafkaServer] ProductPublishMode: $ProductPublishMode")

  val env = System.getenv()

  implicit class ConfigInterpolation(c: Config) {
    def getStringProxy(path: String): String = {
      val pathRename = path.replace(".", "_")
      val value = env.get(pathRename)
      if (value != null && !value.isEmpty)
        return value

      if (ProductPublishMode) c.getString("product." + path) else c.getString("test." + path)
    }
  }

}
