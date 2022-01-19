package com.kingmeter.single.charging.client.business.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingmeter.common.KingMeterMarker;
import com.kingmeter.dto.charging.v2.socket.in.vo.DockStateInfoFromHeartBeatVO;
import com.kingmeter.dto.charging.v2.socket.out.LoginResponseDto;
import com.kingmeter.single.charging.client.business.utils.BusinessWorker;
import com.kingmeter.socket.framework.dto.RequestBody;
import com.kingmeter.socket.framework.dto.ResponseBody;
import com.kingmeter.socket.framework.role.client.ClientAdapter;
import com.kingmeter.socket.framework.strategy.RequestStrategy;
import com.kingmeter.socket.framework.util.CacheUtil;
import com.kingmeter.utils.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class LoginStrategy implements RequestStrategy {


    @Autowired
    private BusinessWorker businessWorker;

    @Autowired
    private ClientAdapter clientAdapter;

    @Value("${kingmeter.default.timezone}")
    private int timezone;

    @Value("${device.heartbeat.internal}")
    private int internal;

    @Override
    public void process(RequestBody requestBody, ResponseBody responseBody,
                        ChannelHandlerContext ctx) {

        SocketChannel channel = (SocketChannel) ctx.channel();

        AttributeKey<Long> nameAttrKey = AttributeKey.valueOf("siteId");
        Attribute<Long> attr = ctx.channel().attr(nameAttrKey);
        Long siteId = attr.get();

        //password
        AttributeKey<String> passwordKey = AttributeKey.valueOf("password");
        Attribute<String> passwordAttr = channel.attr(passwordKey);
        String password = passwordAttr.get();

        CacheUtil.getInstance().getDeviceResultMap().remove(siteId + "_reLogin");

        LoginResponseDto loginParamsDto = JSONObject.
                parseObject(requestBody.getData(), LoginResponseDto.class);

        log.info(new KingMeterMarker("Socket,Login,C002"),
                "{}|{}|{}|{}|{}|{}|{}|{}|{}|{}", siteId,
                loginParamsDto.getSls(), loginParamsDto.getPwd(),
                loginParamsDto.getUrl(), loginParamsDto.getPot(),
                loginParamsDto.getKnum(), loginParamsDto.getBnum(),
                loginParamsDto.getCid(), loginParamsDto.getTim(), "");

        if (StringUtil.isNotEmpty(loginParamsDto.getUrl())) {
            //clientAdapter
//            AttributeKey<EventExecutorGroup> executorGroupAttributeKey =
//                    AttributeKey.valueOf("businessGroup");
//            Attribute<EventExecutorGroup> groupAttr = channel.attr(executorGroupAttributeKey);
//            EventExecutorGroup businessGroup = groupAttr.get();
//            businessGroup.shutdownGracefully();

            CacheUtil.getInstance().getDeviceResultMap().put(siteId + "_reLogin", new HashMap<>());
            ctx.close();

            clientAdapter.bind(loginParamsDto.getUrl(),
                    loginParamsDto.getPot(), siteId, 1, password);
            businessWorker.login(ctx, siteId, password);

            return;
        }

        //2,没有新地址
        String token = requestBody.getToken();
        byte[] tokenArray = requestBody.getTokenArray();

        CacheUtil.getInstance().dealWithLoginSucceed(String.valueOf(siteId), token, tokenArray,
                (SocketChannel) ctx.channel());

        dealWithLoginInRedis(token, siteId,timezone);

        businessWorker.doJob(tokenArray, ctx, siteId,internal);
    }


    private void dealWithLoginInRedis(String token, long siteId,int timezone) {
        Map<String, String> siteMap = new HashMap<>();

        if (CacheUtil.getInstance().getDeviceInfoMap().containsKey(siteId)) {
            siteMap = CacheUtil.getInstance().getDeviceInfoMap().get(siteId);
        } else {
            DateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
            siteMap.put("loginTime", sdf.format(new Date()));
        }

        siteMap.put("token", token);
        Date now = new Date();
        DateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        siteMap.put("loginTime", sdf.format(now));
        siteMap.put("timezone", String.valueOf(timezone));


        int dockCountForEverySite = 12;

        DockStateInfoFromHeartBeatVO[] state = new DockStateInfoFromHeartBeatVO[dockCountForEverySite];

        long dockStartId = (siteId - 8000000000000l) == 1000000000001l ? 1000000000001l : (siteId - 8000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);
        long bikeStartId = (siteId - 7000000000000l) == 2000000000001l ? 2000000000001l : (siteId - 7000000000001l) + dockCountForEverySite * (siteId - 9000000000001l);

        for (int i = 0; i < dockCountForEverySite; i++) {
            DockStateInfoFromHeartBeatVO vo = new DockStateInfoFromHeartBeatVO();
            vo.setKid(dockStartId);
            vo.setKln(i + 1);
            vo.setBid((i + 1) % 2 == 0 ? bikeStartId : 0);
            vo.setBsoc(0);
            state[i] = vo;
            dockStartId++;
            bikeStartId++;
        }

        siteMap.put("state", JSON.toJSONString(state));

        CacheUtil.getInstance().getDeviceInfoMap().put(siteId, siteMap);
    }

}
