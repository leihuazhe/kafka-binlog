package com.today.data.transfer.canal;

import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.Header;

/**
 * 描述: canal 数据结构体 基于 Gson
 *
 * @author hz.lei
 * @date 2018年03月06日 下午9:07
 */
public class GsonEntry {

    private Header header;

    private RowChange rowChange;

    public GsonEntry(Header header, RowChange rowChange) {
        this.header = header;
        this.rowChange = rowChange;
    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public RowChange getRowChange() {
        return rowChange;
    }

    public void setRowChange(RowChange rowChange) {
        this.rowChange = rowChange;
    }
}
