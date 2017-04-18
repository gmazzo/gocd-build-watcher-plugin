package com.github.gmazzo.gocd.model;

import com.google.gson.annotations.SerializedName;

public class StageStatus {

    @SerializedName("pipeline")
    public Pipeline pipeline;

    public static class Pipeline {

        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public int counter;

        @SerializedName("label")
        public String label;

        @SerializedName("group")
        public String group;

        @SerializedName("stage")
        public Stage stage;

    }

    public static class Stage {

        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public int counter;

        @SerializedName("state")
        public String state;

        @SerializedName("result")
        public String result;

    }

}
