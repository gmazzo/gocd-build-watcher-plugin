package com.github.gmazzo.gocd.model.api;

import com.google.gson.annotations.SerializedName;

public class PluginSettings {
    public static final String SETTING_SERVER_BASE_URL = "server_base_url";
    public static final String SETTING_SLACK_API_TOKEN = "slack_api_token";
    public static final String SETTING_SLACK_CHANNEL = "slack_channel";
    public static final String SETTING_SLACK_BOT_USERNAME = "slack_bot_username";
    public static final String SETTING_EMAIL_SMTP_SERVER = "email_smtp_server";
    public static final String SETTING_EMAIL_SMTP_PORT = "email_smtp_port";
    public static final String SETTING_EMAIL_SMTP_SSL = "email_smtp_ssl";
    public static final String SETTING_EMAIL_AUTH_USER = "email_auth_user";
    public static final String SETTING_EMAIL_AUTH_PASSWORD = "email_auth_password";
    public static final String SETTING_EMAIL_FROM = "email_from";
    public static final String SETTING_EMAIL_CC = "email_cc";

    @SerializedName(SETTING_SERVER_BASE_URL)
    public String serverBaseUrl;

    @SerializedName(SETTING_SLACK_API_TOKEN)
    public String slackAPIToken;

    @SerializedName(SETTING_SLACK_CHANNEL)
    public String slackChannel;

    @SerializedName(SETTING_SLACK_BOT_USERNAME)
    public String slackBotUsername;

    @SerializedName(SETTING_EMAIL_SMTP_SERVER)
    public String emailSMTPServer;

    @SerializedName(SETTING_EMAIL_SMTP_PORT)
    public int emailSMTPPort;

    @SerializedName(SETTING_EMAIL_SMTP_SSL)
    public boolean emailSMTPSSL;

    @SerializedName(SETTING_EMAIL_AUTH_USER)
    public String emailAuthUser;

    @SerializedName(SETTING_EMAIL_AUTH_PASSWORD)
    public String emailAuthPassword;

    @SerializedName(SETTING_EMAIL_FROM)
    public String emailFrom;

    @SerializedName(SETTING_EMAIL_CC)
    public String emailCC;

}
