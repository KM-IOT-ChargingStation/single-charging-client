package com.kingmeter;

import com.kingmeter.socket.framework.role.client.ClientAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SingleChargingClientApplication implements CommandLineRunner {

    @Value("${socket.host}")
    private String host;
    @Value("${socket.port}")
    private int port;
    @Value("${device.deviceCount}")
    private int deviceCount;
    @Value("${device.siteIdStart}")
    private long siteIdStart;

    @Autowired
    private ClientAdapter clientAdapter;

    public static void main(String[] args) {
        SpringApplication.run(SingleChargingClientApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        clientAdapter.bind(host,port,siteIdStart,deviceCount,"100419");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> clientAdapter.destroy()));
    }
}
