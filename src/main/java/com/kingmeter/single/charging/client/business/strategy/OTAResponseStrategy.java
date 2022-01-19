package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.OTARequestDto;
import com.kingmeter.dto.charging.v2.socket.out.OTAResponseDto;
import com.kingmeter.single.charging.client.business.code.ClientFunctionCodeType;
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
public class OTAResponseStrategy implements RequestStrategy {

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        OTAResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), OTAResponseDto.class);

        OTARequestDto response =
                new OTARequestDto(siteId,
                        requestDto.getParts(), 1, 0);

        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.OTAResponse);
        responseBody.setData(JSONObject.toJSON(response).toString());
        ctx.writeAndFlush(responseBody);


    }
}
