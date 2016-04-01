package net.manager;

import net.interfaces.OnMessageChangedListener;
import net.interfaces.OnMsgActionListener;
import net.socket.SocketConnect;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理
 */
public class MessageManager implements IMessageMgrAction, OnMsgActionListener {
	// 消息更新监听事件
	private OnMessageChangedListener mOnMessageChangedListener = null;
	private Map<String, MessageInfo> mMsgMap = null;

	public MessageManager() {
		mMsgMap = new HashMap<String, MessageInfo>();
	}

	/**
	 * 获取指定设备的消息处理对象,当设备不存在时,创建以此设备为标识的消息处理对象
	 *
	 * @param deviceToken
	 * @return 返回的必定不是null, 或者是已存在的消息对象, 或者是空的消息对象
	 */
	private MessageInfo checkDeviceToken(String deviceToken) {
		MessageInfo info = mMsgMap.get(deviceToken);
		// 标识不存在,直接创建新的消息对象
		if (info == null) {
			info = new MessageInfo(deviceToken);
			mMsgMap.put(deviceToken, info);
		}
		return info;
	}

	@Override
	public MessageInfo getMsgInfoByDevice(String deviceToken) {
		return mMsgMap.get(deviceToken);
	}

	@Override
	public void appendMsg(String deviceToken, String msg, int from) {
		MessageInfo info = checkDeviceToken(deviceToken);
		// 添加到消息对象中
		info.appendMsg(msg, from);
		if (mOnMessageChangedListener != null) {
			// 通知消息添加
			mOnMessageChangedListener.onAppendMsg(deviceToken, msg, from);
			int cleanCount=info.cleanMsg(from);
            if(cleanCount>0) {
                // 通知尝试清除多余消息
                mOnMessageChangedListener.onCleanMsg(deviceToken, cleanCount, from);
            }
		}
	}

	@Override
	public void clearMsg(String deviceToken, int from) {
		boolean isSaveMsg = false;
		// 通知是否清除消息前进行保存操作
		if (mOnMessageChangedListener != null) {
			isSaveMsg = mOnMessageChangedListener.isSaveMsgBeforeClearMsg(deviceToken, from);
		}
		// 清除消息
		MessageInfo info = checkDeviceToken(deviceToken);
		info.clearMsg(isSaveMsg, from);
	}

	@Override
	public void saveAllMsg(String deviceToken, boolean isClearAfterSave, int from) {
		MessageInfo info = checkDeviceToken(deviceToken);
		info.saveAllMsg(from);
		if (isClearAfterSave) {
			info.cleanMsg(from);
		}
	}

	@Override
	public void setOnMessageChangedListener(OnMessageChangedListener listener) {
		mOnMessageChangedListener = listener;
	}

	@Override
	public OnMessageChangedListener getOnMessageChangedListener() {
		return mOnMessageChangedListener;
	}

	@Override
	public OnMsgActionListener getOnMsgActionListener() {
		return this;
	}

	@Override
	public void clearMsgAllDevice(int from) {
		for (String device : mMsgMap.keySet()) {
			this.clearMsg(device, from);
		}
	}

	@Override
	public void saveMsgAllDevice(int from) {
		for (String device : mMsgMap.keySet()) {
			this.saveAllMsg(device, false, from);
		}
	}

	@Override
	public void onReceiveMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg) {
		this.appendMsg(deviceToken, msg, MessageInfo.MSG_FOR_RECEIVE);
	}

	@Override
	public void onSendMsg(SocketConnect connector, String deviceToken, int socketWorkType, String msg) {
		this.appendMsg(deviceToken, msg, MessageInfo.MSG_FOR_SEND);
	}
}
