package com.github.gmazzo.gocd.model;

import com.google.gson.annotations.SerializedName;

public class PluginSettings {
    public static final String SETTING_SERVER_BASE_URL = "server_base_url";
    public static final String SETTING_SLACK_API_TOKEN = "slack_api_token";
    public static final String SETTING_SLACK_CHANNEL = "slack_channel";
    public static final String SETTING_SLACK_BOT_USERNAME = "slack_bot_username";

    @SerializedName(SETTING_SERVER_BASE_URL)
    public String serverBaseUrl;

    @SerializedName(SETTING_SLACK_API_TOKEN)
    public String slackAPIToken;

    @SerializedName(SETTING_SLACK_CHANNEL)
    public String slackChannel;

    @SerializedName(SETTING_SLACK_BOT_USERNAME)
    public String slackBotUsername;

}
