package net.manager;


import net.interfaces.OnDeviceFreeStateCallBack;
import net.interfaces.OnMsgActionListener;
import net.socket.DeviceInfo;
import net.socket.SocketConnect;
import net.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by taro on 16/3/8.
 */
public class MessageParseManager implements IMsgParseMgrAction {
    private OnMsgActionListener mMsgActionListener = null;
    private Map<String, OnDeviceFreeStateCallBack> mDeviceFreeStateCallbackMap = null;

    public MessageParseManager() {
        this(null);
    }

    /**
     * 设备状态改变时监听回调接口
     *
     * @param msgListener 消息接收发送处理接口,用于存储数据等
     */
    public MessageParseManager(OnMsgActionListener msgListener) {
        mMsgActionListener = msgListener;
        mDeviceFreeStateCallbackMap = new HashMap<String, OnDeviceFreeStateCallBack>();
    }

    @Override
    public void onParseReceivedMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg) {
        boolean isSaveMsg = false;
        try {
            CommonUtils.logInfo("接收到的消息:" + msg);
            if (socketWorkType == DeviceInfo.SOCKET_IN_SERVER) {
                // 处理来自服务端的消息
                isSaveMsg = handleMsgReceiveFromClient(connector, deviceToken, msg);
            } else if (socketWorkType == DeviceInfo.SOCKET_IN_CLIENT) {
                // 处理来自客户端的消息
                isSaveMsg = handleMsgReceiveFromServer(connector, deviceToken, msg);
            }
        } catch (Exception e) {
            // 可能会解析出错
            CommonUtils.logError(e);
        }
        if (isSaveMsg && mMsgActionListener != null) {
            mMsgActionListener.onReceiveMsg(connector, deviceToken, socketWorkType, msg);
        }
    }

    @Override
    public void onParseSentMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg) {
        boolean isSaveMsg = false;
        try {
            CommonUtils.logInfo("发送的消息:" + msg);
            if (socketWorkType == DeviceInfo.SOCKET_IN_SERVER) {
                isSaveMsg = handleMsgSendFromServer(deviceToken, connector, msg);
            } else if (socketWorkType == DeviceInfo.SOCKET_IN_CLIENT) {
                isSaveMsg = handleMsgSendFromClient(deviceToken, connector, msg);
            }
        } catch (Exception e) {
            // 可能会解析出错
            CommonUtils.logError(e);
        }
        if (isSaveMsg && mMsgActionListener != null) {
            mMsgActionListener.onSendMsg(connector, deviceToken, socketWorkType, msg);
        }
    }

    @Override
    public void setOnMsgActionListener(OnMsgActionListener listener) {
        mMsgActionListener = listener;
    }

    @Override
    public OnMsgActionListener getOnMsgActionListener() {
        return mMsgActionListener;
    }


    @Override
    public void setOnDeviceFreeStateCallBack(String deviceToken, OnDeviceFreeStateCallBack callBack) {
        if (deviceToken != null) {
            mDeviceFreeStateCallbackMap.put(deviceToken, callBack);
        }
    }

    /**
     * 处理由服务端(ANDROID)发送的消息
     *
     * @param deviceToken
     * @param connector
     * @param msg
     * @return 保存当前发送的消息返回true, 否则返回false
     */
    public boolean handleMsgSendFromServer(String deviceToken, SocketConnect connector, String msg) throws IllegalStateException {
        //TODO: 解析消息
        boolean isSaveMsg = true;
        return isSaveMsg;
    }

    /**
     * 处理由客户端(PC)发送的消息
     *
     * @param deviceToken
     * @param connector
     * @param msg
     * @return 保存当前发送的消息返回true, 否则返回false
     */
    public boolean handleMsgSendFromClient(String deviceToken, SocketConnect connector, String msg) throws IllegalStateException {
        //TODO: 解析消息
        boolean isSaveMsg = true;
        return isSaveMsg;
    }

    /**
     * 处理来自客户端的消息,ANDROID 端执行
     *
     * @param connector   连接对象相关信息
     * @param deviceToken 设备标识
     * @param msg         消息内容
     * @return
     * @throws IllegalStateException
     */
    public boolean handleMsgReceiveFromClient(SocketConnect connector, String deviceToken, String msg)
            throws IllegalStateException {
        //TODO: 解析消息
        boolean isSaveMsg = true;
        return isSaveMsg;
    }

    /**
     * 处理来自服务端的消息,PC 端执行
     *
     * @param connector   连接对象相差信息
     * @param deviceToken 设备标识
     * @param msg         消息内容
     * @return
     * @throws IllegalStateException
     */
    public boolean handleMsgReceiveFromServer(SocketConnect connector, String deviceToken, String msg)
            throws IllegalStateException {
        boolean isSaveMsg = true;
        // 更新设备信息,建立连接状态
        //connector.getDeviceInfo().updateDeviceConnectState(true);
        // 更新设备状态,不管是否支付成功,支付完都设置为空闲
        //connector.getDeviceInfo().setFreeState(true);
        // 此回调只会使用一次
        //OnDeviceFreeStateCallBack callBack = mDeviceFreeStateCallbackMap.remove(deviceToken);
        //if (callBack != null) {
        //    callBack.onDeviceFreeStateChanged(deviceToken, true);
        //}
        return isSaveMsg;
    }
}
