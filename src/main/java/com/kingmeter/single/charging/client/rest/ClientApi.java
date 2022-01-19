package com.kingmeter.single.charging.client.rest;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.dto.charging.v2.rest.request.SwingCardUnLockRequestRestDto;
import com.kingmeter.dto.charging.v2.socket.in.*;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromDockDataUploadVO;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.SiteHeartResponseDto;
import com.kingmeter.single.charging.client.business.code.ClientFunctionCodeType;
import com.kingmeter.single.charging.client.rest.dto.*;
import com.kingmeter.socket.framework.application.SocketApplication;
import com.kingmeter.socket.framework.role.client.ClientAdapter;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.HardWareUtils;
import com.kingmeter.utils.StringUtil;
import io.netty.channel.socket.SocketChannel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RequestMapping("/client")
@RestController
public class ClientApi {

    @Autowired
    private ClientAdapter clientAdapter;

    @Autowired
    private SocketApplication socketApplication;

    @Value("${kingmeter.default.timezone}")
    private int defaultTimezone;

    @PostMapping("/testLogin")
    public String testLogin(@RequestBody TestLoginDto loginDto){
        clientAdapter.bind(loginDto.getHost(),loginDto.getPort(),
                loginDto.getSiteIdStart(),loginDto.getDeviceCount(),
                loginDto.getPassword());
        return "ok";
    }

    @DeleteMapping("/disconnect")
    public String disconnect(@RequestBody TestLogoutDto logoutDto){
        long siteId = logoutDto.getSiteIdStart();
        ConcurrentMap<String, SocketChannel> map = CacheUtil.getInstance().getDeviceIdAndChannelMap();
        for (int i = 0; i < logoutDto.getDeviceCount(); i++) {
            SocketChannel channel = socketApplication.getChannelByDeviceId(String.valueOf(siteId));
            siteId++;
            try{
                CacheUtil.getInstance().dealWithOffLine(channel,String.valueOf(siteId));
                channel.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return "ok";
    }

    @PostMapping("/bikeInDock")
    public String bikeInDock(@RequestBody TestBikeInDock bikeInDock){

        long siteId = bikeInDock.getSiteId();

        Map<String, String> siteMap = CacheUtil.getInstance()
                .getDeviceInfoMap()
                .getOrDefault(siteId, new ConcurrentHashMap<>());

        int timezone = Integer.parseInt(siteMap.getOrDefault("timezone", String.valueOf(defaultTimezone)));

        BikeInDockRequestDto requestDto =
                new BikeInDockRequestDto();
        requestDto.setSid(bikeInDock.getSiteId());
        requestDto.setKid(bikeInDock.getDockId());
        requestDto.setBid(bikeInDock.getVehicleId());
        requestDto.setTim(HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

        socketApplication.sendSocketMsg(siteId,
                ClientFunctionCodeType.BikeInDock,
                JSONObject.toJSON(requestDto));


        List<DockStateInfoFromHeartBeatVO> stateList = JSONObject.parseArray(
                siteMap.get("state")
                , DockStateInfoFromHeartBeatVO.class);

        DockStateInfoFromHeartBeatVO[] state = new DockStateInfoFromHeartBeatVO[stateList.size()];

        for (int i = 0; i < stateList.size(); i++) {
            DockStateInfoFromHeartBeatVO vo = stateList.get(i);

            if (vo.getKid() == bikeInDock.getDockId()) {
                vo.setBid(bikeInDock.getVehicleId());
            }
            state[i] = vo;
        }
        siteMap.put("state", JSON.toJSONString(state));
        CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);

        return "ok";
    }



    @PostMapping("/swingCardUpload")
    public String swingCardUpload(@RequestBody SwingCardUnLockRequestRestDto requestDto){

        Map<String, String> siteMap = CacheUtil.getInstance()
                .getDeviceInfoMap()
                .getOrDefault(requestDto.getSiteId(), new ConcurrentHashMap<>());

        int timezone = Integer.parseInt(siteMap.getOrDefault("timezone", String.valueOf(defaultTimezone)));

        SwingCardUnLockRequestDto restDto = new SwingCardUnLockRequestDto(
                requestDto.getSiteId(),requestDto.getDockId(),
                requestDto.getVehicleId(),requestDto.getCard(),
                HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone)
        );

        socketApplication.sendSocketMsg(restDto.getSid(),
                ClientFunctionCodeType.SwingCardUnLock,
                JSONObject.toJSON(restDto));

        return "ok";
    }

    @PostMapping("/swingCardConfirm")
    public String swingCardConfirm(@RequestBody SwingCardUnLockRequestConfirmDto requestDto){

        Map<String, String> siteMap = CacheUtil.getInstance()
                .getDeviceInfoMap()
                .getOrDefault(requestDto.getSid(), new ConcurrentHashMap<>());

        int timezone = Integer.parseInt(siteMap.getOrDefault("timezone", String.valueOf(defaultTimezone)));

        requestDto.setTim(HardWareUtils.getInstance().getUtcTimeStampOnDevice(timezone));

        socketApplication.sendSocketMsg(requestDto.getSid(),
                ClientFunctionCodeType.SwingCardConfirm,
                JSONObject.toJSON(requestDto));

        return "ok";
    }


    @PostMapping("/malfunctionUpload")
    public String malfunctionUpload(@RequestBody TestMalfunctionDto dto){

        DockMalfunctionUploadRequestDto requestDto =
                new DockMalfunctionUploadRequestDto();

        BeanUtils.copyProperties(dto,requestDto);

        requestDto.setSid(dto.getSiteId());
        requestDto.setKln(dto.getDockKln());
        requestDto.setKid(dto.getDockId());
        requestDto.setBid(dto.getVehicleId());
        requestDto.setCer(dto.getCer());
        requestDto.setBer(dto.getBer());
        requestDto.setPer(dto.getPer());
        requestDto.setDisp(dto.getDisp());
        requestDto.setPerws(dto.getPerws());
        requestDto.setPerlk(dto.getPerlk());

        socketApplication.sendSocketMsg(dto.getSiteId(),
                ClientFunctionCodeType.MalfunctionUpload,
                JSONObject.toJSON(requestDto));

        return "ok";
    }


    @PostMapping("/dockDataUpload")
    public String dockDataUpload(long siteId,long dockId){
        DockDataUploadRequestDto requestDto =
                new DockDataUploadRequestDto();
        requestDto.setSid(siteId);

        DockStateInfoFromDockDataUploadVO[] stateList =
                new DockStateInfoFromDockDataUploadVO[1];

        DockStateInfoFromDockDataUploadVO state =
                new DockStateInfoFromDockDataUploadVO();
        state.setKid(dockId);
        state.setKln(1);
        state.setBid(0);
        state.setBrang(0);
        state.setBsoc(0);
        state.setChgi(0);
        state.setChgv(0);
        state.setIpow(0);
        state.setItemp(0);

        stateList[0] = state;

        requestDto.setState(stateList);

        socketApplication.sendSocketMsg(siteId,
                ClientFunctionCodeType.DockDataUpload,
                JSONObject.toJSON(requestDto));

        return "ok";
    }

}
