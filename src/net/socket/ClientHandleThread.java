package net.socket;

import net.interfaces.OnConnectionChangedListener;
import net.manager.IMsgParseMgrAction;
import net.utils.CommonUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 连接socket处理线程,此线程只负责处理接收消息与发送消息
 *
 * @author taro
 */
public class ClientHandleThread extends Thread implements IClientAction {

    private volatile boolean mIsClientAlive = false;
    private String TAG_CLIENT = "";
    // 发送消息的处理线程
    private SendMsgQueueHandleThread mSendMsgQueueHandleThread = null;
    // 发送心跳检测线程
    private SendHeartbeatThread mSendHeartbeatThread = null;
    // 消息缓存的列表
    private LinkedBlockingQueue<String> mSendMsgQueue = null;
    // 客户端
    private Socket mClient = null;
    private SocketConnect mParentConnect = null;
    // 接收消息回调的接口
    private IMsgParseMgrAction mMsgParseAction = null;
    // 连接状态更新监听接口
    private OnConnectionChangedListener mOnConnectionChangedListener = null;
    // 最后一次心跳(或者消息)接收到的时间
    // 由于服务端会持续发送心跳消息,一旦接收不到任何消息,即存在断连的可能
    // 消息间隔不超过5秒
    private long mLastHeartbeatTimestamp = 0;
    // 是否需要检测心跳连接(客户端的操作)
    private boolean mIsNeedCheckHeartbeat = false;

    /**
     * 创建socket处理线程
     *
     * @param connect 来自父级的连接信息,同时也包含了此对象的引用
     * @param client  socket客户端
     */
    public ClientHandleThread(SocketConnect connect, Socket client) {
        this(connect, client, null);
    }

    /**
     * 创建socket处理线程
     *
     * @param connect 来自父级的连接信息,同时也包含了此对象的引用
     * @param client  socket客户端
     * @param action  接收消息的回调事件
     */
    public ClientHandleThread(SocketConnect connect, Socket client, IMsgParseMgrAction action) {
        if (client == null || connect == null) {
            throw new RuntimeException("socket连接或客户端连接对象及信息不可为null");
        }
        mParentConnect = connect;
        mClient = client;
        mMsgParseAction = action;
        mSendMsgQueue = new LinkedBlockingQueue<String>(100);
        mSendMsgQueueHandleThread = new SendMsgQueueHandleThread();
        mIsClientAlive = true;
    }

    @Override
    public String getTag() {
        if (mParentConnect != null) {
            return mParentConnect.getDeviceInfo().getClientTag();
        } else {
            return TAG_CLIENT;
        }
    }

