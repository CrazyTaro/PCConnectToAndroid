package net.interfaces;


import net.manager.IMsgParseMgrAction;
import net.socket.DeviceInfo;

/**
 * Created by taro on 16/2/29. socket服务端提供的操作接口
 */
public interface IServerAction {
    /**
     * 尝试启动服务,当服务已启动时,将不会做任何操作
     */
    public void tryStartServer();

    /**
     * 重新启动服务,不管当前服务的状态如何,直接关闭当前的服务并重新启动
     */
    public void restartServer();

    /**
     * 关闭服务
     */
    public void closeServer();

    /**
     * 启动心跳检测发送,间隔性发送确认连接心跳
     */
    public void startHeartbeat();

    /**
     * 服务是否存在
     *
     * @return
     */
    public boolean isServerAlive();

    /**
     * 获取连接的端口
     *
     * @return
     */
    public int getConnectPort();

    /**
     * 获取设备信息
     *
     * @return
     */
    public DeviceInfo getDeviceInfo();

    /**
     * 设置消息处理接口
     *
     * @param msgParseAction
     */
    public void setIMsgParseAction(IMsgParseMgrAction msgParseAction);

    /**
     * 获取消息处理接口
     *
     * @return
     */
    public IMsgParseMgrAction getIMsgParseAction();

    /**
     * 设置设备连接状态监听事件
     *
     * @param listener
     */
    public void setOnConnectChangedListener(OnConnectionChangedListener listener);

    /**
     * 获取设备连接状态监听事件
     *
     * @return
     */
    public OnConnectionChangedListener getOnConnectionChangedListener();

    /**
     * 发送消息
     *
     * @param output
     */
    public void sendMsg(String output);
}
