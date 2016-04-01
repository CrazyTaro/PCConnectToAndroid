package net.manager;


import net.interfaces.OnDeviceFreeStateCallBack;
import net.interfaces.OnMsgActionListener;
import net.socket.SocketConnect;

/**
 * Created by taro on 16/3/8.
 * 消息解析接口
 */
public interface IMsgParseMgrAction {
    /**
     * 处理接收到的消息/命令解析,当需要将此消息存储到消息列表时,返回true,否则返回false
     *
     * @param connector
     * @param deviceToken    来自的设备token
     * @param socketWorkType socket对象运行在客户端还是服务端<br/>
     *                       {@link net.socket.DeviceInfo#SOCKET_IN_CLIENT}<br/>
     *                       {@link net.socket.DeviceInfo#SOCKET_IN_SERVER}
     * @param msg            接收到的消息/命令
     * @return
     */
    public void onParseReceivedMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg);

    /**
     * 处理发送的消息/命令解析,当需要将此消息存储到消息列表时,返回true,否则返回false
     *
     * @param connector
     * @param deviceToken    来自的设备token
     * @param socketWorkType socket对象运行在客户端还是服务端<br/>
     *                       {@link net.socket.DeviceInfo#SOCKET_IN_CLIENT}<br/>
     *                       {@link net.socket.DeviceInfo#SOCKET_IN_SERVER}
     * @param msg            发送的消息/命令
     * @return
     */
    public void onParseSentMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg);

    /**
     * 设置消息接收发送处理监听接口,主要用于保存数据
     *
     * @param listener
     */
    public void setOnMsgActionListener(OnMsgActionListener listener);

    /**
     * 获取消息接收发送处理监听接口
     *
     * @return
     */
    public OnMsgActionListener getOnMsgActionListener();

    /**
     * 设置恩设备空闲状态回调事件,此接口只会被回调一次,并且如果同一个token有其它的接口将会被替换
     *
     * @param callBack 临时用于回调的接口,提供给用户回调获取数据
     */
    public void setOnDeviceFreeStateCallBack(String deviceToken, OnDeviceFreeStateCallBack callBack);

}
