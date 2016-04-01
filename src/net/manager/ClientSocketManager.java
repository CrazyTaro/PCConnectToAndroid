package net.manager;


import net.interfaces.OnConnectionChangedListener;
import net.interfaces.OnInfoChangedListener;
import net.socket.DeviceInfo;
import net.socket.SocketConnect;
import net.utils.AdbUtils;
import net.utils.CommonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 多个客户端连接对象的管理
 */
public class ClientSocketManager implements ISocketMgrAction {
    // 使用对象及变量
    private AdbUtils mAdb = null;
    private List<String> mDeviceTokens = null;
    private Map<String, SocketConnect> mClientSocketMap = null;

    private OnInfoChangedListener mPresetInfoChangedListener = null;
    private IMsgParseMgrAction mPresetMsgParseAction = null;
    private OnConnectionChangedListener mPresetConnectionChangedListener = null;

    private ReentrantLock mSocketMapLock = null;

    public ClientSocketManager(AdbUtils adbUtils) {
        resetAdbUtils(adbUtils);
        mClientSocketMap = new ConcurrentHashMap<String, SocketConnect>();
    }

    /**
     * 创建新的连接对象
     *
     * @param deviceToken 标识
     * @param socketMap   客户端连接保存Map
     * @param adbUtils    adb工具
     * @return 创建的连接对象, 此连接对象有可能是不可用的, 因为需要设备需要启动服务并且成功无误之后才可以进行正常的连接
     */
    private SocketConnect createNewSocketConnet(String deviceToken, Map<String, SocketConnect> socketMap,
                                                AdbUtils adbUtils) {
        SocketConnect socket = new SocketConnect(deviceToken);
        socket.getDeviceInfo().initialPort(socketMap);
        HashMap<String, Integer> intExtra = new HashMap<String, Integer>();
        intExtra.put("port", socket.getDeviceInfo().getUsbPort());
        String result = adbUtils.broadcast(deviceToken, AdbUtils.ACTION_START_SERVICE, null, intExtra, null);
        if (result.contains("result=0")) {
            socket.setIsDeviceReady(true);
        }
        return socket;
    }

    @Override
    public void updateSocketClientConnect() {
        updateDeviceToken();
        for (String device : mDeviceTokens) {
            if (mClientSocketMap == null || mClientSocketMap.containsKey(device)) {
                continue;
            } else {
                // 创建连接对象,但没有自动进行连接
                SocketConnect socket = createNewSocketConnet(device, mClientSocketMap, mAdb);
                mClientSocketMap.put(device, socket);
            }
        }
        Set<String> keySet = mClientSocketMap.keySet();
        if (keySet != null) {
            for (String token : keySet) {
                if (!mDeviceTokens.contains(token)) {
                    closeSocket(token);
                }
            }
        }
    }

    @Override
    public void connectAll(IMsgParseMgrAction msgParseAction, OnConnectionChangedListener connectListener, OnInfoChangedListener infoChangedListener) {
        if (mClientSocketMap == null || mClientSocketMap.size() <= 0) {
            return;
        } else {
            for (SocketConnect socket : mClientSocketMap.values()) {
                // 客户端处理线程已存在则不进行连接
                if (!socket.isClientAlive()) {
                    socket.setIMsgParseAction(msgParseAction);
                    socket.setOnConnectionChangedListener(connectListener);
                    socket.setOnInfoChangedListener(infoChangedListener);
                    if (!socket.connectInClient(mAdb)) {
                        System.out.println("device:" + socket.getDeviceToken() + " : 尝试连接不成功!\n isDeviceReady = "
                                + socket.isDeviceReady());
                    }
                }
            }
        }
    }

    @Override
    public boolean connectSocket(String deviceToken, IMsgParseMgrAction msgParseAction,
                                 OnConnectionChangedListener connectListener, OnInfoChangedListener infoChangedListener) {
        if (mClientSocketMap == null || !mClientSocketMap.containsKey(deviceToken)) {
            return false;
        } else {
            SocketConnect socket = mClientSocketMap.get(deviceToken);
            if (!socket.isClientAlive()) {
                socket.setIMsgParseAction(msgParseAction);
                socket.setOnConnectionChangedListener(connectListener);
                socket.setOnInfoChangedListener(infoChangedListener);
                // PC客户端进行连接,需要使用ADB工具处理
                return socket.connectInClient(mAdb);
            } else {
                return false;
            }
        }
    }

    @Override
    public void connectAllUsePresetListener() {
        if (mPresetMsgParseAction == null) {
            throw new RuntimeException("没有预设 IMsgParseAction对象,无法进行消息处理");
        }
        connectAll(mPresetMsgParseAction, mPresetConnectionChangedListener, mPresetInfoChangedListener);
    }

    @Override
    public void connectSocketUsePresetListener(String deviceToekn) {
        if (mPresetMsgParseAction == null) {
            throw new RuntimeException("没有预设 IMsgParseAction对象,无法进行消息处理");
        }
        connectSocket(deviceToekn, mPresetMsgParseAction, mPresetConnectionChangedListener, mPresetInfoChangedListener);
    }

    @Override
    public void closeAll() {
        if (mClientSocketMap == null || mClientSocketMap.size() <= 0) {
            return;
        } else {
            for (SocketConnect socket : mClientSocketMap.values()) {
                socket.closeClient();
            }
            mClientSocketMap.clear();
        }
    }

