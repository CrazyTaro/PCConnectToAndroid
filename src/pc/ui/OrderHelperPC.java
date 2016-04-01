package pc.ui;

import net.interfaces.OnInfoChangedListener;
import net.interfaces.OnMessageChangedListener;
import net.manager.*;
import net.socket.DeviceInfo;
import net.utils.AdbUtils;
import net.utils.CommonUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class OrderHelperPC extends JFrame implements OnMessageChangedListener, OnInfoChangedListener {
    private ClientSocketManager mSocketMgr = null;
    private IMessageMgrAction mMsgMgr = null;
    private IMsgParseMgrAction mIMsgParseMgrAction = null;
    private AdbUtils mAdbUtils = null;

    public static final int WIDTH = 900;
    public static final int HEIGHT = 500;

    private String mCurrentDeviceToken = null;

    // 设备显示描述标题
    private JLabel label_device_main_title;
    // 设备信息描述标题
    private JLabel label_device_info_title;
    private JLabel label_msg_recieve;
    private JLabel label_msg_send;
    private JList<String> list_device_token;
    private JList<String> list_device_info;
    private JButton btn_connect_all;
    private JButton btn_disconnect_alll;
    private JButton btn_connect;
    private JButton btn_disconnect;
    private JButton btn_query_order;
    private JButton btn_place_order;
    private JButton btn_device_info;
    private JButton btn_clear_info;
    private JTextArea txtarea_msg_recieve;
    private JTextArea txtarea_msg_send;

    public OrderHelperPC() {
        // 设置窗口大小
        this.setSize(WIDTH, HEIGHT);
        // 设置窗口关闭时同时退出
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        // 设置窗口大小不可缩放
        this.setResizable(false);
        // 设置窗口相对屏幕中心显示
        this.setLocationRelativeTo(null);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                mSocketMgr.closeAll();
                // updateDeviceInfoList(null);
                // updateDeviceList(null);
                // updateMsgList(null, MessageInfo.MSG_FOR_RECEIVE);
                // updateMsgList(null, MessageInfo.MSG_FOR_SEND);
            }
        });
        // 初始化所有界面/数据
        this.initial();
    }

    private void initialData() {
        //初始化adb连接方式
        mAdbUtils = new AdbUtils(AdbUtils.DEFAULT_ADB_PATH);
        //创建多客户端管理对象
        mSocketMgr = new ClientSocketManager(mAdbUtils);
        //消息管理对象
        mMsgMgr = new MessageManager();
        //设置消息处理回调
        mMsgMgr.setOnMessageChangedListener(this);
        //创建消息解析对象
        mIMsgParseMgrAction = new MessageParseManager();
        //设置消息解析对象
        mIMsgParseMgrAction.setOnMsgActionListener(mMsgMgr.getOnMsgActionListener());


        mSocketMgr.presetIMsgParseAction(mIMsgParseMgrAction);
        mSocketMgr.presetOnInfoChangedListener(this);
    }

    /**
     * 显示界面
     */
    public void showWindow() {
        this.setVisible(true);
    }

    /**
     * 隐藏界面
     */
    public void hideWindow() {
        this.setVisible(false);
    }

    /**
     * 初始化
     */
    private void initial() {
        createView();
        initialView();
        initialData();
    }

    /**
     * 创建界面元素
     */
    private void createView() {
        label_device_main_title = new JLabel();
        label_device_info_title = new JLabel();
        label_msg_recieve = new JLabel();
        label_msg_send = new JLabel();

        list_device_token = new JList<String>();
        list_device_info = new JList<String>();

        btn_connect_all = new JButton();
        btn_disconnect_alll = new JButton();
        btn_connect = new JButton();
        btn_disconnect = new JButton();
        btn_query_order = new JButton();
        btn_place_order = new JButton();
        btn_clear_info = new JButton();
        btn_device_info = new JButton();

        txtarea_msg_recieve = new JTextArea();
        txtarea_msg_send = new JTextArea();
        txtarea_msg_recieve.setLineWrap(true);
        txtarea_msg_send.setLineWrap(true);

        label_device_main_title.setText("设备Token/全部连接操作");
        label_device_info_title.setText("设备信息/操作");
        label_msg_recieve.setText("设备接收的消息(来自ANDROID端)");
        label_msg_send.setText("设备发送的消息(来自PC端)");

        btn_connect_all.setText("全部连接");
        btn_disconnect_alll.setText("全部断开");
        btn_connect.setText("建立连接");
        btn_disconnect.setText("断开连接");
        btn_query_order.setText("查询订单");
        btn_place_order.setText("下单支付");
        btn_clear_info.setText("清除消息");
        btn_device_info.setText("更新设备");
    }

    /**
     * 初始化界面元素
     */
    private void initialView() {
        // 设备token版块,管理连接设备
        JScrollPane scrollPaneForDevice = new JScrollPane(list_device_token);
        JPanel panelForAllDeviceBtn = new JPanel(new BorderLayout());
        JPanel panelForDevice = new JPanel(new BorderLayout());
        scrollPaneForDevice.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelForAllDeviceBtn.add(btn_connect_all, BorderLayout.NORTH);
        panelForAllDeviceBtn.add(btn_disconnect_alll, BorderLayout.SOUTH);
        panelForDevice.add(label_device_main_title, BorderLayout.NORTH);
        panelForDevice.add(scrollPaneForDevice, BorderLayout.CENTER);
        panelForDevice.add(panelForAllDeviceBtn, BorderLayout.SOUTH);

        // 设备相关信息及按钮操作版块
        JScrollPane scrollPaneForInfo = new JScrollPane(list_device_info);
        JPanel panelForInfo = new JPanel(new BorderLayout());
        JPanel panelForBtn = new JPanel(new GridLayout(0, 2));
        JPanel panelForInfoBtn = new JPanel(new BorderLayout());
        scrollPaneForInfo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panelForInfo.add(label_device_info_title, BorderLayout.NORTH);
        panelForInfo.add(scrollPaneForInfo, BorderLayout.CENTER);
        panelForBtn.add(btn_connect);
        panelForBtn.add(btn_disconnect);
        panelForBtn.add(btn_query_order);
        panelForBtn.add(btn_place_order);
        panelForBtn.add(btn_clear_info);
        panelForBtn.add(btn_device_info);
        panelForInfoBtn.add(panelForInfo, BorderLayout.CENTER);
        panelForInfoBtn.add(panelForBtn, BorderLayout.SOUTH);

        // 接收/发送消息显示版块
        JScrollPane scrollPaneForRecieve = new JScrollPane(txtarea_msg_recieve);
        JScrollPane scrollPaneForSend = new JScrollPane(txtarea_msg_send);
        JPanel panelForRecieve = new JPanel(new BorderLayout());
        JPanel panelForSend = new JPanel(new BorderLayout());
        panelForRecieve.add(label_msg_recieve, BorderLayout.NORTH);
        panelForRecieve.add(scrollPaneForRecieve, BorderLayout.CENTER);
        panelForSend.add(label_msg_send, BorderLayout.NORTH);
        panelForSend.add(scrollPaneForSend, BorderLayout.CENTER);

        // 设备操作版块,设备token+设备信息
        JPanel panelForDeviceHandle = new JPanel(new GridLayout(1, 2));
        panelForDeviceHandle.add(panelForDevice);
        panelForDeviceHandle.add(panelForInfoBtn);

        // 消息显示版块,接收+发送消息
        JPanel panelForDeviceMsg = new JPanel(new GridLayout(1, 2));
        panelForDeviceMsg.add(panelForRecieve);
        panelForDeviceMsg.add(panelForSend);

        // 全局布局
        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);
        this.add(panelForDeviceHandle);
        this.add(panelForDeviceMsg);

        GridBagConstraints s = new GridBagConstraints();
        // 在容器内完全填充,行列方面都填充
        s.fill = GridBagConstraints.BOTH;
        // X单元格位置
        s.gridx = 0;
        // Y单元格位置
        s.gridy = 0;
        // X占用单元格数量
        s.gridwidth = 1;
        // Y占用单元格数据,此处设置为填充到容器底部
        s.gridheight = GridBagConstraints.REMAINDER;
        // 缩放时在X方向上的缩放程度,0为不缩放
        s.weightx = 0;
        // 绽放时在Y方向上的缩放程度,0-1之前,1为完全填充缩放
        s.weighty = 1;
        layout.setConstraints(panelForDeviceHandle, s);
        s.fill = GridBagConstraints.BOTH;
        s.gridx = 1;
        s.gridy = 0;
        s.gridwidth = 2;
        s.gridheight = GridBagConstraints.REMAINDER;
        s.weightx = 1;
        s.weighty = 1;
        layout.setConstraints(panelForDeviceMsg, s);

        BtnActionListener btnActionListener = new BtnActionListener();
        JListSelectionListener selectionListener = new JListSelectionListener();
        btn_connect_all.addActionListener(btnActionListener);
        btn_disconnect_alll.addActionListener(btnActionListener);
        btn_connect.addActionListener(btnActionListener);
        btn_disconnect.addActionListener(btnActionListener);
        btn_query_order.addActionListener(btnActionListener);
        btn_place_order.addActionListener(btnActionListener);
        btn_clear_info.addActionListener(btnActionListener);
        btn_device_info.addActionListener(btnActionListener);

        list_device_token.addListSelectionListener(selectionListener);
        list_device_info.addListSelectionListener(selectionListener);
    }

    private void showDialog(String title, String msg) {
        JOptionPane.showInternalMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateButtonEnable(String currentToken) {
        DeviceInfo info = mSocketMgr.getSocketDeviceInfo(currentToken);
        if (info == null) {
            btn_connect.setEnabled(false);
            btn_disconnect.setEnabled(false);
            btn_query_order.setEnabled(false);
            btn_place_order.setEnabled(false);
        } else {
            if (info.isConnectAvaliable()) {
                btn_disconnect.setEnabled(true);
                btn_connect.setEnabled(false);
            } else {
                btn_connect.setEnabled(true);
                btn_disconnect.setEnabled(false);
            }
            if (info.isFree()) {
                btn_place_order.setEnabled(true);
            } else {
                btn_place_order.setEnabled(false);
            }
        }
    }

    private void updateDeviceList(List<String> deviceList) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        if (deviceList != null) {
            model.addElement("");
            for (String device : deviceList) {
                model.addElement(device);
            }
        }
        list_device_token.setModel(model);
    }

    private void updateDeviceInfoList(String deviceToken) {
        DeviceInfo info = mSocketMgr.getSocketDeviceInfo(deviceToken);
        DefaultListModel<String> model = new DefaultListModel<String>();
        if (info != null) {
            model.addElement("设备识别:" + info.getDeviceToken());
            model.addElement("绑定号码:" + info.getBindMobile());
            model.addElement("是否连接:" + info.isConnectAvaliable());
            model.addElement("是否空闲:" + info.isFree());
            model.addElement("是否可用:" + info.isDeviceReady());
            model.addElement("本地端口:" + info.getLocalPort());
            model.addElement("转发端口:" + info.getUsbPort());
        }
        list_device_info.setModel(model);
    }

    private void updateMsgList(String deviceToken, int from) {
        MessageInfo info = mMsgMgr.getMsgInfoByDevice(deviceToken);
        StringBuilder builder = new StringBuilder();
        if (info != null) {
            List<String> msgList = info.getMsgListByFrom(from);
            if (msgList != null) {
                for (String msg : msgList) {
                    builder.append(msg);
                    builder.append("\n");
                }
            }
        }

        if (from == MessageInfo.MSG_FOR_RECEIVE) {
            txtarea_msg_recieve.setText(builder.toString());
        } else if (from == MessageInfo.MSG_FOR_SEND) {
            txtarea_msg_send.setText(builder.toString());
        }

    }

    private class BtnActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();
            if (source.equals(btn_connect_all)) {
                //全部连接
                mSocketMgr.updateSocketClientConnect();
                mSocketMgr.connectAllUsePresetListener();
                updateDeviceList(mSocketMgr.getUSBDeviceToken());
                updateDeviceInfoList(mCurrentDeviceToken);
                updateButtonEnable(mCurrentDeviceToken);
            } else if (source.equals(btn_disconnect_alll)) {
                //全部断开
                mSocketMgr.closeAll();
                mCurrentDeviceToken = null;
                updateDeviceInfoList(mCurrentDeviceToken);
                updateButtonEnable(mCurrentDeviceToken);
            } else if (source.equals(btn_connect)) {
                // 连接
                mSocketMgr.connectSocketUsePresetListener(mCurrentDeviceToken);
                updateDeviceInfoList(mCurrentDeviceToken);
                updateButtonEnable(mCurrentDeviceToken);
            } else if (source.equals(btn_disconnect)) {
                // 断开连接
                mSocketMgr.closeSocket(mCurrentDeviceToken);
                updateDeviceInfoList(mCurrentDeviceToken);
                updateButtonEnable(mCurrentDeviceToken);
            } else if (source.equals(btn_clear_info)) {
                // 消除消息
                txtarea_msg_recieve.setText("");
                txtarea_msg_send.setText("");
            } else if (source.equals(btn_device_info)) {
                // 设备信息,暂时无用
                mSocketMgr.updateSocketClientConnect();
                updateDeviceList(mSocketMgr.getUSBDeviceToken());
            }
        }

    }

    private class JListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            Object source = e.getSource();
            if (source.equals(list_device_token)) {
                // 记录当前的选中的设备
                String selectToken = list_device_token.getSelectedValue();
                if (selectToken != null && !selectToken.equals(mCurrentDeviceToken)) {
                    // 更新设备信息
                    mCurrentDeviceToken = selectToken;
                }
            }
            updateButtonEnable(mCurrentDeviceToken);
            updateDeviceInfoList(mCurrentDeviceToken);
            updateMsgList(mCurrentDeviceToken, MessageInfo.MSG_FOR_RECEIVE);
            updateMsgList(mCurrentDeviceToken, MessageInfo.MSG_FOR_SEND);
        }
    }

    @Override
    public void onAppendMsg(String deviceToken, String msg, int from) {
        if (from == MessageInfo.MSG_FOR_RECEIVE) {
            txtarea_msg_recieve.append("token :" + deviceToken + "\t" + msg + "\n");
        } else if (from == MessageInfo.MSG_FOR_SEND) {
            txtarea_msg_send.append("token :" + deviceToken + "\t" + msg + "\n");
        }
    }

    @Override
    public void onCleanMsg(String deviceToken, int saveCount, int from) {
        // TODO Auto-generated method stub
        CommonUtils.logInfo("清除消息");
        txtarea_msg_recieve.setText("");
    }

    @Override
    public boolean isSaveMsgBeforeClearMsg(String deviceToken, int from) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onDeviceInfoChanged(String deviceToken, DeviceInfo info, int changedType, Object oldValue) {
        updateDeviceInfoList(deviceToken);
        updateButtonEnable(deviceToken);
    }
}
