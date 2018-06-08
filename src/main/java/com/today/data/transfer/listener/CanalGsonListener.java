package com.today.data.transfer.listener;

import com.today.data.transfer.canal.GsonEntry;

/**
 * 描述:基于 Gson 的 canal 监听器
 *
 * @author hz.lei
 * @date 2018年03月06日 下午9:01
 */
public interface CanalGsonListener {

    /**
     * 基于 Gson  监听 binlog change
     *
     * @param entry
     */
    void onBinlog(GsonEntry entry);


}
