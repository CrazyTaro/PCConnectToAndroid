package net.interfaces;

import java.io.InputStream;

/**
 * Created by taro on 16/3/10.
 */
public interface HttpCallBack {
	public void onConnectTimeout();

	public void onConnectFail();

	public void onConnected(InputStream in);
}
