package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.QueryDockInfoRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromQueryDockInfoVO;
import com.kingmeter.dto.charging.v2.socket.out.QueryDockInfoResponseDto;
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
public class QueryDockInfoStrategy implements RequestStrategy {

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody, ChannelHandlerContext ctx) {

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        QueryDockInfoResponseDto requestDto =
                JSONObject.
                        parseObject(requestBody.getData(), QueryDockInfoResponseDto.class);


        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);


        int dockCountForEverySite = stateList.size();

//        int dockCountForEverySite = 12;
//        long dockStartId = (siteId - 8000000000000l) == 1000000000001l ? 1000000000001l : (siteId - 8000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);

        DockStateInfoFromQueryDockInfoVO[] state = new DockStateInfoFromQueryDockInfoVO[requestDto.getKid() == 0 ? dockCountForEverySite : 1];
//                {new DockStateInfoFromQueryDockInfoVO(1,dockId,
//                        "","","","","","")};

        if (requestDto.getKid() == 0) {
            for (int i = 0; i < dockCountForEverySite; i++) {
                DockStateInfoFromHeartBeatVO voInHeart = stateList.get(i);

                DockStateInfoFromQueryDockInfoVO vo = new DockStateInfoFromQueryDockInfoVO();
                vo.setKid(voInHeart.getKid());
                vo.setKln(voInHeart.getKln());
                vo.setPhv("");
                vo.setPsv("");
                vo.setDsv("");
                vo.setDhv("");
                vo.setEsv("");
                vo.setEhv("");
                vo.setSls(0);
                state[i] = vo;
//                dockStartId++;
            }
        } else {
//            long dockId = validateDockId(requestDto.getKid(), dockStartId);
            long dockId = validateDockId(requestDto.getKid(), stateList.get(0).getKid());
            int dockKln = 0;
            int gbs = 0;
            if (dockId == 0) {
                gbs = 33;
            } else {
                for (int i = 0; i < stateList.size(); i++) {
                    DockStateInfoFromHeartBeatVO vo = stateList.get(i);
                    if (vo.getKid() == dockId) {
                        dockKln = vo.getKln();
                    }
                }
            }

            state[0] = new DockStateInfoFromQueryDockInfoVO(
                    dockKln, dockId,
                    "", "", "", "", "", "", gbs);
        }

        QueryDockInfoRequestDto response =
                new QueryDockInfoRequestDto(siteId,
                        "", "",
                        "", "",
                        state);

        responseBody.setTokenArray(requestBody.getTokenArray());
        responseBody.setFunctionCodeArray(ClientFunctionCodeType.QueryDockInfo);
        responseBody.setData(JSONObject.toJSON(response).toString());
        ctx.writeAndFlush(responseBody);
    }

    private long validateDockId(long dockId, long dockStartId) {
        if (dockId >= dockStartId && dockId < dockStartId + 12) {
            return dockId;
        }
        return 0l;
    }

//    private int getKlnByDockId(long dockId, long dockStartId) {
//        if (dockId >= dockStartId && dockId < dockStartId + 12) {
//            return Long.valueOf(dockId - dockStartId + 1).intValue();
//        }
//        return 0;
//    }

}
