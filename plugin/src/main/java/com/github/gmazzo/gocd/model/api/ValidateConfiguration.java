package com.github.gmazzo.gocd.model.api;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;

public class ValidateConfiguration {

    @SerializedName("plugin-settings")
    private Map<String, ValueHolder> settings = Collections.emptyMap();

    static class ValueHolder {

        @SerializedName("value")
        String value;

    }

    public String get(String value) {
        ValueHolder holder = settings.get(value);
        return holder != null ? holder.value : null;
    }

}
