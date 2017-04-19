package com.github.gmazzo.gocd.model.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PipelineInstance {

    @SerializedName("build_cause")
    public BuildCause buildCause;

    @SerializedName("stages")
    public List<Stage> stages;

    public static class BuildCause {

        @SerializedName("material_revisions")
        public List<MaterialRevision> revisions;

    }

    public static class MaterialRevision {

        @SerializedName("modifications")
        public List<Modification> modifications;

    }

    public static class Modification {

        @SerializedName("modified_time")
        public long modifiedTime;

        @SerializedName("revision")
        public String revision;

        @SerializedName("email_address")
        public String emailAddress;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("comment")
        public String comment;

    }

    public static class Stage {

        @SerializedName("name")
        public String name;

        @SerializedName("result")
        public StageResult result;

    }

}
