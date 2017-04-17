package com.github.gmazzo.gocd;

import com.github.gmazzo.gocd.model.StageStatus;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import static com.github.gmazzo.utils.HttpUtils.response;
import static com.github.gmazzo.utils.MapUtils.map;

@Extension
public class PolicemanPlugin implements GoPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicemanPlugin.class);
    private final Gson gson = new Gson();

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("notification", Collections.singletonList("1.0"));
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest requestMessage) throws UnhandledRequestTypeException {
        LOGGER.debug("request: name=%s, body:\n", requestMessage.requestName(), requestMessage.requestBody());

        switch (requestMessage.requestName()) {
            case "notifications-interested-in":
                return response(map("notifications", Collections.singletonList("stage-status")));

            case "go.plugin-settings.get-configuration":
                return response(map(
                        "slack-webhook", configItem("Slack Web-Hook URL", "", false, true)));

            case "go.plugin-settings.get-view":
                String content = new Scanner(getClass().getResourceAsStream("/plugin-settings.html")).useDelimiter("\\Z").next();
                return response(map("template", content));

            case "stage-status":
                StageStatus status = gson.fromJson(requestMessage.requestBody(), StageStatus.class);
                // TODO programar
                return response(Collections.emptyMap());
        }
        return null;
    }

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
    }

    private Map<?, ?> configItem(String displayName, String defaultValue, boolean required, boolean secure) {
        return map("display-name", displayName,
                "display-value", defaultValue,
                "required", required,
                "secure", secure);
    }

}
