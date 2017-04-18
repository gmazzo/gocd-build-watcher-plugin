package com.github.gmazzo.gocd;

public interface Notifier {

    void sendMessage(String userEmail, String message);

}
