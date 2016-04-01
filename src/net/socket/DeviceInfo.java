package net.socket;

import net.interfaces.OnInfoChangedListener;

import java.util.Map;
import java.util.Random;

/**
 * Created by taro on 16/3/14. 设备相关信息
 */
public class DeviceInfo implements IDeviceAction {
    /**
     * 可用的开始端口号
     */
    public static final int START_PORT = 10000;
    /**
     * 可用的结束端口号
     */
    public static final int END_PORT = 65535;
    /**
     * 可用的端口号范围
     */
    public static final int PORT_BOUND = END_PORT - START_PORT;

    /**
     * 当前socket对象运行的环境,在客户端中(PC),同时可能存在多个其它的客户端,
     * 每个客户端收发消息需要以device_token识别消息来自哪个服务端(Android)<br/>
     * 客户端与服务端的连接对象处理逻辑是相同的,使用的是相同的类对象,需要以此标识进行识别,{@link #getSocketWorkType()}
     */
    public static final int SOCKET_IN_CLIENT = 110;
    /**
     * 当前socket对象运行的环境,在服务端中(Android),仅存在一个连接对象处理,仅连接一个客户端<br/>
     * 客户端与服务端的连接对象处理逻辑是相同的,使用的是相同的类对象,需要以此标识进行识别,{@link #getSocketWorkType()}
     */
    public static final int SOCKET_IN_SERVER = 119;

    public static final int CHANGED_TYPE_MOBILE = 0X1;
    public static final int CHANGED_TYPE_CLIENT_TAG = 0X2;
    public static final int CHANGED_TYPE_SOCKET_WORK_TYPE = 0X3;
    public static final int CHANGED_TYPE_FREE_STATE = 0X4;
    public static final int CHANGED_TYPE_CONNECT_AVALIABLE = 0X5;
    public static final int CHANGED_TYPE_DEVICE_READY = 0X6;
    public static final int CHANGED_TYPE_DEVICE_TOKEN = 0X7;

    private SocketConnect mParentSocket = null;
    // 设备标识
    private String mDeviceToken = null;
    // 客户端标识
    private String mClientTag = null;
    // 本地端口
    private int mLocalPort = -1;
    // 转发USB端口,服务端不存在USB端口
    private int mUsbPort = -1;
    // 设备是否已经准备好连接(服务是否已经启动...)
    private boolean mIsDeviceReady = false;
    // 当前连接所在的工作环境,在客户端或者是服务端
    private int mSocketWorkType = -1;
    // 设备绑定的号码
    private String mBindMobile = null;
    // 当前设备连接是否空闲
    private volatile boolean mIsFree = false;
    // 当前设备连接是否可用(设备可能断开连接但设备还是可用的状态)
    private boolean mIsConnectAvaliable = false;
    private OnInfoChangedListener mOnInfoChangedListener = null;

    /**
     * 创建用于服务端运行的连接设备信息
     *
     * @param socket
     * @param clientTag
     * @return
     */
    public static DeviceInfo createServerDeviceInfo(SocketConnect socket, String clientTag) {
        return new DeviceInfo(socket, null, clientTag, SOCKET_IN_SERVER);
    }

    /**
     * 创建用于客户端运行的连接设备信息
     *
     * @param socket
     * @param deviceToken
     * @return
     */
    public static DeviceInfo createClientDeviceInfo(SocketConnect socket, String deviceToken) {
        return new DeviceInfo(socket, deviceToken, null, SOCKET_IN_CLIENT);
    }

    /**
     * 创建设备信息
     *
     * @param socket
     * @param deviceToken    设备标识
     * @param clientTag      客户端标识
     * @param socketWorkType 连接创建的环境,客户端或者是服务端
     */
    private DeviceInfo(SocketConnect socket, String deviceToken, String clientTag, int socketWorkType) {
        if (socket == null) {
            throw new RuntimeException("socket 连接对象不可为null");
        }
        mDeviceToken = deviceToken;
        mClientTag = clientTag;
        mSocketWorkType = socketWorkType;
        mParentSocket = socket;
    }

    /**
     * 回调信息更新接口
     *
     * @param changedType
     * @param oldValue
     */
    private void callbackInfoListener(int changedType, Object oldValue) {
        if (mOnInfoChangedListener != null) {
            mOnInfoChangedListener.onDeviceInfoChanged(mDeviceToken, this, changedType, oldValue);
        }
    }

    /**
     * 更新设备识别,只限服务端的连接有效
     *
     * @param deviceToken
     */
    public void setDeviceToken(String deviceToken) {
        if (mSocketWorkType == SOCKET_IN_SERVER) {
            String oldValue = mDeviceToken;
            mDeviceToken = deviceToken;
            callbackInfoListener(CHANGED_TYPE_DEVICE_TOKEN, oldValue);
        }
    }

    /**
     * 更新设备当前准备状态,仅客户端有效
     *
     * @param isDeviceReady
     */
    public void setDeviceReady(boolean isDeviceReady) {
        Object oldValue = mIsDeviceReady;
        mIsDeviceReady = isDeviceReady;
        callbackInfoListener(CHANGED_TYPE_DEVICE_READY, oldValue);
    }

