package net.interfaces;

/**
 * Created by taro on 16/3/3.
 * <p/>
 * 中止执行命令时读取数据的回调接口
 */

public interface OnBreakReadCmdResultListener {
	/**
	 * 中止执行命令时读取数据的回调判断
	 *
	 * @param result
	 *            当次读取的字符串结果,可能为"",即没有读取到
	 * @return 若要中止读取数据, 返回true, 否则返回false
	 */
	public boolean onBreadReadCmdResult(String result);
}
