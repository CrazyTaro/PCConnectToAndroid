package net.interfaces;

/**
 * Created by taro on 16/3/9. 连接状态改变监听事件
 */
public interface OnConnectionChangedListener {
	/**
	 * 操持连接超时,可能连接会中断
	 *
	 * @param deviceToken
	 *            设备识别
	 * @param socketWorkType
	 *            socket工作环境,在客户端或者服务端中
	 */
	public void onConnectTimeout(String deviceToken, int socketWorkType);

	/**
	 * 连接断开
	 *
	 * @param deviceToken
	 *            设备识别
	 * @param socketWorkType
	 *            socket工作环境,在客户端或者服务端中
	 */
	public void onConnectClosing(String deviceToken, int socketWorkType);
}
