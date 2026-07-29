package com.sky.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//管理端提醒业务类
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {
    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();
    // 连接建立时触发
    @OnOpen
    public void onOpen(Session session) {
        SESSION_MAP.put(session.getId(), session);
        System.out.println("管理员客户端连接成功，当前在线：" + SESSION_MAP.size());
    }
//
//    // 前端发消息（本业务用不到）
//    @OnMessage
//    public void onMessage(String msg, Session session) {
//        System.out.println("收到前端消息：" + msg);
//    }

    // 连接关闭
    @OnClose
    public void onClose(Session session) {
        SESSION_MAP.remove(session.getId());
        System.out.println("客户端断开连接，" + SESSION_MAP.size());
    }
    // 给管理端发送通知(type\orderId\content)
    public void sendNotice(String messgae) {
        for (Session session : SESSION_MAP.values()) {
            try {
                session.getBasicRemote().sendText(messgae);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
