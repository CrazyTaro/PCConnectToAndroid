package net.manager;

import net.interfaces.OnMessageChangedListener;
import net.interfaces.OnMsgActionListener;
import net.manager.MessageInfo;

/**
 * Created by taro on 16/3/4. 消息管理操作
 */
public interface IMessageMgrAction {
	/**
	 * 获取指定设备的消息信息
	 *
	 * @param deviceToken
	 * @return
	 */
	public MessageInfo getMsgInfoByDevice(String deviceToken);

	/**
	 * 添加消息到指定设备消息缓存中(区分客户端消息和服务端消息)
	 *
	 * @param deviceToken
	 *            设备标识
	 * @param msg
	 *            消息内容
	 * @param from
	 *            消息来源对象:{@link MessageInfo#MSG_FOR_SEND}客户端/
	 *            {@link MessageInfo#MSG_FOR_RECEIVE}服务端
	 */
	public void appendMsg(String deviceToken, String msg, int from);

	/**
	 * 清除指定设备的消息
	 *
	 * @param deviceToken
	 *            设备标识
	 * @param from
	 *            消息来源对象:{@link MessageInfo#MSG_FOR_SEND}客户端/
	 *            {@link MessageInfo#MSG_FOR_RECEIVE}服务端
	 */
	public void clearMsg(String deviceToken, int from);

	/**
	 * 清除所有设备的消息
	 */
	public void clearMsgAllDevice(int from);

	/**
	 * 保存所有设备的消息
	 */
	public void saveMsgAllDevice(int from);

	/**
	 * 保存所有的消息
	 *
	 * @param deviceToken
	 *            设备标识
	 * @param isClearAfterSave
	 *            是否在保存后清除,true为清除,false不清除缓存
	 * @param from
	 *            消息来源对象:{@link MessageInfo#MSG_FOR_SEND}客户端/
	 *            {@link MessageInfo#MSG_FOR_RECEIVE}服务端
	 */
	public void saveAllMsg(String deviceToken, boolean isClearAfterSave, int from);

	/**
	 * 设置消息更新接口
	 *
	 * @param listener
	 */
	public void setOnMessageChangedListener(OnMessageChangedListener listener);

	/**
	 * 获取消息更新接口,消息新增/清除所有/清除部分缓存/是否保存都会通过此接口回调
	 *
	 * @return
	 */
	public OnMessageChangedListener getOnMessageChangedListener();

	/**
	 * 获取消息接收处理监听,此监听接口用于处理接收到的所有数据
	 *
	 * @return
	 */
	public OnMsgActionListener getOnMsgActionListener();
}