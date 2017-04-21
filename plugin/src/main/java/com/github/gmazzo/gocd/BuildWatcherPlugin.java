package com.github.gmazzo.gocd;

import com.github.gmazzo.gocd.email.EmailNotifier;
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
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_LABEL;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_PIPELINE;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_PIPELINE_COUNTER;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_STAGE;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_STAGE_COUNTER;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_STATE_CURRENT;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_STATE_PREVIOUS;
import static com.github.gmazzo.gocd.model.api.PluginSettings.PLACEHOLDER_USER;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_AUTH_PASSWORD;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_AUTH_USER;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_CC;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_FROM;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_SMTP_PORT;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_SMTP_SERVER;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_EMAIL_SMTP_SSL;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_MESSAGE_PIPE_BROKEN;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_MESSAGE_PIPE_FIXED;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_MESSAGE_PIPE_STILL_BROKEN;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SERVER_API_PASSWORD;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SERVER_API_USERNAME;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SERVER_BASE_URL;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SLACK_API_TOKEN;
import static com.github.gmazzo.gocd.model.api.PluginSettings.SETTING_SLACK_BOT_IMAGE;
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
        LOGGER.debug("request: name=" + requestMessage.requestName() + ", body:" + requestMessage.requestBody());

        switch (requestMessage.requestName()) {
            case "notifications-interested-in":
                return response(map("notifications", Collections.singletonList("stage-status")));

            case "go.plugin-settings.get-configuration":
                PluginSettings defaults = new PluginSettings();
                return response(map(
                        SETTING_SERVER_BASE_URL, configItem("Server Base URL", defaults.serverBaseUrl, false, false),
                        SETTING_SERVER_API_USERNAME, configItem("Server API Username", defaults.serverAPIUsername, true, false),
                        SETTING_SERVER_API_PASSWORD, configItem("Server API Username", defaults.serverAPIPassword, true, true),
                        SETTING_SLACK_API_TOKEN, configItem("Slack API Token", defaults.slackAPIToken, true, true),
                        SETTING_SLACK_CHANNEL, configItem("Slack Channel", defaults.slackChannel, false, false),
                        SETTING_SLACK_BOT_USERNAME, configItem("Slack Bot Username", defaults.slackBotUsername, false, false),
                        SETTING_SLACK_BOT_IMAGE, configItem("Slack Bot Image", defaults.slackBotImage, false, false),
                        SETTING_EMAIL_SMTP_SERVER, configItem("Email SMTP Server", defaults.emailSMTPServer, true, false),
                        SETTING_EMAIL_SMTP_PORT, configItem("Email SMTP Port", String.valueOf(defaults.emailSMTPPort), true, false),
                        SETTING_EMAIL_SMTP_SSL, configItem("Email SMTP SSL", String.valueOf(defaults.emailSMTPSSL), true, false),
                        SETTING_EMAIL_AUTH_USER, configItem("Email SMTP Authentication Username", defaults.emailAuthUser, false, false),
                        SETTING_EMAIL_AUTH_PASSWORD, configItem("Email SMTP Authentication Password", defaults.emailAuthPassword, false, true),
                        SETTING_EMAIL_FROM, configItem("Email From", defaults.emailFrom, true, false),
                        SETTING_EMAIL_CC, configItem("Email CC", defaults.emailCC, false, false),
                        SETTING_MESSAGE_PIPE_BROKEN, configItem("Message Pipeline Broken", defaults.messagePipeBroken, true, false),
                        SETTING_MESSAGE_PIPE_STILL_BROKEN, configItem("Message Pipeline Still Broken", defaults.messagePipeStillBroken, true, false),
                        SETTING_MESSAGE_PIPE_FIXED, configItem("Message Pipeline Fixed", defaults.messagePipeFixed, true, false)));

            case "go.plugin-settings.validate-configuration":
                ValidateConfiguration configuration = gson.fromJson(requestMessage.requestBody(), ValidateConfiguration.class);

                List<Map<String, String>> errors = new LinkedList<>();

                // Email validation
                if (!isBlank(configuration.get(SETTING_EMAIL_SMTP_SERVER)) && isBlank(configuration.get(SETTING_EMAIL_FROM))) {
                    errors.add(map("key", SETTING_EMAIL_FROM, "message", "The field From is required"));
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

        LOGGER.debug("getSettings: " + response.responseBody());

        return PluginSettings.fromJSON(response.responseBody());
    }

    private void handleStageStatus(StageStatus.Pipeline pipeline) {
        PluginSettings settings = getSettings();

        StageResult currentResult = pipeline.stage.result;

        if (currentResult.isFinal()) {
            PipelineInstance currentInstance = getPipelineInstance(settings, pipeline.name, pipeline.counter);
            PipelineInstance previousInstance = getPipelineInstance(settings, pipeline.name, pipeline.counter - 1);
            StageResult previousResult = getPreviousStageResult(previousInstance, pipeline.stage.name);
            String userEmail = getMaterialUser(currentInstance);
            String changesResume = getChangesResume(currentInstance);
            Message message = getMessage(settings, userEmail, pipeline, currentResult, previousResult, changesResume);

            LOGGER.info("handleStageStatus: userEmail=" + userEmail + ", current=" + currentResult +
                    ", previous=" + previousResult + ", message=" + message);

            if (message != null) {
                // sends the notification
                getNotifiers(settings).forEach(n -> {
                    try {
                        n.sendMessage(userEmail, message);

                    } catch (Exception e) {
                        LOGGER.error("sendMessage failed: notifier=" + n +
                                ", userEmail=" + userEmail + ", current=" + currentResult +
                                ", previous=" + previousResult + ", message=" + message, e);
                    }
                });
            }

        } else {
            LOGGER.info("Ignoring non final state: stage=" +
                    pipeline.name + '/' + pipeline.stage.name + ", stage=" + pipeline.stage.result);
        }
    }

    private URLConnection fillAPIAuth(URLConnection connection, PluginSettings settings) {
        if (!isBlank(settings.serverAPIUsername) && !isBlank(settings.serverAPIPassword)) {
            String userpass = settings.serverAPIUsername + ":" + settings.serverAPIPassword;
            String basicAuth = "Basic " + DatatypeConverter.printBase64Binary(userpass.getBytes());
            connection.setRequestProperty("Authorization", basicAuth);
        }
        return connection;
    }

    private PipelineInstance getPipelineInstance(PluginSettings settings, String name, int counter) {
        try {
            URLConnection connection = new URL(GO_BASE_URL + "/pipelines/" + name + "/instance/" + counter).openConnection();
            fillAPIAuth(connection, settings);

            return gson.fromJson(new InputStreamReader(connection.getInputStream()), PipelineInstance.class);

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

    private Message getMessage(PluginSettings settings, String userEmail, StageStatus.Pipeline pipeline, StageResult current, StageResult previous, String changesResume) {
        String message = null;
        String link = null;
        Message.Type type = null;

        if (previous.isSucceed() && !current.isSucceed()) {
            message = settings.messagePipeBroken;
            type = Message.Type.BAD;

        } else if (!previous.isSucceed()) {
            if (current.isSucceed()) {
                message = settings.messagePipeFixed;
                type = Message.Type.GOOD;

            } else {
                message = settings.messagePipeStillBroken;
                type = Message.Type.BAD;
            }
        }
        if (!isBlank(settings.serverBaseUrl)) {
            link = settings.serverBaseUrl.replaceFirst("/$", "") +
                    "/pipelines/" + pipeline.name + '/' + pipeline.counter +
                    '/' + pipeline.stage.name + '/' + pipeline.stage.counter;
        }
        if (!isBlank(message)) {
            String text = message.replaceAll(PLACEHOLDER_USER, userEmail)
                    .replaceAll(PLACEHOLDER_PIPELINE, pipeline.name)
                    .replaceAll(PLACEHOLDER_PIPELINE_COUNTER, String.valueOf(pipeline.counter))
                    .replaceAll(PLACEHOLDER_STAGE, pipeline.stage.name)
                    .replaceAll(PLACEHOLDER_STAGE_COUNTER, String.valueOf(pipeline.stage.counter))
                    .replaceAll(PLACEHOLDER_LABEL, pipeline.label)
                    .replaceAll(PLACEHOLDER_STATE_CURRENT, current.name())
                    .replaceAll(PLACEHOLDER_STATE_PREVIOUS, previous.name());

            return new Message.Builder()
                    .text(text)
                    .link(link)
                    .type(type)
                    .tag("Pipeline", pipeline.name + '/' + pipeline.counter)
                    .tag("Stage", pipeline.stage.name + '/' + pipeline.stage.counter)
                    .tag("Label", pipeline.label)
                    .tag("Status", capitalize(current.name()))
                    .longTag("Changes", changesResume)
                    .build();
        }
        return null;
    }

    private List<Notifier> getNotifiers(PluginSettings settings) {
        if (accessor != null) {
            List<Notifier> notifs = new ArrayList<>(2);

            if (!isBlank(settings.slackAPIToken)) {
                notifs.add(new SlackNotifier(settings.slackAPIToken, settings.slackChannel,
                        settings.slackBotUsername, settings.slackBotImage));
            }

            if (!isBlank(settings.emailSMTPServer) && !isBlank(settings.emailFrom)) {
                notifs.add(new EmailNotifier(settings.emailSMTPServer, settings.emailSMTPPort, settings.emailAuthUser,
                        settings.emailAuthPassword, settings.emailSMTPSSL, settings.emailFrom, settings.emailCC));
            }

            return notifs;
        }
        return Collections.emptyList();
    }

}
