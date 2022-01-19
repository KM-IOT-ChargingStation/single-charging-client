package com.kingmeter.single.charging.client.rest.dto;


import lombok.Data;

@Data
public class TestMalfunctionDto {
    private long siteId;
    private int wfm;
    private long dockId;
    private int dockKln;
    private long vehicleId;
    private int cer;//
    private int ber;//
    private int disp;//
    private int per;//
    private int perlk;//
    private int perws;//
}
