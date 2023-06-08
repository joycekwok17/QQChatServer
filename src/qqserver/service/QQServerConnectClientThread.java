package qqserver.service;

import common.Message;
import common.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author Xuanchi Guo
 * @project QQserver
 * @created 6/7/23
 * QQServerConnectClientThread is a thread that keeps listening to the client for new messages
 */
public class QQServerConnectClientThread extends Thread{
    private Socket socket;
    private String username;  // the username of the client

    public QQServerConnectClientThread(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
    }

    @Override
    public void run() { // keep the thread alive, because it is used to listen to the client for new messages
        while(true) {
            try {
                System.out.println("Server thread is listening to the client "+ username +"...");
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message =(Message) ois.readObject();// read the message object from the client

                switch (message.getMessageType()) {
                    case MessageType.message_get_onLineFriend -> {
                        System.out.println("User " + username + " is requesting the online friend list");
                        // get the online friend list
                        String onlineFriends = ManagerServerConnectClientThread.getOnlineFriendList();
                        // send the online friend list to the client
                        Message messageToClient = new Message();
                        messageToClient.setMessageType(MessageType.message_ret_onLineFriend);
                        messageToClient.setContent(onlineFriends);
                        messageToClient.setReceiver(message.getSender());
                        // send the message object to the client
                        new ObjectOutputStream(socket.getOutputStream()).writeObject(messageToClient);
                    }
                    case MessageType.message_client_exit -> {
                        System.out.println("User " + username + " is exiting");
                        ManagerServerConnectClientThread.removeQQServerConnectClientThread(message.getSender());
                        socket.close();
                    }
                    case MessageType.message_comm_mes -> {
                        System.out.println("User " + username + " is sending a private message to " + message.getReceiver() + ": " + message.getContent());
                        // send the private message to the receiver
                        if (ManagerServerConnectClientThread.getHm().containsKey(message.getReceiver())) {
                            ObjectOutputStream oos = new ObjectOutputStream(ManagerServerConnectClientThread.getQQServerConnectClientThread(message.getReceiver()).getSocket().getOutputStream());
                            oos.writeObject(message);
                        } else {
                            System.out.println("User " + message.getReceiver() + " is offline, this message will be stored in the database");
                            // if the receiver is offline, the message will be sent to the server and stored in the database for later retrieval
                            QQserver.addOfflineMessage(message.getReceiver(), message);
                        }
                    }
                    case MessageType.message_group_message -> {
                        System.out.println("User " + username + " is sending a group message: " + message.getContent());
                        // send the group message to all the online users
                        for (QQServerConnectClientThread qqServerConnectClientThread : ManagerServerConnectClientThread.getHm().values()) {
                            if (!qqServerConnectClientThread.getUsername().equals(username)) {
                                ObjectOutputStream oos1 = new ObjectOutputStream(qqServerConnectClientThread.getSocket().getOutputStream());
                                oos1.writeObject(message);
                            }
                        }
                    }
                    case MessageType.message_file_message -> {
                        System.out.println("User " + username + " is sending a file to " + message.getReceiver());
                        // send the file to the receiver
                        if (ManagerServerConnectClientThread.getHm().containsKey(message.getReceiver())) {
                            ObjectOutputStream oos = new ObjectOutputStream(ManagerServerConnectClientThread.getQQServerConnectClientThread(message.getReceiver()).getSocket().getOutputStream());
                            oos.writeObject(message);
                        } else {
                            // if the receiver is offline, the message will be sent to the server and stored in the database for later retrieval
                            QQserver.addOfflineMessage(message.getReceiver(), message);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUsername() {
        return username;
    }
}
