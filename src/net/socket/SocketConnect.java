package net.socket;

import net.interfaces.OnConnectionChangedListener;
import net.interfaces.OnInfoChangedListener;
import net.manager.IMsgParseMgrAction;
import net.utils.AdbUtils;
import net.utils.CommonUtils;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;


/**
 * 客户端处理及连接相关信息
 *
 * @author taro
 */
public class SocketConnect implements ISocketAction {
    private DeviceInfo mDeviceInfo = null;
    private ClientHandleThread mClientThread = null;
    private IMsgParseMgrAction mMsgParseAction = null;
    private OnConnectionChangedListener mOnConnectionChangedListener = null;

    /**
     * 创建服务端专用的连接及消息
     *
     * @param clientTag 客户端标识,此参数来自于服务端
     * @param client    socket客户端
     * @return
     */
    public static SocketConnect createServerConnect(String clientTag, Socket client) {
        return new SocketConnect(clientTag, client);
    }

    /**
     * 创建服务端专用连接使用
     *
     * @param clientTag
     * @param client
     */
    private SocketConnect(String clientTag, Socket client) {
        mDeviceInfo = DeviceInfo.createServerDeviceInfo(this, clientTag);
        // 创建客户端线程
        mClientThread = new ClientHandleThread(this, client);
    }

    /**
     * 创建客户端使用的连接,以设备标识为唯一识别消息
     *
     * @param deviceToken
     */
    public SocketConnect(String deviceToken) {
        if (CommonUtils.isEmptyString(deviceToken)) {
            throw new RuntimeException("设备标识码不能为null");
        }
        mDeviceInfo = DeviceInfo.createClientDeviceInfo(this, deviceToken);
    }

    /**
     * 创建新的socket连接
     *
     * @param host 连接的主机地址
     * @param port 使用的端口号
     * @return
     */
    public static Socket createNewSocket(String host, int port) {
        if (CommonUtils.isEmptyString(host) || port <= DeviceInfo.START_PORT || port > DeviceInfo.END_PORT) {
            throw new RuntimeException("创建socket时,host与port不可为null,port不可在不正常范围内: host = " + host + "/port = " + port);
        }
        try {
            // 不可以使用setReuseAddress,否则会出现bindException:Address is used
            // 原因不明
            Socket socket = new Socket(host, port);
            return socket;
        } catch (SocketException e) {
            CommonUtils.logError(e);
            return null;
        } catch (IOException e) {
            CommonUtils.logError(e);
            return null;
        }
    }

    @Override
    public DeviceInfo getDeviceInfo() {
        return mDeviceInfo;
    }

    @Override
    public void updateDeviceToken(String deviceToken) {
        mDeviceInfo.setDeviceToken(deviceToken);
    }

    @Override
    public boolean isDeviceReady() {
        return mDeviceInfo.isDeviceReady();
    }

    @Override
    public void setIsDeviceReady(boolean isReady) {
        mDeviceInfo.setDeviceReady(isReady);
    }

    @Override
    public String getDeviceToken() {
        return mDeviceInfo.getDeviceToken();
    }

    @Override
    public int getSocketWorkType() {
        return mDeviceInfo.getSocketWorkType();
    }

    @Override
    public void setIMsgParseAction(IMsgParseMgrAction action) {
        mMsgParseAction = action;
        if (mClientThread != null) {
            mClientThread.setIMsgParseAction(action);
        }
    }

    @Override
    public void setOnConnectionChangedListener(OnConnectionChangedListener listener) {
        mOnConnectionChangedListener = listener;
        if (mClientThread != null) {
            mClientThread.setOnConnectionChangedListener(listener);
        }
    }

    @Override
    public boolean connectInClient(AdbUtils adbUtils) {
        if (mClientThread != null && mClientThread.isClientAlive()) {
            startReceiveMsg();
            return true;
        } else {
            if (mDeviceInfo.getSocketWorkType() == DeviceInfo.SOCKET_IN_SERVER) {
                throw new RuntimeException("此连接创建在服务端运行,无法作为客户端连接启动");
            }
            // 尝试关闭连接再重新连接(若客户端已经存在但失活)
            closeClient();
            // 转发接口
            adbUtils.forwardPort(mDeviceInfo.getDeviceToken(), String.valueOf(mDeviceInfo.getLocalPort()),
                    String.valueOf(mDeviceInfo.getUsbPort()));
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                CommonUtils.logError(e);
            }
            if (mDeviceInfo.isDeviceReady()) {
                // 创建连接
                Socket client = createNewSocket("127.0.0.1", mDeviceInfo.getLocalPort());
                // 创建连接处理线程
                mClientThread = new ClientHandleThread(this, client);
                mClientThread.setIMsgParseAction(mMsgParseAction);
                mClientThread.setOnConnectionChangedListener(mOnConnectionChangedListener);
                // 开启监听消息线程
                startReceiveMsg();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public void connectInServer() {
        if (mDeviceInfo.getSocketWorkType() == DeviceInfo.SOCKET_IN_CLIENT) {
            throw new RuntimeException("此连接创建在客户端运行,无法作为服务端连接启动");
        }
        startReceiveMsg();
    }

    @Override
    public boolean isClientAlive() {
        return !(mClientThread == null || !mClientThread.isAlive() || !mClientThread.isClientAlive());
    }

    @Override
    public void closeClient() {
        if (mClientThread != null) {
            mClientThread.closeClient();
            mClientThread = null;
        }
    }

    @Override
    public void startHeartbeatConnect() {
        if (mClientThread != null && mClientThread.isClientAlive()) {
            mClientThread.startHeartbeatConnect();
        }
    }

    @Override
    public boolean startReceiveMsg() {
        if (mClientThread != null && mClientThread.isClientAlive()) {
            if (!mClientThread.isAlive()) {
                mClientThread.setIMsgParseAction(mMsgParseAction);
                mClientThread.start();
            }
            return true;
        }
        return false;
    }

    @Override
    public IMsgParseMgrAction getIMsgParseAction() {
        return mMsgParseAction;
    }

    @Override
    public OnConnectionChangedListener getOnConnectionChangedListener() {
        return mOnConnectionChangedListener;
    }

    @Override
    public String getTag() {
        return mDeviceInfo.getClientTag();
    }

    @Override
    public void sendMsg(String output) {
        if (!CommonUtils.isEmptyString(output) && mClientThread != null && mClientThread.isClientAlive()) {
            mClientThread.sendMsg(output);
        }
    }

    @Override
    public void setOnInfoChangedListener(OnInfoChangedListener listener) {
        mDeviceInfo.setOnInfoChangedListener(listener);
    }

    @Override
    public OnInfoChangedListener getOnInfoChangedListener() {
        return mDeviceInfo.getOnInfoChangedListener();
    }
}
