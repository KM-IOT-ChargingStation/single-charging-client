package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.ForceUnLockRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.ForceUnLockResponseDto;
import com.kingmeter.single.charging.client.business.code.ClientFunctionCodeType;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ForceUnLockStrategy implements RequestStrategy {


    @Value("${kingmeter.default.timezone}")
    private int timezone;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();


        ForceUnLockResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), ForceUnLockResponseDto.class);

//        log.info("forceunlock {} , {}",siteId,requestDto.getKid());

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);

        long dockId = validateDockId(requestDto.getKid(), stateList.get(0).getKid());

        long bikeId = 0;
        if (dockId != 0) {
            for (int i = 0; i < stateList.size(); i++) {
                DockStateInfoFromHeartBeatVO vo = stateList.get(i);
                if (vo.getKid() == dockId) {
                    bikeId = vo.getBid();
                }
            }
        }

//        int dockCountForEverySite = 12;
//        long dockStartId = (siteId - 8000000000000l) == 1000000000001l ? 1000000000001l : (siteId - 8000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
//        long bikeStartId = (siteId - 7000000000000l) == 2000000000001l ? 2000000000001l : (siteId - 7000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
//
//        long bikeId = getBikeId(requestDto.getKid(), dockStartId);

        int gbs = 0;
        if (dockId == 0) {
            gbs = 33;
        } else {
            if (bikeId == 0) {
                gbs = 24;
            } else if (bikeId == stateList.get(11).getBid()) {
                gbs = 7;
            }
        }

        ForceUnLockRequestDto response =
                new ForceUnLockRequestDto(siteId,
                        dockId, bikeId, requestDto.getUid(),
                        gbs,
                        HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));


        if (gbs == 0) {
            DockStateInfoFromHeartBeatVO[] state = new DockStateInfoFromHeartBeatVO[stateList.size()];

            for (int i = 0; i < stateList.size(); i++) {
                DockStateInfoFromHeartBeatVO vo = stateList.get(i);

                if (vo.getKid() == dockId) {
                    vo.setBid(0);
                }
                state[i] = vo;
            }

            siteMap.put("state", JSON.toJSONString(state));
            CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);
        }


        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.ForceUnLock);
        responseBody.setData(JSONObject.toJSON(response).toString());
        ctx.writeAndFlush(responseBody);
    }

    private long validateDockId(long dockId, long dockStartId) {
        if (dockId >= dockStartId && dockId < dockStartId + 12) {
            return dockId;
        }
        return 0l;
    }

//    private long getBikeId(long dockId, long dockStartId) {
//        if (validateDockId(dockId, dockStartId) > 0) {
//            if ((dockId & 1) != 1) {
//                return dockId + 1000000000000l;
//            }
//        }
//        return 0l;
//    }
}
