package common;

/**
 * @author Xuanchi Guo
 * @project QQserver
 * @created 6/7/23
 */
public interface MessageType {
    String message_login_succeed = "1"; // login succeed
    String message_login_fail = "2"; // login failed
    String message_comm_mes = "3"; // common message
    String message_get_onLineFriend = "4"; // get online friend
    String message_ret_onLineFriend = "5"; // return online friend
    String message_client_exit = "6"; // logout
   String message_group_message = "7"; // group message
    String message_file_message = "8"; // file message
    String message_offline_message = "9"; // offline message

}
