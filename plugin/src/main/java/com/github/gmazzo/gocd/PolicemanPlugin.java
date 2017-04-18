package com.github.gmazzo.gocd;

import com.github.gmazzo.gocd.model.PluginSettings;
import com.github.gmazzo.gocd.model.StageStatus;
import com.github.gmazzo.gocd.model.ValidateConfiguration;
import com.github.gmazzo.gocd.slack.SlackNotifier;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.github.gmazzo.gocd.model.PluginSettings.SETTING_SERVER_BASE_URL;
import static com.github.gmazzo.gocd.model.PluginSettings.SETTING_SLACK_API_TOKEN;
import static com.github.gmazzo.gocd.model.PluginSettings.SETTING_SLACK_BOT_USERNAME;
import static com.github.gmazzo.gocd.model.PluginSettings.SETTING_SLACK_CHANNEL;
import static com.github.gmazzo.utils.HttpUtils.response;
import static com.github.gmazzo.utils.IOUtils.readStream;
import static com.github.gmazzo.utils.MapUtils.map;
import static com.github.gmazzo.utils.StringUtils.isBlank;

@Extension
public class PolicemanPlugin implements GoPlugin {
    private static final Logger LOGGER = Logger.getLoggerFor(PolicemanPlugin.class);
    private final Gson gson = new Gson();
    private GoApplicationAccessor accessor;

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("notification", Collections.singletonList("1.0"));
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest requestMessage) throws UnhandledRequestTypeException {
        LOGGER.info("request: name=" + requestMessage.requestName() + ", body:" + requestMessage.requestBody());

        switch (requestMessage.requestName()) {
            case "notifications-interested-in":
                return response(map("notifications", Collections.singletonList("stage-status")));

            case "go.plugin-settings.get-configuration":
                return response(map(
                        SETTING_SERVER_BASE_URL, configItem("Server Base URL", "The server base URL", false, false),
                        SETTING_SLACK_API_TOKEN, configItem("Slack API Token", "The API OAuth token for Slack API", true, true),
                        SETTING_SLACK_CHANNEL, configItem("Slack Channel", "The slack target channel", false, false),
                        SETTING_SLACK_BOT_USERNAME, configItem("Slack Bot Username", "The slack bot username", false, false)));

            case "go.plugin-settings.validate-configuration":
                ValidateConfiguration configuration = gson.fromJson(requestMessage.requestBody(), ValidateConfiguration.class);

                List<Map<String, String>> errors = new LinkedList<>();
                if (isBlank(configuration.get(SETTING_SLACK_API_TOKEN))) {
                    errors.add(map("key", SETTING_SLACK_API_TOKEN, "message", "Slack API Token not specified"));
                }
                return response(errors);

            case "go.plugin-settings.get-view":
                return response(map("template", readStream(getClass().getResourceAsStream("/plugin-settings.template.html"))));

            case "stage-status":
                StageStatus status = gson.fromJson(requestMessage.requestBody(), StageStatus.class);
                // TODO programar
                return response(map("status", "success"));
        }
        return null;
    }

    private Map<?, ?> configItem(String displayName, String defaultValue, boolean required, boolean secure) {
        return map("display-name", displayName,
                "display-value", defaultValue,
                "required", required,
                "secure", secure);
    }

    private List<Notifier> getNotifiers() {
        if (accessor != null) {
            PluginSettings settings = getSettings();

            if (settings != null) {
                List<Notifier> notifs = new ArrayList<>(2);
                if (!isBlank(settings.slackAPIToken)) {
                    notifs.add(new SlackNotifier(settings.slackAPIToken, settings.slackChannel, settings.slackBotUsername));
                }
                return notifs;

            } else {
                LOGGER.warn("Plugin not configured!");
            }
        }
        return Collections.emptyList();
    }

    private PluginSettings getSettings() {
        DefaultGoApiRequest request = new DefaultGoApiRequest(
                "go.processor.plugin-settings.get",
                "1.0",
                pluginIdentifier()
        );
        request.setRequestBody(gson.toJson(map("plugin-id", "policeman.notifier")));
        GoApiResponse response = accessor.submit(request);
        return gson.fromJson(response.responseBody(), PluginSettings.class);
    }

}
