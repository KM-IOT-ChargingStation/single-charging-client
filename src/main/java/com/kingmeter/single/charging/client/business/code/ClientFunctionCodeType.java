package com.kingmeter.single.charging.client.business.code;

/**
 * wifimaster upload
 */
public interface ClientFunctionCodeType {

    byte[] LoginType = {(byte)192,(byte)1};//C0 01
    byte[] ScanUnLock = {(byte)193,(byte)1};//C1 01
    byte[] ForceUnLock = {(byte)193,(byte)3};//C1 03
    byte[] BikeInDock = {(byte)194,(byte)1};//C2 01
    byte[] SiteHeartBeat = {(byte)195,(byte)1};//C3 01
    byte[] SwingCardUnLock = {(byte)196,(byte)1};//C4 01
    byte[] SwingCardConfirm = {(byte)196,(byte)3};//C4 03
    byte[] QueryDockInfo = {(byte)198,(byte)1};//C6 01
    byte[] QueryDockLockStatus = {(byte)199,(byte)1};//C7 01
    byte[] MalfunctionUpload = {(byte)201,(byte)1};//C9 01
    byte[] MalfunctionClear = {(byte)202,(byte)1};//CA 01
    byte[] SiteSetting = {(byte)203,(byte)1};//CB 01
    byte[] QueryDockBikeInfo = {(byte)204,(byte)1};//CC 01
    byte[] DockDataUpload = {(byte)205,(byte)1};//CD 01
    byte[] OTAResponse = {(byte)206,(byte)1};//CE 01

}
