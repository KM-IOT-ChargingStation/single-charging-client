package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v2.socket.out.SiteHeartResponseDto;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class HeartBeatStrategy implements RequestStrategy {

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        SiteHeartResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), SiteHeartResponseDto.class);

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

//        log.info(new KingMeterMarker("Socket,HeartBeat,C302"),
//                "{}|{}|{}|{}", siteId, requestDto.getSls(),
//                requestDto.getBnum(),requestDto.getTim());

    }
}
