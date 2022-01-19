package com.kingmeter.single.charging.client.rest.dto;


import lombok.Data;

@Data
public class TestLoginDto {

    private String host;
    private int port;

    private long siteIdStart;
    private int deviceCount;

    private String password;
}
