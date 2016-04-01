package net.manager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import net.utils.CommonUtils;

/**
 * 消息处理对象,管理单个设备的客户端及服务端的所有消息
 */
public class MessageInfo {
	/**
	 * 发送的消息
	 */
	public static final int MSG_FOR_SEND = 0;
	/**
	 * 接收的消息
	 */
	public static final int MSG_FOR_RECEIVE = 1;

	/**
	 * 默认的缓存消息数量
	 */
	private int MSG_CACHE_COUNT = 200;
	private String mDeviceToken = "";
	private List<String> mClientMsgList = null;
	private List<String> mServerMsgList = null;

	private ReentrantLock mMsgLock = null;

	public MessageInfo(String deviceToken) {
		mClientMsgList = new LinkedList<String>();
		mServerMsgList = new LinkedList<String>();
		mDeviceToken = deviceToken;
		mMsgLock = new ReentrantLock();
	}

	/**
	 * 添加消息
	 * @param msgList
	 * @param msg
     */
	private void addMsg(List<String> msgList, String msg) {
		msgList.add(msg);
	}

	/**
	 * 移除消息
	 * @param msgList
	 * @param index
     * @return
     */
	private String removeMsg(List<String> msgList, int index) {
		return msgList.remove(index);
	}

	/**
	 * 根据消息来源获取对应的消息列表(客户端或者服务端的)
	 *
	 * @param from
	 * @return
	 */
	public List<String> getMsgListByFrom(int from) {
		List<String> oldMsgList = null;
		if (MSG_FOR_SEND == from) {
			oldMsgList = mClientMsgList;
		} else if (MSG_FOR_RECEIVE == from) {
			oldMsgList = mServerMsgList;
		}
		return oldMsgList;
	}

	/**
	 * 获取此消息处理对象的设备标识
	 *
	 * @return
	 */
	public String getDeviceToken() {
		return mDeviceToken;
	}

	/**
	 * 添加新消息
	 *
	 * @param msg
	 *            消息内容
	 * @param from
	 * @return 返回消息列表添加新消息后的总数量;若消息为null或者是空字符串,则返回-1
	 */
	public int appendMsg(String msg, int from) {
		if (CommonUtils.isEmptyString(msg)) {
			return -1;
		} else {
			int size = 0;
			List<String> msgList = getMsgListByFrom(from);
			if (msgList != null) {
				// 新消息总是添加在最前
				addMsg(msgList, msg);
				size = msgList.size();
			}
			return size;
		}
	}

	/**
	 * 清除消息
	 *
	 * @param isSaveMsg
	 *            是否在清除前保存消息
	 * @param from
	 */
	public void clearMsg(boolean isSaveMsg, int from) {
		if (isSaveMsg) {
			saveAllMsg(from);
		}
		List<String> msgList = getMsgListByFrom(from);
		if (msgList != null) {
			msgList.clear();
		}
	}

	/**
	 * 保存所有消息
	 *
	 * @param from
	 */
	public void saveAllMsg(int from) {
		List<String> oldMsgList = getMsgListByFrom(from);
		int saveCount = oldMsgList == null ? 0 : oldMsgList.size();

		writeToFile(oldMsgList, saveCount, from);
	}

	/**
	 * 清除缓存消息
	 *
	 * @param from
	 * @return 返回被清除(保存到文件中)的缓存消息数量
	 */
	public int cleanMsg(int from) {
		int saveCount = 0;
		List<String> oldMsgList = getMsgListByFrom(from);
		// 超过默认的缓存消息数量上限,进行保存
		if (oldMsgList != null && oldMsgList.size() > MSG_CACHE_COUNT) {
			int remainCount = (int) (MSG_CACHE_COUNT * 0.2);
			saveCount = oldMsgList.size() - remainCount;
			LinkedList<String> newMsgList = new LinkedList<String>();
			// **************************************************
			// old消息列表中的前部分消息为最新的消息
			// 后部分消息为旧消息
			// **************************************************
			if (remainCount < saveCount) {
				for (int i = 0; i < remainCount; i++) {
					// 将old消息列表的前remainCount保存到new消息列表中
					// new消息列表将保存下来,old消息列表将被保存到文件中
					newMsgList.add(newMsgList.size(), oldMsgList.get(0));
					oldMsgList.remove(0);
				}
			} else {
				List<String> tempList = null;
				for (int i = 0; i < saveCount; i++) {
					// 将old消息列表的最后saveCount保存到new消息列表中
					// 此时new消息列表中保存的消息将保存到文件中(暂存)
					newMsgList.add(oldMsgList.remove(oldMsgList.size() - 1));
				}
				// 将new消息列表引用转到old消息列表(用于保存到文件)
				tempList = newMsgList;
				newMsgList = (LinkedList<String>) oldMsgList;
				// 将old消息列表引用转到new消息列表(缓存部分)
				oldMsgList = tempList;
			}
			// 保存消息
			writeToFile(oldMsgList, saveCount, from);

			if (MSG_FOR_SEND == from) {
				mClientMsgList = newMsgList;
			} else if (MSG_FOR_RECEIVE == from) {
				mServerMsgList = newMsgList;
			}
		}
		return saveCount;
	}

	/**
	 * 将消息写入文件保存
	 *
	 * @param oldMsgList
	 *            消息列表
	 * @param count
	 *            保存数量
	 * @param from
	 */
	public void writeToFile(List<String> oldMsgList, int count, int from) {
		//TODO: 将指定量的消息写到文件中保存,未确定要保存的形式及类型...
	}
}
