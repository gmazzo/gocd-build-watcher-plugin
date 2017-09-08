package com.github.gmazzo.gocd.model.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.Iterator;
import java.util.Map;

import static com.github.gmazzo.utils.StringUtils.isBlank;

public class PluginSettings {
    public static final String SETTING_SERVER_BASE_URL = "server_base_url";
    public static final String SETTING_SERVER_API_USERNAME = "server_api_username";
    public static final String SETTING_SERVER_API_PASSWORD = "server_api_password";
    public static final String SETTING_SLACK_API_TOKEN = "slack_api_token";
    public static final String SETTING_SLACK_CHANNEL = "slack_channel";
    public static final String SETTING_SLACK_BOT_USERNAME = "slack_bot_username";
    public static final String SETTING_SLACK_BOT_IMAGE = "slack_bot_image";
    public static final String SETTING_EMAIL_SMTP_SERVER = "email_smtp_server";
    public static final String SETTING_EMAIL_SMTP_PORT = "email_smtp_port";
    public static final String SETTING_EMAIL_SMTP_SSL = "email_smtp_ssl";
    public static final String SETTING_EMAIL_AUTH_USER = "email_auth_user";
    public static final String SETTING_EMAIL_AUTH_PASSWORD = "email_auth_password";
    public static final String SETTING_EMAIL_FROM = "email_from";
    public static final String SETTING_EMAIL_CC = "email_cc";
    public static final String SETTING_MESSAGE_PIPE_BROKEN = "message_pipe_broken";
    public static final String SETTING_MESSAGE_PIPE_STILL_BROKEN = "message_pipe_still_broken";
    public static final String SETTING_MESSAGE_PIPE_FIXED = "message_pipe_fixed";
    public static final String PLACEHOLDER_USER = "%user%";
    public static final String PLACEHOLDER_PIPELINE = "%pipeline%";
    public static final String PLACEHOLDER_PIPELINE_COUNTER = "%pipeline-count%";
    public static final String PLACEHOLDER_STAGE = "%stage%";
    public static final String PLACEHOLDER_STAGE_COUNTER = "%stage-count%";
    public static final String PLACEHOLDER_LABEL = "%label%";
    public static final String PLACEHOLDER_STATE_CURRENT = "%current-state%";
    public static final String PLACEHOLDER_STATE_PREVIOUS = "%previous-state%";
    private static final String PLACEHOLDER_PIPELINE_ID =
            PLACEHOLDER_PIPELINE + '/' + PLACEHOLDER_PIPELINE_COUNTER + '/' +
                    PLACEHOLDER_STAGE + '/' + PLACEHOLDER_STAGE_COUNTER;

    @SerializedName(SETTING_SERVER_BASE_URL)
    public String serverBaseUrl = "http://localhost:8153/go/";

    @SerializedName(SETTING_SERVER_API_USERNAME)
    public String serverAPIUsername;

    @SerializedName(SETTING_SERVER_API_PASSWORD)
    public String serverAPIPassword;

    @SerializedName(SETTING_SLACK_API_TOKEN)
    public String slackAPIToken;

    @SerializedName(SETTING_SLACK_CHANNEL)
    public String slackChannel;

    @SerializedName(SETTING_SLACK_BOT_USERNAME)
    public String slackBotUsername;

    @SerializedName(SETTING_SLACK_BOT_IMAGE)
    public String slackBotImage;

    @SerializedName(SETTING_EMAIL_SMTP_SERVER)
    public String emailSMTPServer = "smtp.gmail.com";

    @SerializedName(SETTING_EMAIL_SMTP_PORT)
    public int emailSMTPPort = 465;

    @SerializedName(SETTING_EMAIL_SMTP_SSL)
    public boolean emailSMTPSSL = true;

    @SerializedName(SETTING_EMAIL_AUTH_USER)
    public String emailAuthUser;

    @SerializedName(SETTING_EMAIL_AUTH_PASSWORD)
    public String emailAuthPassword;

    @SerializedName(SETTING_EMAIL_FROM)
    public String emailFrom;

    @SerializedName(SETTING_EMAIL_CC)
    public String emailCC;

    @SerializedName(SETTING_MESSAGE_PIPE_BROKEN)
    public String messagePipeBroken = "Hello " + PLACEHOLDER_USER + ", you have broken the build on pipeline " + PLACEHOLDER_PIPELINE_ID;

    @SerializedName(SETTING_MESSAGE_PIPE_STILL_BROKEN)
    public String messagePipeStillBroken = "Hello " + PLACEHOLDER_USER + ", the pipeline " + PLACEHOLDER_PIPELINE_ID + " is still broken";

    @SerializedName(SETTING_MESSAGE_PIPE_FIXED)
    public String messagePipeFixed = "Great job " + PLACEHOLDER_USER + "! The pipeline " + PLACEHOLDER_PIPELINE_ID + " has been fixed";

    public static PluginSettings fromJSON(String json) {
        if (!isBlank(json)) {
            Gson gson = new Gson();
            JsonObject object = gson.fromJson(json, JsonObject.class);

            // ugly hack, but sadly GoCD has a very poor support for configuration
            for (Iterator<Map.Entry<String, JsonElement>> it = object.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, JsonElement> e = it.next();

                if (isBlank(e.getValue().getAsString())) {
                    it.remove();
                }
            }

            return gson.fromJson(object, PluginSettings.class);
        }
        return new PluginSettings();
    }

}