    /**
     * 更新客户端识别
     *
     * @param clientTag
     */
    public void setClientTag(String clientTag) {
        Object oldValue = mClientTag;
        mClientTag = clientTag;
        callbackInfoListener(CHANGED_TYPE_CLIENT_TAG, oldValue);
    }

    /**
     * 更新当前设备的空闲状态,true为空闲,false不空闲
     *
     * @param isFree
     */
    public void setFreeState(boolean isFree) {
        Object oldValue = mIsFree;
        mIsFree = isFree;
        callbackInfoListener(CHANGED_TYPE_FREE_STATE, oldValue);
    }

    /**
     * 更新设备绑定的手机号
     *
     * @param mobile
     */
    public void setBindMobile(String mobile) {
        Object oldValue = mBindMobile;
        mBindMobile = mobile;
        callbackInfoListener(CHANGED_TYPE_MOBILE, oldValue);
    }

    /**
     * 更新设备连接是否可用
     *
     * @param isAvaliable
     */
    public void setConnectState(boolean isAvaliable) {
        Object oldValue = mIsConnectAvaliable;
        mIsConnectAvaliable = isAvaliable;
        setFreeState(isAvaliable);
        callbackInfoListener(CHANGED_TYPE_CONNECT_AVALIABLE, oldValue);
    }

    /**
     * 获取设备标识
     *
     * @return
     */
    public String getDeviceToken() {
        return mDeviceToken;
    }

    /**
     * 获取服务端标识
     *
     * @return
     */
    public String getClientTag() {
        return mClientTag;
    }

    /**
     * 获取本地端口
     *
     * @return
     */
    public int getLocalPort() {
        return mLocalPort;
    }

    /**
     * 获取USB转发端口,服务端保持该值为-1,无效
     *
     * @return
     */
    public int getUsbPort() {
        return mUsbPort;
    }

    /**
     * 获取当前设备是否已经准备好进入连接状态
     *
     * @return
     */
    public boolean isDeviceReady() {
        return mIsDeviceReady;
    }

    /**
     * 获取当前设备连接是否可用状态
     *
     * @return
     */
    public boolean isConnectAvaliable() {
        return mIsConnectAvaliable && (mParentSocket == null ? false : mParentSocket.isClientAlive());
    }

    /**
     * 获取设备工作的环境,客户端或者服务端
     *
     * @return
     */
    public int getSocketWorkType() {
        return mSocketWorkType;
    }

    /**
     * 获取当前设备是否空闲状态,依赖于当前连接是否已经建立
     *
     * @return
     */
    public boolean isFree() {
        return mIsFree && isConnectAvaliable();
    }

    /**
     * 获取与设备绑定的手机号
     *
     * @return
     */
    public String getBindMobile() {
        return mBindMobile;
    }

    /**
     * 客户端使用:初始化端口
     *
     * @param clientSocketMap 保存的多个客户端连接socketConnect
     */
    public void initialPort(Map<String, SocketConnect> clientSocketMap) {
        // 创建随机端口
        this.mLocalPort = createRandomLocalPort(clientSocketMap);
        // 创建adb转发端口,每一个客户端需要对应的是不同的设备,所以每一个客户端需要的转发端口是独立的,不可复用
        this.mUsbPort = getUsbPortFromLocalPort(this.mLocalPort);
    }

    /**
     * 成功创建连接
     *
     * @param isConnected
     */
    public void updateDeviceConnectState(boolean isConnected) {
        Object oldFreeState = mIsFree;
        Object oldConnectedState = mIsConnectAvaliable;
        mIsFree = isConnected;
        mIsConnectAvaliable = isConnected;
        callbackInfoListener(CHANGED_TYPE_CONNECT_AVALIABLE, oldConnectedState);
    }

    /**
     * 客户端使用:检测当前的本地端口是否被占用了
     *
     * @param localPort 本地端口(非转发端口)
     * @return
     */
    public boolean isContainPort(int localPort) {
        return (this.mLocalPort == localPort || this.mUsbPort == localPort);
    }

    /**
     * 获取由本地端口生成的相关联的usb使用端口
     *
     * @param localPort
     * @return
     */
    public static final int getUsbPortFromLocalPort(int localPort) {
        return localPort + 1;
    }

    /**
     * 获取随机生成的端口号
     *
     * @param clientSocketMap 设备标识与端口号绑定的map,每一个设备标识 =(对应) 一个本地端口号
     * @return
     */
    public static int createRandomLocalPort(Map<String, SocketConnect> clientSocketMap) {
        Random random = new Random(System.currentTimeMillis());
        if (clientSocketMap == null || clientSocketMap.size() <= 0) {
            return START_PORT + random.nextInt(PORT_BOUND);
        } else {
            boolean isUsed = false;
            while (true) {
                isUsed = false;
                int port = START_PORT + random.nextInt(PORT_BOUND);
                for (SocketConnect socket : clientSocketMap.values()) {
                    if (socket != null && socket.getDeviceInfo().isContainPort(port)) {
                        isUsed = true;
                        break;
                    }
                }
                if (!isUsed) {
                    return port;
                }
            }
        }
    }

    @Override
    public void setOnInfoChangedListener(OnInfoChangedListener listener) {
        mOnInfoChangedListener = listener;
    }

    @Override
    public OnInfoChangedListener getOnInfoChangedListener() {
        return mOnInfoChangedListener;
    }
}
