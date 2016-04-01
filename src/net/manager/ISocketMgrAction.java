package net.manager;

import net.interfaces.OnConnectionChangedListener;
import net.interfaces.OnInfoChangedListener;
import net.socket.DeviceInfo;
import net.socket.SocketConnect;
import net.utils.AdbUtils;

import java.util.List;
import java.util.Set;

/**
 * Created by taro on 16/3/4. 客户端管理操作接口
 */
public interface ISocketMgrAction {

    /**
     * 更新连接终端的socket连接,已创建连接的将不会重新创建;未创建的连接将会创建新连接; 创建连接仅仅只是创建了连接对象,并不会自动建立连接
     */
    public void updateSocketClientConnect();

    /**
     * 尝试连接所有已创建的连接
     *
     * @param msgParseAction      消息接收对象
     * @param connectListener     设备连接状态监听
     * @param infoChangedListener
     */
    public void connectAll(IMsgParseMgrAction msgParseAction, OnConnectionChangedListener connectListener, OnInfoChangedListener infoChangedListener);

    /**
     * 指定与某设备关联的socket对象进行连接
     *
     * @param deviceToken         设备标识
     * @param msgParseAction      消息接收对象
     * @param connectListener     设备连接状态监听
     * @param infoChangedListener
     * @return
     */
    public boolean connectSocket(String deviceToken, IMsgParseMgrAction msgParseAction,
                                 OnConnectionChangedListener connectListener, OnInfoChangedListener infoChangedListener);

    /**
     * 连接所有已创建的连接,使用预设的处理及监听接口
     */
    public void connectAllUsePresetListener();

    /**
     * 指定与某设备关系的socket对象进行连接,使用预设的处理及监听接口
     *
     * @param deviceToekn
     */
    public void connectSocketUsePresetListener(String deviceToekn);

    /**
     * 关闭所有已存在的连接
     */
    public void closeAll();

    /**
     * 关闭指定设备关联的连接
     *
     * @param deviceToken
     * @return
     */
    public boolean closeSocket(String deviceToken);

    /**
     * 更新设备的标识token
     */
    public void updateDeviceToken();

    /**
     * 指定某个设备更新设备信息更新监听接口
     *
     * @param deviceToken
     * @param listener
     */
    public void setOnInfoChangedListener(String deviceToken, OnInfoChangedListener listener);

    /**
     * 指定某个设备更新连接状态改变监听接口
     *
     * @param deviceToken
     * @param listener
     */
    public void setOnConnectionChangedListener(String deviceToken, OnConnectionChangedListener listener);

    /**
     * 指定某个设备更新消息监听接口
     *
     * @param deviceToken
     * @param msgParseAction
     */
    public void setIMsgParseAction(String deviceToken, IMsgParseMgrAction msgParseAction);

    /**
     * 设置预设的所有设备信息更新监听接口
     *
     * @param listener
     */
    public void presetOnInfoChangedListener(OnInfoChangedListener listener);

    /**
     * 设置预设的所有设备连接状态改变监听接口
     *
     * @param msgParseAction
     */
    public void presetIMsgParseAction(IMsgParseMgrAction msgParseAction);

    /**
     * 设置预设的所有设备消息监听接口
     *
     * @param listener
     */
    public void presetOnConnectionChangedListener(OnConnectionChangedListener listener);


    /**
     * 获取预设的消息处理接口
     *
     * @return
     */
    public IMsgParseMgrAction getPresetIMsgParseAction();

    /**
     * 获取预设的消息状态监听接口
     *
     * @return
     */
    public OnInfoChangedListener getPresetOnInfoChangedListener();

    /**
     * 获取预设的连接状态更新监听接口
     *
     * @return
     */
    public OnConnectionChangedListener getPresetOnConnectChangedListener();

    /**
     * 获取当前已检测到的设备token,使用此方法不会更新设备token,只会返回最后一次更新的结果
     *
     * @return
     */
    public List<String> getUSBDeviceToken();

    /**
     * 返回管理的socket中所有的deviceToken,
     * 此方法与{@link #getUSBDeviceToken()}有区别,上述方法返回的是电脑检测到的deviceToken,此方法返回的是当前实际创建了连接的deviceToken(未创建连接或者不存在连接的device将不在此返回值内)
     *
     * @return
     */
    public Set<String> getConnectedDeviceToken();

    /**
     * 重置adb工具类
     *
     * @param adbUtils
     */
    public void resetAdbUtils(AdbUtils adbUtils);

    /**
     * 获取指定device_token的设备连接是否存活中
     *
     * @param deviceToken
     * @return 当该设备未建立连接或者当前连接已经断开时, 返回false
     */
    public boolean isSocketConnectAlive(String deviceToken);

    /**
     * 根据某个设备的标识获取连接对象
     *
     * @param deviceToken
     * @return
     */
    public SocketConnect getSocketByDeviceToken(String deviceToken);

    /**
     * 获取连接的设备信息
     *
     * @param deviceToken
     * @return
     */
    public DeviceInfo getSocketDeviceInfo(String deviceToken);

    /**
     * 发送消息到指定的设备
     *
     * @param deviceToken
     * @param msg
     */
    public void sendMsgToSocket(String deviceToken, String msg);

    /**
     * 发送消息给所有的设备
     *
     * @param msg
     */
    public void sendMsgToAll(String msg);

    /**
     * 消除当前所有操作所造成的影响,包括: 清除adb转发端口
     */
    public void cleanEnvironment();

    /**
     * 开启指定设备的远程服务,需要指定远程服务监听的端口(对于客户端连接,不需要手动开启服务,创建连接时会自动尝试开启服务)
     *
     * @param serverPort
     */
    public void startService(String deviceToken, int serverPort);

    /**
     * 停止指定设备的远程服务此方法会停止该设备的连接; 因为服务一旦停止了连接没有存在的意义
     */
    public void stopService(String deviceToken);

    /**
     * 停止全部设备的远程服务,此方法会停止当前所有的连接并清除转发端口; 因为服务一旦停止了连接没有存在的意义
     */
    public void stopAllService();
}
