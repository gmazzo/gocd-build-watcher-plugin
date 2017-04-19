package com.github.gmazzo.gocd;

import com.github.gmazzo.gocd.model.Message;

public interface Notifier {

    void sendMessage(String userEmail, Message message);

}
