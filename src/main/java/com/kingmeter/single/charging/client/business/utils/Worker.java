package com.kingmeter.single.charging.client.business.utils;

import com.kingmeter.common.SpringContexts;
import com.kingmeter.single.charging.client.business.code.ServerFunctionCodeType;
import com.kingmeter.socket.framework.business.WorkerTemplate;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Worker extends WorkerTemplate {


    @Autowired
    private SpringContexts springContexts;


    @Override
    public RequestStrategy getRequestStrategy(int functionCode) {
        return (RequestStrategy)springContexts.getBean(ServerFunctionCodeType.getEnum(functionCode).getClassName());
    }


    @Override
    public void doDealWithOffline(SocketChannel channel, String deviceId) {
    }

}