    @Override
    public synchronized void closeClient() {
        if (mOnConnectionChangedListener != null) {
            CommonUtils.logInfo("socket 连接正在关闭...");
            mOnConnectionChangedListener.onConnectClosing(mParentConnect.getDeviceToken(),
                    mParentConnect.getSocketWorkType());
        }
        if (!mClient.isClosed()) {
            try {
                mIsClientAlive = false;
                if (mSendHeartbeatThread != null && mSendHeartbeatThread.isRunning()) {
                    mSendHeartbeatThread.stop();
                    mSendHeartbeatThread = null;
                }
                // 关闭发送消息线程
                if (mSendMsgQueueHandleThread != null && mSendMsgQueueHandleThread.isRunning()) {
                    //TODO:发送线程关闭消息
                    try {
                        this.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mSendMsgQueueHandleThread.close();
                    mSendMsgQueueHandleThread = null;
                }
                // 关闭接收消息的线程
                if (this.isAlive()) {
                    this.interrupt();
                }
                // 关闭连接
                mClient.close();
                mClient = null;
                mParentConnect.getDeviceInfo().updateDeviceConnectState(false);
            } catch (IOException e) {
                e.printStackTrace();
                CommonUtils.logError(e);
            }
        }
    }

    @Override
    public void startHeartbeatConnect() {
        if (mSendHeartbeatThread == null || !mSendHeartbeatThread.isRunning()) {
            mSendHeartbeatThread = new SendHeartbeatThread();
            mSendHeartbeatThread.start();
        }
    }

    @Override
    public boolean isClientAlive() {
        return mIsClientAlive;
    }

    @Override
    public void setIMsgParseAction(IMsgParseMgrAction action) {
        mMsgParseAction = action;
    }

    @Override
    public IMsgParseMgrAction getIMsgParseAction() {
        return mMsgParseAction;
    }

    @Override
    public void setOnConnectionChangedListener(OnConnectionChangedListener listener) {
        mOnConnectionChangedListener = listener;
    }

    @Override
    public OnConnectionChangedListener getOnConnectionChangedListener() {
        return mOnConnectionChangedListener;
    }

    @Override
    public void sendMsg(String output) {
        if (!CommonUtils.isEmptyString(output)) {
            try {
                // 将消息存入发送队列
                mSendMsgQueue.put(output);
                if (mSendMsgQueueHandleThread == null) {
                    mSendMsgQueueHandleThread = new SendMsgQueueHandleThread();
                }
                // 发送线程启动发送
                mSendMsgQueueHandleThread.sendMsg();
            } catch (InterruptedException e) {
                e.printStackTrace();
                CommonUtils.logError(e);
            }
        }
    }

    @Override
    public void run() {
        System.out.println("进入数据接收监听...");
        if (mClient != null) {
            InputStream in = null;
            try {
                in = mClient.getInputStream();
                String receiveMsg = null;
                byte[] buffer = new byte[1024];
                StringBuilder bufferContainer = new StringBuilder();
                while (mIsClientAlive) {
                    // 存在数据可读
                    if (in.available() > 0) {
                        // 读取数据并转成字符串
                        receiveMsg = CommonUtils.readOnceStrFromInputStream(in, buffer, bufferContainer, false);
                        // 回调传递读取到的字符串数据
                        if (mMsgParseAction != null) {
                            mMsgParseAction.onParseReceivedMsg(mParentConnect, mParentConnect.getDeviceToken(),
                                    mParentConnect.getSocketWorkType(), receiveMsg);
                        }
                        if (!CommonUtils.isEmptyString(receiveMsg)) {
                            mLastHeartbeatTimestamp = System.currentTimeMillis();
                        }
                    }
                    // 每一次读取数据后暂停500毫秒的时间
                    Thread.sleep(500);
                    if (mIsNeedCheckHeartbeat) {
                        long keepTime = System.currentTimeMillis() - mLastHeartbeatTimestamp;
                        if (keepTime > 10000) {
                            // 超过10秒未有任何连接消息
                            CommonUtils.logInfo("已超过心跳连接时间!!!!!!");
                            if (mOnConnectionChangedListener != null) {
                                mOnConnectionChangedListener.onConnectTimeout(mParentConnect.getDeviceToken(),
                                        mParentConnect.getSocketWorkType());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtils.logError(e);
            } finally {
                // 关闭流
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        CommonUtils.logError(e);
                    }
                }
            }
        }
    }

    /**
     * 客户端的发送消息线程
     *
     * @author taro
     */
    private class SendHeartbeatThread implements Runnable {
        private volatile boolean mIsRunning = false;
        private Thread mHeartbeatThread = null;

        /**
         * 获取当前发送数据后台线程是否正在进行
         *
         * @return
         */
        public boolean isRunning() {
            return mIsRunning;
        }

        /**
         * 关闭发送消息的线程
         */
        public void stop() {
            if (mHeartbeatThread != null && mHeartbeatThread.isAlive()) {
                mIsRunning = false;
                mHeartbeatThread.interrupt();
                mHeartbeatThread = null;
            }
        }

        /**
         * 通知发送开始发送消息
         */
        public synchronized void start() {
            if (mSendMsgQueue == null || mClient == null || !mIsClientAlive) {
                return;
            } else {
                if (mHeartbeatThread == null) {
                    mHeartbeatThread = new Thread(this);
                    mHeartbeatThread.setDaemon(true);
                    mHeartbeatThread.start();
                }
            }
        }

        @Override
        public void run() {
            mIsRunning = true;
            try {
                while (mIsRunning && mIsClientAlive) {
                    //TODO:发送心跳消息
                    // 每5000毫秒发一次心跳消息
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtils.logError(e);
            } finally {
                mIsRunning = false;
            }
        }
    }

    /**
     * 客户端的发送消息线程
     *
     * @author taro
     */
    private class SendMsgQueueHandleThread implements Runnable {
        private volatile boolean mIsRunning = false;
        private Thread mSendMsgThread = null;

        /**
         * 获取当前发送数据后台线程是否正在进行
         *
         * @return
         */
        public boolean isRunning() {
            return mIsRunning;
        }

        /**
         * 关闭发送消息的线程
         */
        public void close() {
            if (mSendMsgThread != null && mSendMsgThread.isAlive()) {
                mIsRunning = false;
                mSendMsgThread.interrupt();
                mSendMsgThread = null;
            }
        }

        /**
         * 通知发送开始发送消息
         */
        public synchronized void sendMsg() {
            if (mSendMsgQueue == null || mClient == null || !mIsClientAlive) {
                return;
            } else {
                if (mSendMsgThread == null) {
                    mSendMsgThread = new Thread(this);
                    mSendMsgThread.setDaemon(true);
                    mSendMsgThread.start();
                }
            }
        }

        @Override
        public void run() {
            mIsRunning = true;
            OutputStream outputStream = null;
            try {
                outputStream = mClient.getOutputStream();
                int sleepTime = 500;
                while (mIsRunning && mIsClientAlive) {
                    String output = mSendMsgQueue.poll();
                    if (CommonUtils.sendStringToOutputStream(outputStream, output, false)) {
                        if (mMsgParseAction != null) {
                            mMsgParseAction.onParseSentMsg(mParentConnect, mParentConnect.getDeviceToken(),
                                    mParentConnect.getSocketWorkType(), output);
                        }
                    }
                    // 最多每500毫秒发一次消息
                    Thread.sleep(sleepTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
                CommonUtils.logError(e);
                if (e instanceof SocketException) {
                    // 可能由于连接端的断开无法写入数据,直接断开连接
                    mIsRunning = false;
                    ClientHandleThread.this.closeClient();
                }
            } finally {
                mIsRunning = false;
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