    @Override
    public boolean closeSocket(String deviceToken) {
        if (mClientSocketMap == null || !mClientSocketMap.containsKey(deviceToken)) {
            return false;
        } else {
            SocketConnect socket = mClientSocketMap.get(deviceToken);
            socket.closeClient();
            mClientSocketMap.remove(deviceToken);
            return true;
        }
    }

    @Override
    public void updateDeviceToken() {
        mDeviceTokens = mAdb.getDevices();
    }

    @Override
    public void setOnInfoChangedListener(String deviceToken, OnInfoChangedListener listener) {
        for (SocketConnect socket : mClientSocketMap.values()) {
            socket.setOnInfoChangedListener(listener);
        }
    }

    @Override
    public void setOnConnectionChangedListener(String deviceToken, OnConnectionChangedListener listener) {
        SocketConnect socket = getSocketByDeviceToken(deviceToken);
        if (socket != null) {
            socket.setOnConnectionChangedListener(listener);
        }
    }

    @Override
    public void setIMsgParseAction(String deviceToken, IMsgParseMgrAction msgParseAction) {
        SocketConnect socket = getSocketByDeviceToken(deviceToken);
        if (socket != null) {
            socket.setIMsgParseAction(msgParseAction);
        }
    }

    @Override
    public void presetOnInfoChangedListener(OnInfoChangedListener listener) {
        mPresetInfoChangedListener = listener;
        for (SocketConnect socket : mClientSocketMap.values()) {
            socket.setOnInfoChangedListener(listener);
        }
    }

    @Override
    public void presetIMsgParseAction(IMsgParseMgrAction msgParseAction) {
        mPresetMsgParseAction = msgParseAction;
        for (SocketConnect socket : mClientSocketMap.values()) {
            socket.setIMsgParseAction(msgParseAction);
        }
    }

    @Override
    public void presetOnConnectionChangedListener(OnConnectionChangedListener listener) {
        mPresetConnectionChangedListener = listener;
        for (SocketConnect socket : mClientSocketMap.values()) {
            socket.setOnConnectionChangedListener(listener);
        }
    }

    @Override
    public IMsgParseMgrAction getPresetIMsgParseAction() {
        return mPresetMsgParseAction;
    }

    @Override
    public OnInfoChangedListener getPresetOnInfoChangedListener() {
        return mPresetInfoChangedListener;
    }

    @Override
    public OnConnectionChangedListener getPresetOnConnectChangedListener() {
        return mPresetConnectionChangedListener;
    }

    @Override
    public List<String> getUSBDeviceToken() {
        return mDeviceTokens;
    }

    @Override
    public void resetAdbUtils(AdbUtils adbUtils) {
        if (adbUtils == null) {
            throw new RuntimeException("adb utils不可为null,必须是有效的对象");
        }
        mAdb = adbUtils;
    }

    @Override
    public boolean isSocketConnectAlive(String deviceToken) {
        SocketConnect connect = getSocketByDeviceToken(deviceToken);
        return connect == null ? false : connect.isClientAlive();
    }

    @Override
    public SocketConnect getSocketByDeviceToken(String deviceToken) {
        if (mClientSocketMap != null) {
            return mClientSocketMap.get(deviceToken == null ? "" : deviceToken);
        } else {
            return null;
        }
    }

    @Override
    public DeviceInfo getSocketDeviceInfo(String deviceToken) {
        SocketConnect socket = getSocketByDeviceToken(deviceToken);
        if (socket != null) {
            return socket.getDeviceInfo();
        } else {
            return null;
        }
    }

    @Override
    public Set<String> getConnectedDeviceToken() {
        return mClientSocketMap != null ? mClientSocketMap.keySet() : null;
    }

    @Override
    public void sendMsgToSocket(String deviceToken, String msg) {
        SocketConnect socket = getSocketByDeviceToken(deviceToken);
        if (socket != null) {
            socket.sendMsg(msg);
        }
    }

    @Override
    public void sendMsgToAll(String msg) {
        if (mClientSocketMap != null) {
            for (SocketConnect socket : mClientSocketMap.values()) {
                if (socket != null) {
                    socket.sendMsg(msg);
                }
            }
        }
    }

    @Override
    public void cleanEnvironment() {
        if (mClientSocketMap.size() <= 0) {
            CommonUtils.execCommand(mAdb.removeAllForwardPort(), false, null, null);
        }
        mPresetMsgParseAction = null;
        mPresetConnectionChangedListener = null;
        mPresetInfoChangedListener = null;
    }

    @Override
    public void startService(String deviceToken, int serverPort) {
        HashMap<String, Integer> intExtra = new HashMap<String, Integer>(1);
        intExtra.put("port", serverPort);
        mAdb.broadcast(deviceToken, AdbUtils.ACTION_START_SERVICE, null, intExtra, null);
    }

    @Override
    public void stopService(String deviceToken) {
        SocketConnect socket = getSocketByDeviceToken(deviceToken);
        if (socket != null) {
            socket.closeClient();
        }
        mClientSocketMap.remove(deviceToken);
        mAdb.broadcast(deviceToken, AdbUtils.ACTION_STOP_SERVICE, null, null, null);
    }

    @Override
    public void stopAllService() {
        closeAll();
        cleanEnvironment();
        updateDeviceToken();
        for (String device : mDeviceTokens) {
            mAdb.broadcast(device, AdbUtils.ACTION_STOP_SERVICE, null, null, null);
        }
    }
}
