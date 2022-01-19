package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.QueryDockBikeInfoRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateFromQueryDockBikeInfoVO;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.QueryDockBikeInfoResponseDto;
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
public class QueryDockBikeInfoStrategy implements RequestStrategy {

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        QueryDockBikeInfoResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), QueryDockBikeInfoResponseDto.class);


        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);

//        int dockCountForEverySite = 12;

        DockStateFromQueryDockBikeInfoVO[] state = new DockStateFromQueryDockBikeInfoVO[stateList.size()];

//        long dockStartId = (siteId - 8000000000000l)==1000000000001l?1000000000001l:(siteId - 8000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
//        long bikeStartId = (siteId - 7000000000000l)==2000000000001l?2000000000001l:(siteId - 7000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);

//        for (int i = 0; i < dockCountForEverySite; i++) {
//            DockStateFromQueryDockBikeInfoVO vo = new DockStateFromQueryDockBikeInfoVO();
//            vo.setKid(dockStartId);
//            vo.setKln(i + 1);
//            vo.setBid((i + 1) % 2 == 0 ? bikeStartId : 0);
//            state[i] = vo;
//            dockStartId++;
//            bikeStartId++;
//        }

        for (int i = 0; i < stateList.size(); i++) {
            DockStateInfoFromHeartBeatVO tmpInHeartBeat =
                    stateList.get(i);

            DockStateFromQueryDockBikeInfoVO vo = new DockStateFromQueryDockBikeInfoVO();
            vo.setKid(tmpInHeartBeat.getKid());
            vo.setKln(tmpInHeartBeat.getKln());
            vo.setBid(tmpInHeartBeat.getBid());
            state[i] = vo;
        }

        QueryDockBikeInfoRequestDto response =
                new QueryDockBikeInfoRequestDto(siteId,
                        state);

        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.QueryDockBikeInfo);
        responseBody.setData(JSONObject.toJSON(response).toString());
        ctx.writeAndFlush(responseBody);
    }
}
