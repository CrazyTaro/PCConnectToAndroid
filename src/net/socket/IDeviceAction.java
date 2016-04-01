package net.socket;

import net.interfaces.OnInfoChangedListener;

/**
 * Created by taro on 16/3/17.
 */
public interface IDeviceAction {

    /**
     * 设置消息更新监听接口
     * @param listener
     */
    public void setOnInfoChangedListener(OnInfoChangedListener listener);

    /**
     * 获取消息更新监听接口
     * @return
     */
    public OnInfoChangedListener getOnInfoChangedListener();
}
