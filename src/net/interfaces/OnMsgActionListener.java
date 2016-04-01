package net.interfaces;

import net.socket.SocketConnect;

/**
 * 监听消息回调接口
 *
 * @author taro
 */
public interface OnMsgActionListener {
	/**
	 * 监听消息回调事件
	 *
	 * @param connector
	 * @param socketWorkType
	 * @param msg
	 */
	public void onReceiveMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg);

	/**
	 * 发送消息监听事件
	 * 
	 * @param connector
	 * @param deviceToken
	 *            设备标识
	 * @param socketWorkType
	 * @param msg
	 */
	public void onSendMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg);
}
