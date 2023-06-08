package qqserver.service;

import common.Message;
import common.MessageType;
import common.utils.Utility;

import javax.swing.text.Utilities;

/**
 * @author Xuanchi Guo
 * @project QQserver
 * @created 6/8/23
 */
public class SendNewsToAll extends Thread {
    @Override
    public void run() {

        while(true) {
            System.out.println("please input news(input EXIT to exit this service): ");
            String news = Utility.readString(100);

            if (news.equals("EXIT")) {
                break; // exit this service or thread, but the server is still running
            }
            Message message = new Message();
            message.setMessageType(MessageType.message_group_message);
            message.setSender("Server");
            message.setContent(news);
            message.setSendTime(new java.util.Date().toString());
            System.out.println("Server is sending a group message: " + news);
            for (String username : ManagerServerConnectClientThread.getHm().keySet()) {
                try {
                    new java.io.ObjectOutputStream(ManagerServerConnectClientThread.getQQServerConnectClientThread(username).getSocket().getOutputStream()).writeObject(message);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
