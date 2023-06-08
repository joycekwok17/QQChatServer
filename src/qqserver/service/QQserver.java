package qqserver.service;

import common.Message;
import common.MessageType;
import common.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Xuanchi Guo
 * @project QQserver
 * @created 6/7/23
 */
public class QQserver {

    private ServerSocket serverSocket;
    private static HashMap<String, User> validUsers = new HashMap<>(); // key: username, value: user

    private static ConcurrentHashMap<String, List<Message>> offlineMessages = new ConcurrentHashMap<>(); // key: username, value: offline messages

    public static void addOfflineMessage(String receiver, Message message) {
        List<Message> messages = offlineMessages.computeIfAbsent(receiver, k -> new java.util.ArrayList<>());
        messages.add(message);
    }

    static { // static method will be executed when the class is loaded
        validUsers.put("admin", new User("admin", "123456"));
        validUsers.put("user1", new User("user1", "123456"));
        validUsers.put("user2", new User("user2", "123456"));
        validUsers.put("user3", new User("user3", "123456"));
        validUsers.put("user4", new User("user4", "123456"));
    }
    
    private boolean checkUser(String username, String password) {
        User user = validUsers.get(username);
        return user != null && Objects.equals(password, user.getPassword());
    }

    public QQserver() {
        System.out.println("Server is running at port 9999...");
        new SendNewsToAll().start(); // start the thread to send news to all clients
        try {
            serverSocket = new ServerSocket(9999); // create a server socket at port 9999

            while (true) {
                // keep listening to the port 9999 for new connections from clients
                Socket socket = serverSocket.accept(); // if no new connection, the thread will be blocked here

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                User user = (User) ois.readObject(); // read the user object from the client

                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                Message message = new Message();

                if (checkUser(user.getUsername(), user.getPassword())) {
                    System.out.println("User " + user.getUsername() + " is valid");


                    message.setMessageType(MessageType.message_login_succeed);
                    oos.writeObject(message); // send the message object to the client

                    // create a thread to keep listening to the client for new messages -> QQServerConnectClientThread
                    QQServerConnectClientThread thread = new QQServerConnectClientThread(socket, user.getUsername());
                    thread.start(); // start the thread

                    // add the thread to the thread manager
                    ManagerServerConnectClientThread.addQQServerConnectClientThread(user.getUsername(), thread);

                    // check if there are offline messages for the user
                    List<Message> messages = offlineMessages.get(user.getUsername());
                    if (messages != null) {
                        System.out.println("There are offline messages for user " + user.getUsername());
                        message.setMessageType(MessageType.message_offline_message);
                        for (Message m : messages) {
                            message.setContent(m.getSender() + " said to you: " + m.getContent());
                            oos.writeObject(message); // send the message object to the client
                        }
                        offlineMessages.remove(user.getUsername()); // remove the offline messages from the offline messages list
                    }

                } else {
                    System.out.println("User " + user.getUsername() + " is not valid");
                    message.setMessageType(MessageType.message_login_fail);
                    oos.writeObject(message); // send the message object to the client
                    socket.close(); // close the socket because the user is not valid
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                serverSocket.close(); // close the server socket when the server is closed or crashed for some reason (e.g. power outage)
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
