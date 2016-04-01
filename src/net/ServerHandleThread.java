package net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import net.manager.IMsgParseMgrAction;
import net.interfaces.IServerAction;
import net.interfaces.OnConnectionChangedListener;
import net.socket.DeviceInfo;
import net.socket.SocketConnect;
import net.utils.CommonUtils;

/**
 * Created by taro on 16/2/29. 服务端使用
 */
public class ServerHandleThread implements IServerAction, Runnable {

	// 服务端连接使用的端口
	private int mConnectPort = 10086;
	// 服务端的接收socket
	private ServerSocket mServerSocket = null;
	// 接收消息回调监听事件
	private IMsgParseMgrAction mMsgParseAction = null;
	// 设备连接状态监听事件
	private OnConnectionChangedListener mOnConnectChangedListener = null;
	// 客户端的socket
	private SocketConnect mClientSocket = null;
	// 服务端的后台监听客户端连接线程
	private Thread mBackgroundThread = null;
	// 后台线程中止变量
	private volatile boolean mIsKeepRunning = true;

	public ServerHandleThread(int port) {
		this(port, null, null);
	}

	/**
	 * 创建服务端管理线程
	 *
	 * @param port
	 *            服务监听端口
	 * @param msgParseAction
	 *            消息处理操作接口
	 * @param connectListener
	 *            设备连接状态监听事件
	 */
	public ServerHandleThread(int port, IMsgParseMgrAction msgParseAction, OnConnectionChangedListener connectListener) {
		try {
			mConnectPort = port;
			createServerSocket(port, true);
			mMsgParseAction = msgParseAction;
			mOnConnectChangedListener = connectListener;
		} catch (Exception e) {
			e.printStackTrace();
			CommonUtils.logError(e);
			throw new RuntimeException("创建服务端失败!!");
		}
	}

	/**
	 * 创建服务端socket连接
	 *
	 * @param port
	 *            连接端口,有效值在1024-65535
	 * @param isReplaceOld
	 *            是否替换原有的serversocket,若原socket存在
	 */
	private void createServerSocket(int port, boolean isReplaceOld) {
		if (port <= 0 || port > 65535) {
			throw new RuntimeException("端口不正确,请使用1024-65535的端口");
		}
		if (mServerSocket != null) {
			if (!mServerSocket.isClosed() && !isReplaceOld) {
				return;
			}
		}
		// null
		// closed
		// not closed but replace
		closeServer();
		try {
			mServerSocket = new ServerSocket();
			// 设置可立即重用端口
			mServerSocket.setReuseAddress(true);
			mServerSocket.bind(new InetSocketAddress(port));
		} catch (SocketException e) {
			e.printStackTrace();
			CommonUtils.logError(e);
		} catch (IOException e) {
			e.printStackTrace();
			CommonUtils.logError(e);
		}
	}

	/**
	 * 关闭服务/部分服务
	 *
	 * @param isCloseSocketServer
	 *            是否关闭后台的监听客户端连接socket
	 */
	private void closeServer(boolean isCloseSocketServer) {
		mIsKeepRunning = false;
		if (isCloseSocketServer) {
			if (mServerSocket != null && !mServerSocket.isClosed()) {
				try {
					mServerSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			mServerSocket = null;
		}
		if (mBackgroundThread != null && mBackgroundThread.isAlive()) {
			// 关闭后台监听客户端连接线程
			mBackgroundThread.interrupt();
			mBackgroundThread = null;
		}
		// 关闭连接的客户端
		if (mClientSocket != null && mClientSocket.isClientAlive()) {
			mClientSocket.closeClient();
			mClientSocket = null;
		}
	}

	@Override
	public void setIMsgParseAction(IMsgParseMgrAction msgParseAction) {
		mMsgParseAction = msgParseAction;
		if (mClientSocket != null) {
			mClientSocket.setIMsgParseAction(msgParseAction);
		}
	}

	@Override
	public IMsgParseMgrAction getIMsgParseAction() {
		return mMsgParseAction;
	}

	@Override
	public void setOnConnectChangedListener(OnConnectionChangedListener listener) {
		mOnConnectChangedListener = listener;
		if (mClientSocket != null) {
			mClientSocket.setOnConnectionChangedListener(listener);
		}
	}

	@Override
	public OnConnectionChangedListener getOnConnectionChangedListener() {
		return mOnConnectChangedListener;
	}

	@Override
	public void closeServer() {
		System.out.println("尝试关闭当前服务...");
		closeServer(true);
	}

	@Override
	public void startHeartbeat() {
		if (mClientSocket != null && mClientSocket.isClientAlive()) {
			mClientSocket.startHeartbeatConnect();
		}
	}

	@Override
	public void tryStartServer() {
		if (mServerSocket != null && mBackgroundThread != null && mBackgroundThread.isAlive()) {
			System.out.println("尝试启动已运行的服务,不处理...");
			return;
		} else {
			System.out.println("关闭当前服务并重新启动...");
			closeServer(false);
			restartServer();
		}
	}

	@Override
	public void restartServer() {
		System.out.println("尝试重新启动服务...");
		// 创建服务端连接socket
		if (mServerSocket == null) {
			createServerSocket(mConnectPort, true);
		}
		// 开启后台客户端连接监听线程
		if (mBackgroundThread == null || !mBackgroundThread.isAlive()) {
			mBackgroundThread = new Thread(this);
			mBackgroundThread.setDaemon(true);
			mBackgroundThread.start();
			System.out.println("已启动后台服务");
		}
	}

	@Override
	public void sendMsg(String output) {
		if (mClientSocket != null && mClientSocket.isClientAlive()) {
			mClientSocket.sendMsg(output);
		}
	}

	@Override
	public void run() {
		mIsKeepRunning = true;
		while (mIsKeepRunning) {
			try {
				System.out.println("开始接收客户端请求...");
				Socket client = mServerSocket.accept();
				if (client != null) {
					System.out.println("接收客户端通信...");
					String tag = String.valueOf(System.currentTimeMillis());
					// 理论上只会接收一个客户端
					mClientSocket = SocketConnect.createServerConnect(tag, client);
					// 设置消息接收与发送回调接口(用于处理消息及存储消息)
					mClientSocket.setIMsgParseAction(mMsgParseAction);
					// 尝试连接
					mClientSocket.connectInServer();
					//TODO:发送连接请求消息
				}
			} catch (Exception e) {
				e.printStackTrace();
				CommonUtils.logError(e);
				if (e instanceof IOException || e instanceof InterruptedException) {
					// 线程由于某些原因跳出了阻塞,但是线程并不会中止,还是要依赖于中止变量
					CommonUtils.logInfo("*****************服务器跳出阻塞,线程不会中止但是可以进行检测中止变量*******************");
				}
			}
		}
	}

	@Override
	public boolean isServerAlive() {
		return !(mServerSocket == null || mServerSocket.isClosed() || mBackgroundThread == null
				|| !mBackgroundThread.isAlive());
	}

	@Override
	public int getConnectPort() {
		return mConnectPort;
	}

    @Override
    public DeviceInfo getDeviceInfo() {
        if (mClientSocket != null) {
            return mClientSocket.getDeviceInfo();
        } else {
            return null;
        }
    }
}
