package com.kingmeter.single.charging.client.rest.dto;


import lombok.Data;

@Data
public class TestOtaResultUpload {
    private long siteId;
    private int parts;
    private long dockId;
    private int pro;
    private int sls;
}
