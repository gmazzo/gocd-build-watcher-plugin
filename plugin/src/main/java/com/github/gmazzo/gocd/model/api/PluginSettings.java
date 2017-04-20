package com.github.gmazzo.gocd.model.api;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import static com.github.gmazzo.utils.StringUtils.isBlank;

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

    @SerializedName(SETTING_SLACK_API_TOKEN)
    public String slackAPIToken;

    @SerializedName(SETTING_SLACK_CHANNEL)
    public String slackChannel = "#general";

    @SerializedName(SETTING_SLACK_BOT_USERNAME)
    public String slackBotUsername;

    @SerializedName(SETTING_EMAIL_SMTP_SERVER)
    public String emailSMTPServer = "smtp.gmail.com";

    @JsonAdapter(SafeNumberTypeAdapter.class)
    @SerializedName(SETTING_EMAIL_SMTP_PORT)
    public int emailSMTPPort = 465;

    @JsonAdapter(SafeBooleanTypeAdapter.class)
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

}

class SafeNumberTypeAdapter extends TypeAdapter<Double> {

    @Override
    public void write(JsonWriter out, Double value) throws IOException {
        out.value(value);
    }

    @Override
    public Double read(JsonReader in) throws IOException {
        String value = in.nextString();
        return isBlank(value) ? null : Double.parseDouble(value);
    }

}

class SafeBooleanTypeAdapter extends TypeAdapter<Boolean> {

    @Override
    public void write(JsonWriter out, Boolean value) throws IOException {
        out.value(value);
    }

    @Override
    public Boolean read(JsonReader in) throws IOException {
        String value = in.nextString();
        return isBlank(value) ? null : Boolean.parseBoolean(value);
    }

}
