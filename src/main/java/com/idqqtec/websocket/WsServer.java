package com.idqqtec.websocket;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.List;
public class WsServer extends WebSocketServer {
    public WsServer(int port) {
        super(new InetSocketAddress(port));
    }

    public WsServer(InetSocketAddress address) {
        super(address);
    }

    /**
     * 打开连接时触发
     * @param conn
     * @param clientHandshake
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        System.out.println("onOpen<<<<<<<conn::"+conn+"---clientHandshake::"+clientHandshake);
    }

    /**
     * 断开连接时触发
     * @param conn
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("onClose<<<<<<<conn::"+conn+"--code::"+code+"--reason::"+reason+"--remote::"+remote);
        userLeave(conn);
        //当有用户离开时给所有在线用户再发送一份在线用户名单
        Collection<String> onlineUser = WebSocketPool.getOnlineUser();
        String data = JSON.toJSONString(onlineUser);
        Message msg = new Message();
        msg.setStatus("userList");
        msg.setFrom("Server");
        msg.setMsgType("txt");
        msg.setMessage(data);
        String s = JSON.toJSONString(msg);
        WebSocketPool.sendMessageToAll(s);
    }

    /**
     * 收到消息时触发
     * @param conn
     * @param data
     */
    @Override
    public void onMessage(WebSocket conn, String data) {
        System.out.println("onMessage<<<<<<<conn::"+conn+"--data::"+data);
        if (null != data){
            JSONObject jsonObject = JSON.parseObject(data);
            String status = jsonObject.getString("status");
            String fromName = jsonObject.getString("from");
            String toName = jsonObject.getString("to");
            String msgType = jsonObject.getString("msgType");
            String msg = jsonObject.getString("message");
            if (status.equals("online")){
                userJoin(conn,fromName);//用户加入
                //更新并发送在线用户列表
                Collection<String> onlineUser = WebSocketPool.getOnlineUser();
                String users = JSON.toJSONString(onlineUser);
                Message message = new Message();
                message.setStatus("userList");
                message.setFrom("Server");
                message.setMsgType("txt");
                message.setMessage(users);
                String s = JSON.toJSONString(message);
                WebSocketPool.sendMessageToAll(s);
            }else if (status.equals("talking")){
                List<WebSocket> wsByUsers = WebSocketPool.getWsByUser(toName);
                for (WebSocket webSocket : wsByUsers){
                    Message message = new Message();
                    message.setStatus(status);
                    message.setFrom(fromName);
                    message.setTo(toName);
                    message.setMsgType(msgType);
                    message.setMessage(msg);
                    String s = JSON.toJSONString(message);
                    WebSocketPool.sendMessageToUser(webSocket,s);
                }
            }else if (status.equals("offline")){
                userLeave(conn);
                //当有用户离开时给所有在线用户再发送一份在线用户名单
                Collection<String> onlineUser = WebSocketPool.getOnlineUser();
                String users = JSON.toJSONString(onlineUser);
                Message message = new Message();
                message.setStatus("userList");
                message.setFrom("Server");
                message.setMsgType("txt");
                message.setMessage(users);
                String s = JSON.toJSONString(message);
                WebSocketPool.sendMessageToAll(s);
            }else {
                System.out.println("未知消息类型");
            }
        }
    }

    /**
     * 发生错误是触发
     * @param conn
     * @param e
     */
    @Override
    public void onError(WebSocket conn, Exception e) {
        System.out.println("onError<<<<<<<conn::"+conn+"--Exception::"+e);
        e.printStackTrace();
    }

    /**
     * 在连接池中删除当前连接
     * @param conn
     */
    private void userLeave(WebSocket conn) {
        WebSocketPool.removeUser(conn);
    }

    /**
     * 在连接池中加入当前连接
     * @param conn
     * @param userName
     */
    private void userJoin(WebSocket conn, String userName) {
        WebSocketPool.addUser(userName, conn);
        System.out.println(userName);
    }

}
