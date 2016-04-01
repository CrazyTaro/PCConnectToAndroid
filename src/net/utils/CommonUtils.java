package net.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.interfaces.OnBreakReadCmdResultListener;

public class CommonUtils {
	/**
	 * 字符串进行MD5
	 *
	 * @param s
	 * @return
	 */
	public static String md5(String s) {
		try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(s.getBytes());

			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				if ((b & 0xFF) < 0x10)
					hex.append("0");
				hex.append(Integer.toHexString(b & 0xFF));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Huh, MD5 should be supported?", e);
		}

	}

	/**
	 * 检测文本是否为null或者空字符串
	 *
	 * @param input
	 * @return
	 */
	public static boolean isEmptyString(String input) {
		if (input == null || input.equals("")) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 发送字符串到流,该方法为阻塞方法,直到发送完该字符串为止
	 *
	 * @param out
	 *            输出流
	 * @param output
	 *            输出字符串
	 * @param isCloseStream
	 *            是否关闭该流,在发送成功或者失败之后
	 * @return
	 */
	public static boolean sendStringToOutputStream(OutputStream out, String output, boolean isCloseStream)
			throws IOException {
		if (isEmptyString(output) || out == null) {
			return false;
		} else {
			// 获取字符串对应的字节内容
			byte[] buffer = output.getBytes(Charset.forName("UTF-8"));
			try {
				// 输出到流
				out.write(buffer);
				out.flush();
				return true;
			} finally {
				// 尝试关闭流
				if (out != null && isCloseStream) {
					try {
						out.close();
					} catch (IOException e) {
						logError(e);
					}
				}
			}
		}
	}

	/**
	 * 根据输入的流将内容转成指定编码的字符串,该方法为阻塞方法,直到读取数据完毕或者换行才会退出循环
	 *
	 * @param stream
	 *            输入流
	 * @param buffer
	 * @param bufferContainer
	 * @param isCloseStream
	 *            是否关闭该流,在发送成功或者失败之后 @return
	 */
	public static String readOnceStrFromInputStream(InputStream stream, byte[] buffer, StringBuilder bufferContainer,
			boolean isCloseStream) {
		if (stream == null) {
			return "";
		} else {
			if (bufferContainer == null) {
				bufferContainer = new StringBuilder();
			} else {
				// 清除原来所有的字符
				bufferContainer.delete(0, bufferContainer.length());
			}
			// 创建缓冲区
			if (buffer == null) {
				buffer = new byte[1204];
			}
			while (true) {
				try {
					// 读取字节
					int readSize = stream.read(buffer);
					if (readSize > 0) {
						// 将字节转成字符串
						bufferContainer.append(new String(buffer, 0, readSize, "UTF-8"));
					}
					if (readSize < buffer.length || stream.available() <= 0) {
						break;
					}
				} catch (IOException e) {
					logError(e);
					break;
				}
			}
			// 若需要关闭流,则关闭
			if (stream != null && isCloseStream) {
				try {
					stream.close();
				} catch (IOException e) {
					logError(e);
				}
			}
			return bufferContainer.toString();
		}
	}

	/**
	 * 多次读取数据,直到达到停止读取的标识或者是已读取到数据结尾时结束
	 *
	 * @param in
	 * @param isCloseStream
	 *            是否读取完关闭流
	 * @param listener
	 *            判断是否已经达到
	 * @return
	 */
	public static String readStrFromInputStream(InputStream in, boolean isCloseStream,
			OnBreakReadCmdResultListener listener) {
		StringBuilder resultBuilder = new StringBuilder();
		StringBuilder tempBuilder = new StringBuilder();
		byte[] buffer = new byte[1024];
		// 存在退出监听事件,读取数据
		long startTime = System.currentTimeMillis();
		while (true) {
			String singleStr = readOnceStrFromInputStream(in, buffer, tempBuilder, false);
			resultBuilder.append(singleStr);
			if ((listener != null && listener.onBreadReadCmdResult(singleStr)) || singleStr.equals("")) {
				break;
			} else {
				// 每次读取超过10秒将会自动退出
				if ((System.currentTimeMillis() - startTime) > 10000) {
					System.out.println("读取数据超时");
					break;
				}
			}
		}
		if (isCloseStream) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultBuilder.toString();
	}

	/**
	 * 执行命令,读取数据命令最多不能超过10秒,否则作超时处理
	 *
	 * @param command
	 *            命令
	 * @param isNeedToGetResult
	 *            是否需要获取返回的结果字符串
	 * @param charsetName
	 *            返回结果字符串需要使用的编码
	 * @param listener
	 *            多次读取数据时中止读取操作的回调,若此参数为null,只最多只读取一次数据
	 * @return
	 */
	public static String execCommand(String command, boolean isNeedToGetResult, String charsetName,
			OnBreakReadCmdResultListener listener) {
		if (CommonUtils.isEmptyString(command)) {
			return "";
		}
		try {
			System.out.println("执行命令:" + command);
			Process process = Runtime.getRuntime().exec(command);
			InputStream in = process.getInputStream();
			if (isNeedToGetResult) {
				// 不存在退出监听事件,只读取消息一次
				if (listener == null) {
					return readOnceStrFromInputStream(in, null, null, true);
				} else {
					return readStrFromInputStream(in, true, listener);
				}
			} else {
				return "";
			}
		} catch (Exception e) {
			logError(e);
			return "";
		}
	}

	/**
	 * 打印异常消息
	 *
	 * @param e
	 */
	public static void logError(Exception e) {
		if (e != null) {
			e.printStackTrace();
			System.err.println("出错:" + e.getMessage());
		}
	}

	/**
	 * 打印错误消息
	 *
	 * @param error
	 */
	public static void logError(String error) {
		System.err.println("出错:" + error);
	}

	/**
	 * 打印消息
	 *
	 * @param msg
	 */
	public static void logInfo(String msg) {
		System.out.println("info: " + msg);
	}
}
