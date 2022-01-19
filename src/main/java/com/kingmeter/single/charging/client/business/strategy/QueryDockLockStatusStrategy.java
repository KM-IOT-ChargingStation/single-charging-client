package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.QueryDockLockStatusRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.QueryDockLockStatusResponseDto;
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
public class QueryDockLockStatusStrategy implements RequestStrategy {
    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {
        QueryDockLockStatusResponseDto responseDto =
                JSONObject.
                        parseObject(requestBody.getData(), QueryDockLockStatusResponseDto.class);

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        QueryDockLockStatusRequestDto requestDto =
                new QueryDockLockStatusRequestDto();

        requestDto.setSid(siteId);
        requestDto.setKid(responseDto.getKid());
        requestDto.setLks(0);
        requestDto.setUid(responseDto.getUid());

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);
        long dockId = validateDockId(requestDto.getKid(), stateList.get(0).getKid());
        if(dockId==0){
            requestDto.setLks(33);
        }else{
            for (int i = 0; i < stateList.size(); i++) {
                DockStateInfoFromHeartBeatVO vo = stateList.get(i);
                if (vo.getKid() == dockId) {
                    requestDto.setBid(vo.getBid());
                }
            }
        }


        responseBody.setData(JSONObject.toJSON(requestDto).toString());
        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.QueryDockLockStatus);
        ctx.writeAndFlush(responseBody);
    }

    private long validateDockId(long dockId, long dockStartId) {
        if (dockId >= dockStartId && dockId < dockStartId + 12) {
            return dockId;
        }
        return 0l;
    }
}
