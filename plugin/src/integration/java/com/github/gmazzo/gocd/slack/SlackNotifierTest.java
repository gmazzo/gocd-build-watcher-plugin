package com.github.gmazzo.gocd.slack;

import com.github.gmazzo.gocd.model.Message;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class SlackNotifierTest {
    private SlackNotifier directNotifier;
    private SlackNotifier channelNotifier;

    @Before
    public void setUp() {
        String token = System.getenv("SLACK_API_TOKEN");

        assert token != null : "Missing SLACK_API_TOKEN environment variable!";

        directNotifier = new SlackNotifier(token, null, null);
        channelNotifier = new SlackNotifier(token, "#random", null);
    }

    @Test
    public void testSendMessage_Me() {
        directNotifier.sendMessage("gmazzo65@gmail.com", new Message.Builder()
                .text("Hello gmazzo65@gmail.com! this a direct message " + new Date())
                .tag("AAA", "Text AAA")
                .longTag("BBB", "Text BBB")
                .tag("CCC", "Text CCC")
                .tag("DDD", "Text DDD")
                .longTag("EEE", "Text EEE")
                .tag("FFF", "Text FFF")
                .build());
    }

    @Test
    public void testSendMessage_Unknown() {
        directNotifier.sendMessage("who@gmail.com", new Message.Builder()
                .text("Hello who@gmail.com! this a direct message to an unknown user " + new Date())
                .build());
    }

    @Test
    public void testSendMessage_Channel() {
        channelNotifier.sendMessage("gmazzo65@gmail.com", new Message.Builder()
                .text("Hello gmazzo65@gmail.com! this a test message sent to #random " + new Date())
                .build());
    }

}
