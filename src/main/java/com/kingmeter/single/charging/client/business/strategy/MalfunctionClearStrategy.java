package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.MalfunctionClearRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.DockMalfunctionClearResponseDto;
import com.kingmeter.single.charging.client.business.code.ClientFunctionCodeType;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MalfunctionClearStrategy implements RequestStrategy {

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        DockMalfunctionClearResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), DockMalfunctionClearResponseDto.class);


        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);
        long dockId = validateDockId(requestDto.getKid(), stateList.get(0).getKid());

//        int dockCountForEverySite = 12;
//        long dockStartId = (siteId - 8000000000000l) == 1000000000001l ? 1000000000001l : (siteId - 8000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
//
//        long dockId = validateDockId(requestDto.getKid(), dockStartId);
        int gbs = 0;
        if (dockId == stateList.get(11).getKid()) {
            gbs = 32;
        }
        if (requestDto.getErr() != 18) {
            gbs = 32;
        }
        if (dockId == 0) {
            gbs = 33;
        }


        MalfunctionClearRequestDto response =
                new MalfunctionClearRequestDto(siteId,
                        dockId, gbs);

        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.MalfunctionClear);
        responseBody.setData(JSONObject.toJSON(response).toString());
        ctx.writeAndFlush(responseBody);
    }

    private long validateDockId(long dockId, long dockStartId) {
        if (dockId >= dockStartId && dockId < dockStartId + 12) {
            return dockId;
        }
        return 0l;
    }
}
