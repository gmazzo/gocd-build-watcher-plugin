package com.github.gmazzo.gocd.model.api;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.stream.Stream;

@JsonAdapter(StageResultTypeAdapter.class)
public enum StageResult {

    PASSED, FAILED, CANCELLED, UNKNOWN;

    public boolean isFinal() {
        return this != UNKNOWN;
    }

    public boolean isSucceed() {
        return this == PASSED;
    }

}

class StageResultTypeAdapter extends SafeEnumTypeAdapter<StageResult> {

    public StageResultTypeAdapter() {
        super(StageResult.class);
    }

}

class SafeEnumTypeAdapter<T extends Enum<T>> extends TypeAdapter<T> {
    private final Class<T> clazz;

    SafeEnumTypeAdapter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        out.jsonValue(value.name());
    }

    @Override
    public T read(JsonReader in) throws IOException {
        String name = in.nextString();
        return name == null ? null : Stream.of(clazz.getEnumConstants())
                .filter(e -> name.equalsIgnoreCase(e.name()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("No enum constant " + clazz.getCanonicalName() + "." + name));
    }

}
