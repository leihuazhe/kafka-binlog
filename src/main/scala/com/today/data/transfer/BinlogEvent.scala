package com.today.data.transfer

import java.sql.Timestamp

import com.alibaba.otter.canal.protocol.CanalEntry.EventType

/**
  *
  * desc: BinlogEvent bean
  *
  * @author hz.lei
  * @date 2018年03月07日 下午3:43
  */
case class BinlogEvent(schema: String, tableName: String, eventType: EventType, timestamp: Timestamp, before: String, after: String)
