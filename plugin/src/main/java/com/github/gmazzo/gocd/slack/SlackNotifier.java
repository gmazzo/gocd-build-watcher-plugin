package com.github.gmazzo.gocd.slack;

import com.github.gmazzo.gocd.Notifier;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersListRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class SlackNotifier implements Notifier {
    private final Slack slack = Slack.getInstance();
    private final String token;
    private final String channel;
    private final String username;

    public SlackNotifier(String token, String channel, String username) {
        this.token = token;
        this.channel = channel != null ? channel.replaceFirst("(?!#)", "#") : null;
        this.username = username;
    }

    @Override
    public void sendMessage(String userEmail, String message) {
        Optional<String> user = detectSlackUser(userEmail);

        String slackId = user.orElse(userEmail);
        String text = user.isPresent() ? message.replaceAll("\\b" + Pattern.quote(userEmail) + "\\b", slackId) : message;
        String channel = this.channel != null ? this.channel : user.isPresent() ? slackId : "#general";

        checkResponse(() -> slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
                .token(token)
                .text(text)
                .channel(channel)
                .username(username)
                .build()));
    }

    private Optional<String> detectSlackUser(String userEmail) {
        return checkResponse(() -> slack.methods().usersList(UsersListRequest.builder().token(token).build()))
                .getMembers().stream()
                .filter(u -> u.getProfile() != null && userEmail.equalsIgnoreCase(u.getProfile().getEmail()))
                .findAny()
                .map(u -> "@" + u.getName());
    }

    private <U extends SlackApiResponse> U checkResponse(ApiCall<U> call) {
        try {
            U r = call.call();
            if (r.isOk()) {
                return r;
            }
            throw new RuntimeException(r.getError());

        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }

}

@FunctionalInterface
interface ApiCall<U> {

    U call() throws SlackApiException, IOException;

}
