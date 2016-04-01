package net.interfaces;

import net.socket.DeviceInfo;

/**
 * Created by taro on 16/3/16. 信息更新监听接口
 */
public interface OnInfoChangedListener {
	/**
	 * 设备信息更新监听事件
	 * @param deviceToken
	 * @param info
	 * @param changedType
	 * @param oldValue
	 */
	public void onDeviceInfoChanged(String deviceToken, DeviceInfo info, int changedType, Object oldValue);
}
