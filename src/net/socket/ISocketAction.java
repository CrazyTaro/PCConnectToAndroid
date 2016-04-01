package net.socket;

import net.utils.AdbUtils;

/**
 * Created by taro on 16/3/8.
 */
public interface ISocketAction extends IClientAction,IDeviceAction {
	/**
	 * 获取设备相关信息
	 *
	 * @return
	 */
	public DeviceInfo getDeviceInfo();

	/**
	 * 更新deviceToken,此方法仅提供给服务端使用(客户端的deviceToken不需要更新)
	 *
	 * @param deviceToken
	 */
	public void updateDeviceToken(String deviceToken);

	/**
	 * 设备是否已经准备完毕(服务成功启动的情况下),若为true则可以进行连接
	 *
	 * @return
	 */
	public boolean isDeviceReady();

	/**
	 * 设置设备是否已经准备完毕
	 *
	 * @param isReady
	 */
	public void setIsDeviceReady(boolean isReady);

	/**
	 * 获取连接的设备标识
	 *
	 * @return
	 */
	public String getDeviceToken();

	/**
	 * 识别socket处理的线程是在客户端还是在服务端<br/>
	 * {@link DeviceInfo#SOCKET_IN_CLIENT}<br/>
	 * {@link DeviceInfo#SOCKET_IN_SERVER}
	 *
	 * @return
	 */
	public int getSocketWorkType();

	/**
	 * 客户端使用:尝试创建socket进行连接
	 *
	 * @param adbUtils
	 *            adb工具,用于设置转发接口
	 * @return
	 */
	public boolean connectInClient(AdbUtils adbUtils);

	/**
	 * 服务端使用:建立连接(连接已经建立,实际上是启动socket的后台接收消息监听)
	 */
	public void connectInServer();

	/**
	 * 开始接收消息
	 */
	public boolean startReceiveMsg();
}
