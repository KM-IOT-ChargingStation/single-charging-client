package com.kingmeter.single.charging.client.business.strategy;


import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v2.socket.in.SiteSettingRequestDto;
import com.kingmeter.dto.charging.v2.socket.out.SiteSettingResponseDto;
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
public class SiteSettingStrategy implements RequestStrategy {


    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        SiteSettingResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), SiteSettingResponseDto.class);

        log.info(new KingMeterMarker("Socket,SiteSetting,CB02"),
                "{}|{}|{}", siteId, requestDto.getHeart(), requestDto.getRptim());

        SiteSettingRequestDto response = new SiteSettingRequestDto();
        response.setSid(siteId);
        response.setSls(0);
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.SiteSetting);
        responseBody.setData(JSONObject.toJSON(response).toString());
        ctx.writeAndFlush(responseBody);

        log.info(new KingMeterMarker("Socket,SiteSetting,CB01"),
                "{}|{}",response.getSid(),response.getSls());

    }
}
