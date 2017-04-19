package com.github.gmazzo.gocd;

import com.github.gmazzo.gocd.model.Message;
import com.github.gmazzo.gocd.model.api.PipelineInstance;
import com.github.gmazzo.gocd.model.api.PluginSettings;
import com.github.gmazzo.gocd.model.api.StageResult;
import com.github.gmazzo.gocd.model.api.StageStatus;
import com.github.gmazzo.gocd.model.api.ValidateConfiguration;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SERVER_BASE_URL;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SLACK_API_TOKEN;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SLACK_BOT_USERNAME;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SLACK_CHANNEL;
import static com.github.gmazzo.utils.HttpUtils.response;
import static com.github.gmazzo.utils.IOUtils.readStream;
import static com.github.gmazzo.utils.MapUtils.map;
import static com.github.gmazzo.utils.StringUtils.capitalize;
import static com.github.gmazzo.utils.StringUtils.extractEmail;
import static com.github.gmazzo.utils.StringUtils.isBlank;

@Extension
public class BuildWatcherPlugin implements GoPlugin {
    private static final Logger LOGGER = Logger.getLoggerFor(BuildWatcherPlugin.class);
    private static final String GO_BASE_URL = "http://localhost:8153/go/api";
    private static final String USER_PLACEHOLDER = "%user%";
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
                handleStageStatus(status.pipeline);
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

    private PluginSettings getSettings() {
        DefaultGoApiRequest request = new DefaultGoApiRequest(
                "go.processor.plugin-settings.get",
                "1.0",
                pluginIdentifier()
        );
        request.setRequestBody(gson.toJson(map("plugin-id", "build-watcher.notifier")));
        GoApiResponse response = accessor.submit(request);

        PluginSettings settings = gson.fromJson(response.responseBody(), PluginSettings.class);
        return settings != null ? settings : new PluginSettings();
    }

    private void handleStageStatus(StageStatus.Pipeline pipeline) {
        PluginSettings settings = getSettings();

        StageResult currentResult = pipeline.stage.result;

        if (currentResult.isFinal()) {
            PipelineInstance currentInstance = getPipelineInstance(pipeline.name, pipeline.counter);
            PipelineInstance previousInstance = getPipelineInstance(pipeline.name, pipeline.counter - 1);
            StageResult previousResult = getPreviousStageResult(previousInstance, pipeline.stage.name);
            String userEmail = getMaterialUser(currentInstance);
            String changesResume = getChangesResume(currentInstance);
            Message message = getMessage(settings, pipeline, userEmail, currentResult, previousResult, changesResume);

            LOGGER.info("handleStageStatus: userEmail=" + userEmail + ", current=" + currentResult +
                    ", previous=" + previousResult + ", message=" + message);

            if (message != null) {
                getNotifiers(settings).forEach(n -> n.sendMessage(userEmail, message));
            }

        } else {
            LOGGER.info("Ignoring non final state: stage=" +
                    pipeline.name + '/' + pipeline.stage.name + ", stage=" + pipeline.stage.result);
        }
    }

    private PipelineInstance getPipelineInstance(String name, int counter) {
        try {
            URL url = new URL(GO_BASE_URL + "/pipelines/" + name + "/instance/" + counter);

            return gson.fromJson(new InputStreamReader(url.openStream()), PipelineInstance.class);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMaterialUser(PipelineInstance instance) {
        return instance.buildCause.revisions.stream()
                .flatMap(m -> m.modifications.stream())
                .sorted((a, b) -> -Long.compare(a.modifiedTime, b.modifiedTime))
                .findFirst()
                .map(m -> m.emailAddress != null ? m.emailAddress : extractEmail(m.userName))
                .filter($ -> $ != null)
                .orElseThrow(() -> new IllegalArgumentException("No modifications found for material!"));
    }

    private StageResult getPreviousStageResult(PipelineInstance instance, String stage) {
        return instance.stages.stream()
                .filter(s -> stage.equals(s.name))
                .findAny()
                .map(s -> s.result)
                .orElse(StageResult.PASSED);
    }

    private String getChangesResume(PipelineInstance instance) {
        return instance.buildCause.revisions.stream()
                .flatMap($ -> $.modifications.stream())
                .map($ -> $.revision + ":\n" + $.comment + " - " + $.userName)
                .collect(Collectors.joining("\n\n"));
    }

    private Message getMessage(PluginSettings settings, StageStatus.Pipeline pipeline, String userEmail, StageResult current, StageResult previous, String changesResume) {
        String id = pipeline.name + '/' + pipeline.counter + '/' + pipeline.stage.name + '/' + pipeline.stage.counter;
        String text = null;
        String link = null;
        Message.Type type = null;

        if (previous.isSucceed() && !current.isSucceed()) {
            text = "Hello " + USER_PLACEHOLDER + ", you have broken the build on pipeline " + id;
            type = Message.Type.BAD;

        } else if (!previous.isSucceed()) {
            if (current.isSucceed()) {
                text = "Great job " + USER_PLACEHOLDER + "! The pipeline " + id + " has been fixed";
                type = Message.Type.GOOD;

            } else {
                text = "Hello " + USER_PLACEHOLDER + ", the pipeline " + id + " is still broken";
                type = Message.Type.BAD;
            }
        }
        if (!isBlank(settings.serverBaseUrl)) {
            link = settings.serverBaseUrl.replaceFirst("/$", "") +
                    "/pipelines/" + pipeline.name + '/' + pipeline.counter +
                    '/' + pipeline.stage.name + '/' + pipeline.stage.counter;
        }
        return text == null ? null : new Message.Builder()
                .text(text.replaceAll(USER_PLACEHOLDER, userEmail))
                .link(link)
                .type(type)
                .tag("Pipeline", pipeline.name + '/' + pipeline.counter)
                .tag("Stage", pipeline.stage.name + '/' + pipeline.stage.counter)
                .tag("Label", pipeline.label)
                .tag("Status", capitalize(current.name()))
                .longTag("Changes", changesResume)
                .build();
    }

    private List<Notifier> getNotifiers(PluginSettings settings) {
        if (accessor != null) {
            List<Notifier> notifs = new ArrayList<>(2);
            if (!isBlank(settings.slackAPIToken)) {
                notifs.add(new SlackNotifier(settings.slackAPIToken, settings.slackChannel, settings.slackBotUsername));
            }
            return notifs;
        }
        return Collections.emptyList();
    }

}
