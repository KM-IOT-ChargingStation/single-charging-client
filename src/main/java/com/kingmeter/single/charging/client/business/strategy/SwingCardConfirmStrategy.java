package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.out.SwingCardUnLockConfirmResponseDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SwingCardConfirmStrategy implements RequestStrategy {
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext channelHandlerContext) {
        SwingCardUnLockConfirmResponseDto responseDto =
                JSONObject.
                        parseObject(requestBody.getData(), SwingCardUnLockConfirmResponseDto.class);

        responseDto.getKid();
    }
}
