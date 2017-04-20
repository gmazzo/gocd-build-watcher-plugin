<html>
    <head>
        <base href="../">
        <title>GoCD Build Watcher - Slack Integration</title>
        <link rel="stylesheet" href="https://a.slack-edge.com/00dbf/style/plastic_typography.css" />
        <link rel="stylesheet" href="https://a.slack-edge.com/3ea1/style/plastic_forms.css" />
        <link rel="stylesheet" href="https://a.slack-edge.com/a929/style/plastic_layout.css" />
        <link rel="stylesheet" href="https://a.slack-edge.com/7ba3/style/plastic_helpers.css" />
        <link rel="stylesheet" href="https://a.slack-edge.com/de779/style/plastic_buttons.css" />
        <style>

            .card {
                display: block;
                width: 500px;
            }

            .inline_textfield_button {
                float: right;
                margin-top: -41px;
                margin-right: 4px;
            }

        </style>
        <script src="https://code.jquery.com/jquery-3.2.1.min.js"></script>
        <script src="static/base.js"></script>
        <script>

            $(document).ready(function() {

                $('#copy').click(function() {
                    copyToClipboard($('#oauth_access_token').val())
                })

            })

        </script>
    </head>
    <body>
        <div id="auth-card" class="card clearfix large_bottom_padding">
            <h2>Activate Slack integration</h2>
            <p>Complete the process by copying the token into GoCD plugin's configuration</p>
            <label for="oauth_access_token">OAuth Access Token</label>
            <input type="text" id="oauth_access_token" class="small disabled" disabled="" value="<%= request.getAttribute("token") %>">
            <div class="inline_textfield_button">
                <button id="copy" type="button" class="btn btn_small btn_outline" >Copy</button>
            </div>
            <p>
                When ready, continue the tutorial here:
                <a href="https://github.com/gmazzo/gocd-build-watcher-plugin#adding-slack-notifications">https://github.com/gmazzo/gocd-build-watcher-plugin#adding-slack-notifications</a>
            </p>
        </div>
        <img src="static/settings1.png" class="card clearfix large_bottom_padding" />
        <img src="static/settings2.png" class="card clearfix large_bottom_padding" />
     </body>
</html>
