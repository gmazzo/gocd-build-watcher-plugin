package com.github.gmazzo.gocd.model.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.junit.Test;

import java.util.function.Consumer;

import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_SMTP_PORT;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_SMTP_SSL;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_MESSAGE_PIPE_BROKEN;
import static org.junit.Assert.assertEquals;

public class PluginSettingsTest {
    private final Gson gson = new Gson();
    private final PluginSettings defaults = new PluginSettings();

    private PluginSettings changeAndSerialize(Consumer<JsonObject> action) {
        JsonObject jsonObject = (JsonObject) gson.toJsonTree(new PluginSettings());
        action.accept(jsonObject);
        return PluginSettings.fromJSON(gson.toJson(jsonObject));
    }

    @Test
    public void testDeserializationAndDefaults_String() {
        PluginSettings settings = changeAndSerialize(($) -> $.addProperty(SETTING_MESSAGE_PIPE_BROKEN, ""));

        assertEquals("Defaults no taken when empty string is stored!", defaults.messagePipeBroken, settings.messagePipeBroken);
    }

    @Test
    public void testDeserializationAndDefaults_Int() {
        PluginSettings settings = changeAndSerialize(($) -> $.addProperty(SETTING_EMAIL_SMTP_PORT, ""));

        assertEquals("Defaults no taken when empty string is stored!", defaults.emailSMTPPort, settings.emailSMTPPort);
    }

    @Test
    public void testDeserializationAndDefaults_Boolean() {
        PluginSettings settings = changeAndSerialize(($) -> $.addProperty(SETTING_EMAIL_SMTP_SSL, ""));

        assertEquals("Defaults no taken when empty string is stored!", defaults.emailSMTPSSL, settings.emailSMTPSSL);
    }

}
