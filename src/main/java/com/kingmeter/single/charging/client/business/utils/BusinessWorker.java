package com.kingmeter.single.charging.client.business.utils;


import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.socket.in.SiteHeartRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.SiteLoginRequestDto;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.single.charging.client.business.code.ClientFunctionCodeType;
import com.kingmeter.socket.framework.application.SocketApplication;
import com.kingmeter.socket.framework.role.client.BusinessWorkerTemplate;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.MD5Util;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Slf4j
@Component()
public class BusinessWorker extends BusinessWorkerTemplate {

    @Autowired
    private SocketApplication socketApplication;


    public void login(ChannelHandlerContext ctx, Long siteId, String password) {
//        String token = "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        byte[] tokenArray = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        //100419
//        byte[] passwordArray = {49,48,48,52,49,57};
        byte[] passwordArray = password.getBytes();
        String passwordMd5 = MD5Util.MD5Encode(passwordArray);

        SiteLoginRequestDto requestDto =
                new SiteLoginRequestDto();
        requestDto.setSid(siteId);
        requestDto.setPwd(passwordMd5);
        requestDto.setMhv("");
        requestDto.setMsv("");

        socketApplication.sendSocketMsg(siteId,tokenArray, (SocketChannel) ctx.channel(),
                ClientFunctionCodeType.LoginType,
                JSONObject.toJSON(requestDto).toString());
    }


    public void sendHeartBeat(byte[] tokenArray, SocketChannel channel, long siteId) {
        SiteHeartRequestDto requestDto = new SiteHeartRequestDto();
        requestDto.setSid(siteId);

//        int dockCountForEverySite = 12;
//
//        DockStateInfoFromHeartBeatVO[] state = new DockStateInfoFromHeartBeatVO[dockCountForEverySite];
//
//        long dockStartId = (siteId - 8000000000000l)==1000000000001l?1000000000001l:(siteId - 8000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
//        long bikeStartId = (siteId - 7000000000000l)==2000000000001l?2000000000001l:(siteId - 7000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
//
//        for (int i = 0; i < dockCountForEverySite; i++) {
//            DockStateInfoFromHeartBeatVO vo = new DockStateInfoFromHeartBeatVO();
//            vo.setKid(dockStartId);
//            vo.setKln(i + 1);
//            vo.setBid((i + 1) % 2 == 0 ? bikeStartId : 0);
//            vo.setBsoc(0);
//            state[i] = vo;
//            dockStartId++;
//            bikeStartId++;
//        }

        Map<String, String> siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);

        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);

        DockStateInfoFromHeartBeatVO[] state = new DockStateInfoFromHeartBeatVO[stateList.size()];

        for (int i = 0; i < stateList.size(); i++) {
            state[i] = stateList.get(i);
        }
        
        requestDto.setState(state);

        socketApplication.sendSocketMsg(siteId,tokenArray, channel,
                ClientFunctionCodeType.SiteHeartBeat,
                JSONObject.toJSON(requestDto));

        log.info("state:{}",JSONObject.toJSONString(state));

//        log.info(new KingMeterMarker("Socket,HeartBeat,C301"),
//                "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", siteId, requestDto.getSst(), requestDto.getScf(),
//                Float.valueOf(requestDto.getSur()) / 100,
//                Float.valueOf(requestDto.getSvl()) / 100,
//                Float.valueOf(requestDto.getItp()) / 10,
//                Float.valueOf(requestDto.getEtp()) / 10,
//                requestDto.getStu(),
//                calculatePower(Float.valueOf(requestDto.getSvl()) / 100),
//                requestDto.getState());
    }

    private int calculatePower(float svl) {
        if (svl <= 31.5) {
            return 0;
        } else if (svl >= 41.5) {
            return 100;
        } else {
            Double rate = (svl - 31.5) / (42 - 31.5) * 100;
            return rate.intValue();
        }
    }
}
