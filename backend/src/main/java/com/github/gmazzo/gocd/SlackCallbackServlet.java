package com.github.gmazzo.gocd;

import com.github.gmazzo.utils.PropertiesUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SlackCallbackServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private String clientId;
    private String clientSecret;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        Properties properties = PropertiesUtils.get();
        clientId = properties.getProperty("slack-app.clientId");
        clientSecret = properties.getProperty("slack-app.clientSecret");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");

        URL url = new URL("https://slack.com/api/oauth.access?client_id=" + clientId +
                "&client_secret=" + clientSecret + "&code=" + code);
        AccessResponse response = gson.fromJson(new InputStreamReader(url.openStream()), AccessResponse.class);

        req.setAttribute("token", response.ok ? response.accessToken : response.error);

        getServletContext().getRequestDispatcher("/slack-callback.jsp").forward(req, resp);
    }

}

class AccessResponse {

    @SerializedName("ok")
    boolean ok;

    @SerializedName("error")
    String error;

    @SerializedName("access_token")
    String accessToken;

}
