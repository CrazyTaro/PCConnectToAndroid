package net.socket;

import net.manager.IMsgParseMgrAction;
import net.interfaces.OnConnectionChangedListener;

/**
 * Created by taro on 16/3/1.
 */
public interface IClientAction {
	/**
	 * 获取当前的客户端标识,此标识应该是来自服务端的
	 *
	 * @return
	 */
	public String getTag();

	/**
	 * 发送消息,当消息为null或者空字符串时,将不会被发送; 此方法为将消息存储到消息发送队列中,实际的发送操作将由后台的发送线程处理
	 *
	 * @param output
	 */
	public void sendMsg(String output);

	/**
	 * 关闭客户端连接
	 */
	public void closeClient();

	/**
	 * 启动心跳检测连接(每固定时间发送一次心跳消息),此方法请不要在任何可能发生阻塞的线程中调用
	 */
	public void startHeartbeatConnect();

	/**
	 * 当前客户端后台监听线程是否存活
	 *
	 * @return
	 */
	public boolean isClientAlive();

	/**
	 * 设置消息回调监听
	 *
	 * @param action
	 */
	public void setIMsgParseAction(IMsgParseMgrAction action);

	/**
	 * 获取消息回调监听
	 *
	 * @return
	 */
	public IMsgParseMgrAction getIMsgParseAction();

	/**
	 * 设置连接状态改变监听接口
	 *
	 * @param listener
	 */
	public void setOnConnectionChangedListener(OnConnectionChangedListener listener);

	/**
	 * 获取连接状态改变监听接口
	 *
	 * @return
	 */
	public OnConnectionChangedListener getOnConnectionChangedListener();
}
