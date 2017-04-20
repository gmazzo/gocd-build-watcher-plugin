package com.github.gmazzo.gocd.slack;

import com.github.gmazzo.gocd.Notifier;
import com.github.gmazzo.gocd.model.Message;
import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.SlackApiResponse;
import com.github.seratch.jslack.api.methods.request.chat.ChatPostMessageRequest;
import com.github.seratch.jslack.api.methods.request.users.UsersListRequest;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.model.User;

import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.github.gmazzo.utils.StringUtils.isBlank;

public class SlackNotifier implements Notifier {
    private final Slack slack = Slack.getInstance();
    private final String token;
    private final String channel;
    private final String username;

    public SlackNotifier(String token, String channel, String username) {
        this.token = token;
        this.channel = !isBlank(channel) ? channel.replaceFirst("(?!#)", "#") : null;
        this.username = username;
    }

    @Override
    public void sendMessage(String userEmail, Message message) {
        User user = detectSlackUser(userEmail);

        String text = user != null ?
                message.text.replaceAll("\\b" + Pattern.quote(userEmail) + "\\b", "<@" + user.getName() + ">") :
                message.text;
        String channel = this.channel != null ? this.channel : user != null ? "@" + user.getName() : "#general";

        checkResponse(() -> slack.methods().chatPostMessage(ChatPostMessageRequest.builder()
                .token(token)
                .text(text)
                .channel(channel)
                .username(username)
                .attachments(Collections.singletonList(Attachment.builder()
                        .title(message.link)
                        .titleLink(message.link)
                        .fields(message.tags.stream()
                                .map($ -> Field.builder()
                                        .title($.name)
                                        .value($.value)
                                        .valueShortEnough($.isShort)
                                        .build())
                                .collect(Collectors.toList()))
                        .color(message.type == Message.Type.GOOD ? "good" :
                                message.type == Message.Type.BAD ? "danger" : null)
                        .build()))
                .build()));
    }

    private User detectSlackUser(String userEmail) {
        return checkResponse(() -> slack.methods().usersList(UsersListRequest.builder().token(token).build()))
                .getMembers().stream()
                .filter(u -> u.getProfile() != null && userEmail.equalsIgnoreCase(u.getProfile().getEmail()))
                .findAny()
                .orElse(null);
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
