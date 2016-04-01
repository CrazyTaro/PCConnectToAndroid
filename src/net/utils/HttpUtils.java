package net.utils;

import net.interfaces.HttpCallBack;

import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 此类提供给PC端使用,ANDROID 端不使用
 */
public class HttpUtils {
    /**
     * POST请求方式
     */
    public static final int POST = 120;
    /**
     * GET请求方式
     */
    public static final int GET = 119;

    public static final int REQUEST_FAIL = 110;
    public static final int REQUEST_TIME_OUT = 119;
    public static final int REQUEST_SUCCESS = 120;

    private static HttpUtils mHttpUtils = null;
    // 线程池
    private ExecutorService mExecutor = null;

    /**
     * 获取Http请求工具类
     */
    public synchronized static HttpUtils getInstance() {
        if (mHttpUtils == null) {
            mHttpUtils = new HttpUtils();
        }
        return mHttpUtils;
    }

    private HttpUtils() {
        mExecutor = Executors.newFixedThreadPool(2);
    }

    // 请求线程执行代码
    private class ExecuteRunnable implements Runnable {
        private String mUrl = null;
        private HttpCallBack mCallBack = null;
        private String mParamsStr = null;
        private int mRequestType = HttpUtils.GET;
        private Map<String, String> mRequestPropertyMap = null;

        /**
         * 创建请求执行线程代码
         *
         * @param url                请求Url,不可带参数
         * @param params             请求参数字符串,{@link #getParamsFromMap(Map)}
         * @param requestType        请求方式,{@link #GET}/{@link #POST}
         * @param requestPropertyMap
         * @param callBack           请求回调 {@link HttpCallBack}
         */
        public ExecuteRunnable(String url, String params, int requestType, Map<String, String> requestPropertyMap, HttpCallBack callBack) {
            this.mUrl = url;
            this.mParamsStr = params;
            this.mCallBack = callBack;
            this.mRequestType = requestType;
            this.mRequestPropertyMap = requestPropertyMap;
        }

        @Override
        public void run() {
            try {
                CommonUtils.logInfo("开始 post 请求:" + mParamsStr);
                String requestUrl = null;
                URLConnection conn = null;
                // GET请求拼接请求字符串
                if (this.mRequestType == HttpUtils.GET) {
                    requestUrl = this.mUrl + "?" + this.mParamsStr;
                    URL realUrl = new URL(requestUrl);
                    // 打开和URL之间的连接
                    conn = realUrl.openConnection();
                    // POST请求,只需要URL本身即可
                } else if (this.mRequestType == HttpUtils.POST) {
                    requestUrl = this.mUrl;
                    URL realUrl = new URL(requestUrl);
                    // 打开和URL之间的连接
                    conn = realUrl.openConnection();
                    // POST必须打开input/output
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                }
                // 设置通用的请求属性
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                if (mRequestPropertyMap != null && mRequestPropertyMap.size() > 0) {
                    for (Map.Entry<String, String> entry : mRequestPropertyMap.entrySet()) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                conn.setConnectTimeout(10000);
                // 建立实际的连接
                conn.connect();
                // POST请求,将参数流写到对方连接
                if (this.mRequestType == HttpUtils.POST) {
                    // 获取URLConnection对象对应的输出流
                    PrintWriter out = new PrintWriter(conn.getOutputStream());
                    // 发送请求参数
                    out.print(this.mParamsStr);
                    // flush输出流的缓冲
                    out.flush();
                    out.close();
                }
                // 创建连接后回调监听
                if (mCallBack != null) {
                    mCallBack.onConnected(conn.getInputStream());
                }
            } catch (SocketTimeoutException e) {
                CommonUtils.logError(e);
                CommonUtils.logInfo("连接请求超时！");
                // 超时
                if (mCallBack != null) {
                    mCallBack.onConnectTimeout();
                }
            } catch (Exception e) {
                CommonUtils.logError(e);
                CommonUtils.logInfo("发送GET请求出现异常！");
                // 异常
                if (mCallBack != null) {
                    mCallBack.onConnectFail();
                }

            }
        }
    }

    /**
     * post请求
     *
     * @param url                请求Url,不可带参数
     * @param params             请求参数字符串,{@link #getParamsFromMap(Map)}
     * @param requestPropertyMap
     * @param callBack           请求回调 {@link HttpCallBack}
     */
    public void sendPost(String url, String params, Map<String, String> requestPropertyMap, HttpCallBack callBack) {
        ExecuteRunnable executor = new ExecuteRunnable(url, params, HttpUtils.POST, requestPropertyMap, callBack);
        mExecutor.execute(executor);
    }

    /**
     * get请求
     *
     * @param url                请求url,不可带参数
     * @param params             请求参数字符串,{@link #getParamsFromMap(Map)}
     * @param requestPropertyMap
     * @param callBack           请求回调 {@link HttpCallBack}
     */
    public void sendGet(String url, String params, Map<String, String> requestPropertyMap, HttpCallBack callBack) {
        ExecuteRunnable executor = new ExecuteRunnable(url, params, HttpUtils.GET, requestPropertyMap, callBack);
        mExecutor.execute(executor);
    }

    /**
     * 将参数转成对应的参数字符串,此部分是独立的,不管是GET还是POST都可以使用到此参数字符串
     *
     * @param paramsMap 参数列表
     * @return 参数以 key1=value1&key2=value2...形式返回
     */
    public static String getParamsFromMap(Map<String, ? extends Object> paramsMap) {
        StringBuilder strBuilder = new StringBuilder();
        if (paramsMap != null && paramsMap.size() > 0) {
            for (Map.Entry<String, ? extends Object> entry : paramsMap.entrySet()) {
                strBuilder.append(entry.getKey());
                strBuilder.append('=');
                strBuilder.append(entry.getValue());
                strBuilder.append('&');
            }
            if (strBuilder.length() > 0) {
                strBuilder.deleteCharAt(strBuilder.length() - 1);
            }
        }
        return strBuilder.toString();
    }
}
