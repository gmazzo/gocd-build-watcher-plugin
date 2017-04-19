package com.github.gmazzo.gocd.model.api;

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

        @SerializedName("stage")
        public Stage stage;

    }

    public static class Stage {

        @SerializedName("name")
        public String name;

        @SerializedName("counter")
        public int counter;

        @SerializedName("result")
        public StageResult result;

    }

}
