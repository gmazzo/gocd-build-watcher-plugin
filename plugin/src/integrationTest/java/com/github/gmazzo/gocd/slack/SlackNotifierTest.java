package com.github.gmazzo.gocd.slack;

import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class SlackNotifierTest {
    private SlackNotifier directNotifier;
    private SlackNotifier channelNotifier;

    @Before
    public void setUp() {
        String token = System.getenv("SLACK_API_TOKEN");

        directNotifier = new SlackNotifier(token, null, null);
        channelNotifier = new SlackNotifier(token, "#random", null);
    }

    @Test
    public void testSendMessage_Me() {
        directNotifier.sendMessage("gmazzo65@gmail.com", "Hello gmazzo65@gmail.com! this a direct message " + new Date());
    }

    @Test
    public void testSendMessage_Unknown() {
        directNotifier.sendMessage("who@gmail.com", "Hello who@gmail.com! this a direct message to an unknown user " + new Date());
    }

    @Test
    public void testSendMessage_Channel() {
        channelNotifier.sendMessage("gmazzo65@gmail.com", "Hello gmazzo65@gmail.com! this a test message sent to #random " + new Date());
    }

}
