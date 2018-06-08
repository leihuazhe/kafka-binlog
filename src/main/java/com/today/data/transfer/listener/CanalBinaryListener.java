package com.today.data.transfer.listener;

import com.alibaba.otter.canal.protocol.CanalEntry.Entry;

/**
 * 描述: 基于 二进制 的 canal 监听器
 *
 * @author hz.lei
 * @date 2018年03月06日 下午9:02
 */
public interface CanalBinaryListener {
    /**
     * canal 原生 二进制 模式 监听 binlog change
     *
     * @param entry
     */
    void onBinlog(Entry entry);

}
