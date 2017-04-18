package com.github.gmazzo.utils;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

public final class HttpUtils {
    private static final int STATUS_OK = 200;
    private static final Gson GSON = new Gson();

    public static GoPluginApiResponse response(Object body) {
        DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(STATUS_OK);
        if (body != null) {
            response.setResponseBody(GSON.toJson(body));
        }
        return response;
    }

}
