package qqserver.service;

import java.util.HashMap;

/**
 * @author Xuanchi Guo
 * @project QQserver
 * @created 6/7/23
 * ManagerServerConnectClientThread is a thread that manages all the threads that keep listening to the client for new messages
 */
public class ManagerServerConnectClientThread {
    // key: username, value: QQServerConnectClientThread
    // concurrentHashMap is thread-safe, but HashMap is not thread-safe. In multi-threaded environment, you should use ConcurrentHashMap
    private static HashMap<String, QQServerConnectClientThread> hm = new HashMap<>();

    //add a thread to the thread manager
    public static void addQQServerConnectClientThread(String username, QQServerConnectClientThread thread) {
        hm.put(username, thread);
    }
    //get a thread from the thread manager
    public static QQServerConnectClientThread getQQServerConnectClientThread(String username) {
        return hm.get(username);
    }

    // remove a thread from the thread manager
    public static void removeQQServerConnectClientThread(String username) {
        hm.remove(username);
    }

    // get the online friend list
    public static String getOnlineFriendList() {
        String onlineFriendList = "";
        for (String username : hm.keySet()) {
            onlineFriendList += username + " ";
        }
        return onlineFriendList;
    }

    public static HashMap<String, QQServerConnectClientThread> getHm() {
        return hm;
    }
}
