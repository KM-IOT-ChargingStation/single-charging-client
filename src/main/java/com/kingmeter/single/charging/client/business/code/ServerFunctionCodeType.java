package com.kingmeter.single.charging.client.business.code;


import com.kingmeter.single.charging.client.business.strategy.*;

import java.util.HashMap;
import java.util.Map;

/**
 * socket server send these command
 */
public enum ServerFunctionCodeType {

    LoginType(0xc002, LoginStrategy.class),//
    ScanUnLock(0xc102, ScanUnLockStrategy.class),//
    ForceUnLock(0xc104, ForceUnLockStrategy.class),//
    BikeInDock(0xc202, BikeInDockStrategy.class),//
    SiteHeartBeat(0xc302,HeartBeatStrategy.class),//
    SwingCardUnLock(0xc402,SwingCardUnLockStrategy.class),//
    SwingCardConfirm(0xc404,SwingCardConfirmStrategy.class),//
    QueryDockInfo(0xc602, QueryDockInfoStrategy.class),//
    QueryDockLockStatus(0xc702, QueryDockLockStatusStrategy.class),//
    MalfunctionUpload(0xc902,MalfunctionUploadStrategy.class),//
    MalfunctionClear(0xCA02,MalfunctionClearStrategy.class),//
    QueryDockBikeInfo(0xCC02,QueryDockBikeInfoStrategy.class),//
    SiteSetting(0xCB02,SiteSettingStrategy.class),//
    OTAResponse(0xCE02,OTAResponseStrategy.class),//
    OTAResultUpload(0xCF02,OTAResultUploadStrategy.class);//

    private int value;
    private Class className;

    ServerFunctionCodeType(int value, Class className) {
        this.value = value;
        this.className = className;
    }

    public int value() {
        return value;
    }

    public Class getClassName (){
        return className;
    }

    static Map<Integer, ServerFunctionCodeType> enumMap = new HashMap();

    static {
        for (ServerFunctionCodeType type : ServerFunctionCodeType.values()) {
            enumMap.put(type.value(), type);
        }
    }

    public static ServerFunctionCodeType getEnum(Integer value) {
        return enumMap.get(value);
    }

    public static boolean containsValue(Integer value) {
        return enumMap.containsKey(value);
    }
}
