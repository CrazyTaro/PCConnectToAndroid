package net.interfaces;

/**
 * 消息更新监听回调
 */
public interface OnMessageChangedListener {
	/**
	 * 新添加消息事件
	 *
	 * @param deviceToken
	 *            设备标识
	 * @param msg
	 *            消息内容
	 * @param from
	 *            消息来源对象:{@link MessageInfo#MSG_FOR_SEND}客户端/
	 *            {@link MessageInfo#MSG_FOR_RECEIVE}服务端
	 */
	public void onAppendMsg(String deviceToken, String msg, int from);

	/**
	 * 清除消息事件
	 *
	 * @param deviceToken
	 * @param saveCount
	 *            清除时保存到文件中的消息缓存数量
	 * @param from
	 *            消息来源对象:{@link MessageInfo#MSG_FOR_SEND}客户端/
	 *            {@link MessageInfo#MSG_FOR_RECEIVE}服务端
	 */
	public void onCleanMsg(String deviceToken, int saveCount, int from);

	/**
	 * 清除消息缓存前的确认保存事件
	 *
	 * @param deviceToken
	 * @param from
	 *            消息来源对象:{@link MessageInfo#MSG_FOR_SEND}客户端/
	 *            {@link MessageInfo#MSG_FOR_RECEIVE}服务端
	 * @return
	 */
	public boolean isSaveMsgBeforeClearMsg(String deviceToken, int from);

}
