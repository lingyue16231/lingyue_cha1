package com.jin.chat1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.packet.VCard;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XmppManager {
    //服务器地址
    public final static String HOST = "10.22.82.254";
    //服务器端口
    private static int PORT = 5222;

    public static XmppManager instance = null;
    private ChatManager _chatManager;//chat管理
    private static XMPPConnection _xmppConn;
    private Map<String,Chat> _mapChats = Collections.synchronizedMap(new HashMap<String, Chat>());
    private MessageListener _msgListener;
    private IUIMsgListener _uiMsgListener;
    private Registration reg;


    // region Constructors
    private XmppManager(){
        initConfiguration();
        _msgListener = new MessageListener();
    }

    public synchronized static XmppManager getInstance(){
        if(null == instance){
            instance = new XmppManager();
        }
        return instance;
    }

    //进行配置
    public static void initConfiguration(){
        ConnectionConfiguration config =new ConnectionConfiguration(HOST,PORT);
        //调试模式
        config.setDebuggerEnabled(true);
        //允许自动连接
        config.setReconnectionAllowed(true);
        //安全模式
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        _xmppConn = new XMPPConnection(config);

        Log.e("XmppManager","initConfiguration()初始化完成");
    }
    // endregion Constructors

    // region Private Methods
    /** 判断是否连接 **/
    private boolean isConnect(){
        if(_xmppConn == null ){
            return false;
        }
        try {
            _xmppConn.connect();
            Log.e("XmppManager","isConnect()连接服务器成功");
            return true;
        } catch (XMPPException e) {
            e.printStackTrace();
            Log.e("XmppManager","isConnect()发生错误"+e.getMessage().toString());
        }
        return false;
    }

    /** 发送即时消息
     * @param sessionJID
     * @param sessionName
     * @param message
     * @param type
     */
    public void sendMessage(String sessionJID,String sessionName,String message,String type){
        _chatManager = _xmppConn.getChatManager();
        Chat chat = null;
        if(_mapChats.containsKey(sessionJID)){
            chat = _mapChats.get(sessionJID);
        }else{
            chat = _chatManager.createChat(sessionJID, null);
            _mapChats.put(sessionJID,chat);
        }

        if(chat != null){
            try {
                Message msg = new Message();
                msg.setBody(message);
                msg.setTo(sessionJID);
                //				if(type.equals(Message.Type.chat.toString())){
                //					msg.setType(Message.Type.chat);
                //					insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,msg.getBody(),type,IM.FILE_TYPE[0]);
                //				}
                //				else if(type.equals(Message.Type.groupchat.toString())){
                //					msg.setType(Message.Type.groupchat);
                //				}
                chat.sendMessage(msg);

            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    }
    // endregion Private Methods


    /** 登录
     * @param account
     * @param pwd
     * @return
     */
    public boolean Login(String account,String pwd){
        Log.e("account ",account+"    "+pwd);
        if(isConnect()){

            try {
                Log.e("isConnect() 1111111","true");
                _xmppConn.login(account, pwd);
                Log.e("XmppManager","isLogin() 登录成功");
                //发送出席信息
                Presence presence = new Presence(Type.available);
                _xmppConn.sendPacket(presence);
                _xmppConn.addPacketListener(_msgListener, new PacketTypeFilter(Packet.class));
                return true;
            } catch (XMPPException e) {
                e.printStackTrace();
                Log.e("XmppManager","isLogin() 登录错误");
                initConfiguration();
            }
        }
        Log.e("isConnect() 1111111","false");
        return false;
    }
    public boolean Register(String account, String pwd) {
        if(isConnect()){
            Registration reg = new Registration();
            reg.setType(IQ.Type.SET);
            reg.setTo(_xmppConn.getServiceName());
            reg.setUsername(account);
            reg.setPassword(pwd);
            reg.addAttribute("android", "geolo_createUser_android");
            PacketFilter filter = new AndFilter(new PacketIDFilter(reg.getPacketID()), new PacketTypeFilter(IQ.class));

            PacketCollector collector = _xmppConn.createPacketCollector(filter);
            // 给注册的Packet设置Listener，因为只有等到正真注册成功后，我们才可以交流
            // collector.addPacketListener(packetListener, filter);
            // 向服务器端，发送注册Packet包，注意其中Registration是Packet的子类
           _xmppConn.sendPacket(reg);
            IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
            // 停止从队列中等待
            collector.cancel();
            if(result==null) {
                initConfiguration();
                return false;
            }
                    else if(result.getType()==IQ.Type.RESULT)
                    {  return true; }
                         else{
                             if(result.getError().toString().equalsIgnoreCase("conflict(409)"))
                             {
                                 initConfiguration();
                                    return false;
                                }else{
                                 initConfiguration();
                                   return false;
                                 }
                        }
        }
        initConfiguration();
        return false;
    }
    /** 发送单聊文本信息
     * @param pAccount
     * @param pMessage
     */
    public void SendChatMessage(String pAccount, String pMessage) {
        String sessionJID = pAccount + "@"+XmppManager.HOST;
        String sessionName = pAccount;
        sendMessage(sessionJID, sessionName, pMessage, Message.Type.chat.toString());
    }

    /** 断开连接，退出服务器，即退出登录
     *
     */
    public static void DisConnect(){
        if (_xmppConn != null && _xmppConn.isConnected()) {
            //发送出席信息
            Presence presence = new Presence(Type.unavailable);
            _xmppConn.disconnect(presence);
            Log.e("XmppManager","disConnect() 断开连接成功");
        }
    }


    /** 设置界面收到新消息监听
     * @param pListener
     */
    public void SetUIMsgListener(IUIMsgListener  pListener)
    {
        _uiMsgListener = pListener;
    }



    public class MessageListener implements PacketListener {
        // 服务器返回给客户端的信息
        public void processPacket(Packet packet) {
            Log.e("AllPacketListener",""+ packet.toXML());
            //消息监听
            if(packet instanceof Message){
                Message msg = (Message)packet;
                if(msg.getType() == Message.Type.chat){
                    String sessionJID = StringUtils.parseBareAddress(msg.getFrom());
                    Log.e("xmppmanager：个人消息"+msg.getFrom()+" say:",msg.getBody());
                    //insertMessage(sessionJID,sessionJID,msg.getBody(),"chat",IM.FILE_TYPE[0]);
                    if(null != _uiMsgListener)
                    {
                        _uiMsgListener.onMessageReceived(msg.getFrom(), msg.getBody());
                    }
                }
            }
        }
    }
    /**
     * 添加分组
     */
    public boolean addGroup(String groupName){
        Roster roster=_xmppConn.getRoster();
        try {
            roster.createGroup(groupName);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     *添加好友 无分组
     */
    public boolean addUser(String username){//第一个用户名，第二个昵称
        Roster roster=_xmppConn.getRoster();
        try {
            roster.createEntry(username+"@"+HOST,username,new String[]{"Friends"});
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 添加好友 有分组
     */
    public boolean addGroupUser(String username,String name,String groupname){
        Roster roster=_xmppConn.getRoster();
        try {
            roster.createEntry(username,name,new String[]{groupname});
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 删除好友
     */
    public boolean removerUser(String userName){
        Roster roster=_xmppConn.getRoster();
        try{
            RosterEntry entry=roster.getEntry(userName);
            roster.removeEntry(entry);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 获取指定好友信息
     **/
    public RosterEntry getFriend(String user){
        Roster roster=_xmppConn.getRoster();
        String username=user+"@"+HOST;
        RosterEntry rosterEntry=roster.getEntry(user);
        if (rosterEntry==null)
            Log.i("TAG","空");
        else
            Log.i("TAG",rosterEntry.getUser());
        return rosterEntry;
    }
    /**
     * 返回所有信息
     */
    public List<RosterEntry> getAlluser() {
        Roster roster = _xmppConn.getRoster();
        List<RosterEntry> list = new ArrayList<RosterEntry>();
        Collection<RosterEntry> rosterEntries = roster.getEntries();
        Iterator<RosterEntry> i = rosterEntries.iterator();
        while (i.hasNext()) {
            list.add(i.next());
        }
        return list;
    }

    /**
     * 获取某组好友信息
     * @param groupName
     * @return
     */
    public List<RosterEntry> getEntriesByGroup(String groupName) {
        if (_xmppConn == null)
            return null;
        List<RosterEntry> Entrieslist = new ArrayList<RosterEntry>();
        RosterGroup rosterGroup = _xmppConn.getRoster().getGroup(
                groupName);
        Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();
        Iterator<RosterEntry> i = rosterEntry.iterator();
        while (i.hasNext()) {
            Entrieslist.add(i.next());
        }
        return Entrieslist;
    }

    /**
     *获取所有组
     * @return
     */
    public List<RosterGroup> getGroups() {
        Roster roster = _xmppConn.getRoster();
        List<RosterGroup> grouplist = new ArrayList<RosterGroup>();
        Collection<RosterGroup> rosterGroup = roster.getGroups();
        Iterator<RosterGroup> i = rosterGroup.iterator();
        while (i.hasNext()) {
            grouplist.add(i.next());
        }
        return grouplist;
    }
    public Drawable getUserImage(String user){
        ByteArrayInputStream bais = null;
        try {
            VCard vcard = new VCard();
            // 加入这句代码，解决No VCard for
            ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",
                    new org.jivesoftware.smackx.provider.VCardProvider());

            vcard.load(_xmppConn, user+"@"+_xmppConn.getServiceName());

            if (vcard == null || vcard.getAvatar() == null)
                return null;
            bais = new ByteArrayInputStream(vcard.getAvatar());

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (bais == null)
            return null;
        Bitmap bitmap= BitmapFactory.decodeStream(bais);
        return new BitmapDrawable(bitmap);
    }
    //判断好友状态
    public int isonline(String strUrl)
    {

        Roster roster =_xmppConn.getRoster();
        Presence presence = roster.getPresence(strUrl);
        if (presence.getType() == Type.available) {//在线
            return 1;
        }
        return 0;
    }

    public static XMPPConnection getConnect(){
        return _xmppConn;
    }


}


