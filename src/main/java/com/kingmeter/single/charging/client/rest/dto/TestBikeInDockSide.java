package com.kingmeter.single.charging.client.rest.dto;

import lombok.Data;

@Data
public class TestBikeInDockSide {
    private long siteId;
    private long dockId;
    private long lockId;
    private String lsv;
    private String lhv;
    private int lchg;
    private int lsoc;
    private int lsta;
    private int lerr;
    private long tim;
}
