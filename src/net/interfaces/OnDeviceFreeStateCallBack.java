package net.interfaces;

/**
 * Created by taro on 16/3/16.
 * 设备空闲状态更新回调接口
 */
public interface OnDeviceFreeStateCallBack {
    /**
     * 设备空闲状态更改回调接口
     * @param deviceToken
     * @param isFree
     */
    public void onDeviceFreeStateChanged(String deviceToken, boolean isFree);
}
