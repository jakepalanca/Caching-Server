package jakepalanca.caching.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.javalin.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.stream.Stream;

public class JacksonJsonMapper implements JsonMapper {
    private final ObjectMapper objectMapper;

    public JacksonJsonMapper() {
        objectMapper = new ObjectMapper();

        // Configure the ObjectMapper
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public <T> T fromJsonStream(InputStream json, Type targetType) {
        try {
            return objectMapper.readValue(json, objectMapper.constructType(targetType));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON from InputStream", e);
        }
    }

    @Override
    public <T> T fromJsonString(String json, Type targetType) {
        try {
            return objectMapper.readValue(json, objectMapper.constructType(targetType));
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON from String", e);
        }
    }

    @Override
    public InputStream toJsonStream(Object obj, Type type) {
        try {
            byte[] bytes = objectMapper.writerFor(objectMapper.constructType(type)).writeValueAsBytes(obj);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON InputStream", e);
        }
    }

    @Override
    public String toJsonString(Object obj, Type type) {
        try {
            return objectMapper.writerFor(objectMapper.constructType(type)).writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON String", e);
        }
    }

    @Override
    public void writeToOutputStream(Stream<?> stream, OutputStream outputStream) {
        try {
            objectMapper.writeValue(outputStream, stream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to write Stream to OutputStream", e);
        }
    }
}
