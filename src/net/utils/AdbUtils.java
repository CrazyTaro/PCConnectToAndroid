package net.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.interfaces.OnBreakReadCmdResultListener;

public class AdbUtils {
	private String ADB_PATH = "/Users/taro/Library/Android/sdk/platform-tools/";

	/**
	 * adb命令:查询与PC连接的设备
	 */
	public static final String ADB_CMD_DEVICE = "adb devices";
	/**
	 * adb命令:格式化设置设备转发端口
	 */
	public static final String ADB_CMD_FORWARD_FORMAT = "adb -s %s forward tcp:%s tcp:%s";
	/**
	 * adb命令:单个终端连接转发端口
	 */
	public static final String ADB_CMD_FORWARD_SIMPLE = "adb forward tcp:%s tcp:%s";
	/**
	 * adb命令:移除当前所有转发端口的映射
	 */
	public static final String ADB_CMD_FORWARD_REMOVE = "adb forward --remove-all";
	/**
	 * adb命令:单个终端广播(不带参数)
	 */
	public static final String ADB_CMD_BROADCAST_SIMPLE = "adb shell am broadcast -a %s ";
	/**
	 * adb命令:格式化多终端广播(不带参数)
	 */
	public static final String ADB_CMD_BROADCAST_FORMAT = "adb -s %s shell am broadcast -a %s ";
	/**
	 * adb命令:单击指定坐标
	 */
	public static final String ADB_CMD_INPUT = "adb shell input tap %s %s";
	/**
	 * 默认字符串编码
	 */
	public static final String DEFAULT_CHARSET = "UTF-8";
	/**
	 * 默认PC adb程序路径
	 */
	public static final String DEFAULT_ADB_PATH = "/Users/taro/Library/Android/sdk/platform-tools/";

	/**
	 * 广播action:启动服务
	 */
	public static final String ACTION_START_SERVICE = "bestapp.us.orderbyebye.START_SOCKET";
	/**
	 * 广播action:中止服务
	 */
	public static final String ACTION_STOP_SERVICE = "bestapp.us.orderbyebye.STOP_SOCKET";

	public AdbUtils(String adbPath) {
		if (CommonUtils.isEmptyString(adbPath)) {
			throw new RuntimeException("路径不可为空!");
		}
		ADB_PATH = adbPath;
	}

	/**
	 *  将本地端口的数据转到指定的端口,用于处理android与PC使用USB连接时的数据通信
	 *
	 * @param deviceToken
	 *            设备标识，若只有一台设备，可置为null
	 * @param localPort
	 *            本地端口
	 * @param adbPort
	 *            android使用的adb端口
	 * @return
	 */
	public String forwardPort(String deviceToken, String localPort, String adbPort) {
		if (CommonUtils.isEmptyString(localPort) || CommonUtils.isEmptyString(adbPort)) {
			return "";
		}
		try {
			String command = null;
			// 检测是否需要指定设备进行设置端口
			if (CommonUtils.isEmptyString(deviceToken)) {
				// adb forward tcp:端口 tcp:端口
				command = String.format(ADB_CMD_FORWARD_SIMPLE, localPort, adbPort);
			} else {
				// adb -s 设备 forward tcp:端口 tcp:端口
				command = String.format(ADB_CMD_FORWARD_FORMAT, deviceToken, localPort, adbPort);
			}
			command = ADB_PATH + command;
			return CommonUtils.execCommand(command, false, DEFAULT_CHARSET, null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			CommonUtils.logError(e);
			return "";
		}
	}

	/**
	 * 移除所有的转发接口
	 *
	 * @return
	 */
	public String removeAllForwardPort() {
		return ADB_PATH + ADB_CMD_FORWARD_REMOVE;
	}

	/**
	 * 获取与PC连接的设备标识
	 *
	 * @return 设备标识list
	 */
	public List<String> getDevices() {
		// 执行命令
		String resultStr = CommonUtils.execCommand(ADB_PATH + ADB_CMD_DEVICE, true, DEFAULT_CHARSET, null);
		System.out.println("设备信息:" + resultStr);
		// 匹配正则
		String regPattern = "(\\w+)\\s*device";
		Pattern p = Pattern.compile(regPattern);
		Matcher matcher = p.matcher(resultStr);
		List<String> devices = new ArrayList<String>(10);
		String deviceToken = null;
		// 查找匹配
		while (matcher.find()) {
			deviceToken = matcher.group(1);
			// 排除无效错误信息
			if (deviceToken != null && !deviceToken.equals("of")) {
				devices.add(deviceToken);
			}
			System.out.println("匹配设备:" + deviceToken);
		}
		return devices;
	}

	/**
	 * 发送广播Intent
	 *
	 * @param deviceToken
	 *            设备标识
	 * @param action
	 *            广播action,action为null或者空字符串,直接返回
	 * @param strExtra
	 *            广播String Extra,若不需要发送参数,置为null
	 * @param intExtra
	 *            广播int Extra,若不需要发送参数,置为null
	 * @param booleanExtra
	 *            广播boolean Extra,若不需要发送参数,置为null
	 * @return
	 */
	public String broadcast(String deviceToken, String action, Map<String, String> strExtra,
			Map<String, Integer> intExtra, Map<String, Boolean> booleanExtra) {
		if (CommonUtils.isEmptyString(action)) {
			return "";
		}
		// 默认为指定设备的发送广播
		String command = ADB_CMD_BROADCAST_FORMAT;
		StringBuilder sBuilder = new StringBuilder();
		// 根据设备标识确定执行命令为简单版还是格式化命令
		if (CommonUtils.isEmptyString(deviceToken)) {
			command = String.format(ADB_CMD_BROADCAST_SIMPLE, action);
		} else {
			command = String.format(ADB_CMD_BROADCAST_FORMAT, deviceToken, action);
		}
		sBuilder.append(ADB_PATH);
		sBuilder.append(command);
		// 添加extra参数
		// 添加String extra
		if (strExtra != null && strExtra.size() > 0) {
			sBuilder.append(" --es ");
			for (String key : strExtra.keySet()) {
				sBuilder.append(key);
				sBuilder.append(" ");
				sBuilder.append("\"");
				sBuilder.append(strExtra.get(key));
				sBuilder.append("\"");
			}
		}
		// 添加int extra
		if (intExtra != null && intExtra.size() > 0) {
			sBuilder.append(" --ei ");
			for (String key : intExtra.keySet()) {
				sBuilder.append(key);
				sBuilder.append(" ");
				sBuilder.append(intExtra.get(key));
			}
		}
		// 添加boolean extra
		if (booleanExtra != null && booleanExtra.size() > 0) {
			sBuilder.append(" --ez ");
			for (String key : booleanExtra.keySet()) {
				sBuilder.append(key);
				sBuilder.append(" ");
				sBuilder.append(booleanExtra.get(key));
			}
		}
		// 创建命令
		command = sBuilder.toString();
		// 执行命令
		return CommonUtils.execCommand(command, true, DEFAULT_CHARSET, new OnBreakReadCmdResultListener() {

			@Override
			public boolean onBreadReadCmdResult(String result) {
				// 广播的中止回调判断,出现completed则不再监听返回的信息
				if (result != null && result.contains("completed")) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	/**
	 * 单击指定坐标命令
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	public String inputTap(int x, int y) {
		String command = ADB_PATH + String.format(ADB_CMD_INPUT, String.valueOf(x), String.valueOf(y));
		return CommonUtils.execCommand(command, false, DEFAULT_CHARSET, null);
	}
}
