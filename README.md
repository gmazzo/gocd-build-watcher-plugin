# gocd-build-watcher-plugin
A GoCD notification plugin which sends direct emails and Slack messages to the person who brakes a build Edit
Add topics

# Installation
1. Download the lastest release JAR from [jCenter](https://dl.bintray.com/gmazzo/maven/com/github/gmazzo/gocd/build-watcher-plugin/0.1/:build-watcher-plugin-0.1.jar)
1. Drop it under `$GOCD_DIR/plugins/external`
1. Restart Go Server

## Configuration
### Adding Slack notifications
As this pluging needs to resolve user's email into a Slack ID, a WebHook is not enough to work.
You need to install our Slack App into your team and get an API token.
1. [![Add to Slack](https://platform.slack-edge.com/img/add_to_slack.png)](https://slack.com/oauth/authorize?&client_id=170776918258.170870737557&scope=chat:write:bot,users:read.email,users:read)
2. Copy your Slack API Token into GoCD's plugin configuration
3. (optional) Configure a custom channel or slack id for the bot.
