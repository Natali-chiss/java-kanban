package com.yandex.tasktracker.service.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(duration.toString());
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String durationString = jsonReader.nextString();
        try {
            return Duration.parse(durationString);
        } catch (Exception e) {
            System.err.println("Ошибка при десериализации Duration: " + e.getMessage());
            throw new IOException("Ошибка при десериализации Duration", e);
        }
    }
}
