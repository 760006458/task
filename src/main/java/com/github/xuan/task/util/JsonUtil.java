package com.github.xuan.task.util;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    private static final ObjectMapper mapper = new ObjectMapper()
            // jackson 处理 date 类型时会把配置中的 dateFormat 对象 clone 后使用，SimpleDateFormat 实现了深拷贝，所以使用时是线程安全的。
            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
            .registerModule(
                    new JavaTimeModule()
                            .addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                            .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                            .addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                            .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .addMixIn(byte[].class, JacksonMixinIgnoreType.class)
            .addMixIn(InputStream.class, JacksonMixinIgnoreType.class)
//            .addHandler()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static String toJsonStr(Object object, boolean pretty) {
        try {
            if (pretty) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
            } else {
                return mapper.writeValueAsString(object);
            }
        } catch (Exception e) {
            LOGGER.error("Jackson toJsonString com.auraxy.kaipiao.base.exception.");
            return null;
        }
    }

    public static String toJsonStr(Object object) {
        if (object == null) {
            return "";
        }
        return toJsonStr(object, false);
    }

    public static <T> T parse(String json, Class<T> valueType) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Jackson parseObject com.auraxy.kaipiao.base.exception.");
        }
    }

    public static <T> T parse(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, new TypeReference<T>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Jackson parseObject com.auraxy.kaipiao.base.exception.");
        }
    }

    public static <T> T parse(String json, JavaType valueType) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return mapper.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Jackson parseObject com.auraxy.kaipiao.base.exception.");
        }
    }

    public static <T> List<T> parseList(String jsonStr, Class<T> valueType) {
        if (jsonStr == null) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(jsonStr, mapper.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (IOException e) {
            throw new RuntimeException("Jackson parseList com.auraxy.kaipiao.base.exception.");
        }
    }

    public static <T> List<T> objToList(Object obj, Class<T> valueType) {
        if (obj == null) {
            return new ArrayList<>();
        }
        try {
            return mapper.readValue(toJsonStr(obj), mapper.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (IOException e) {
            throw new RuntimeException("Jackson parseList com.auraxy.kaipiao.base.exception.");
        }
    }

    /**
     * 一个标记类，用于 Jackson mixin, 忽略指定 Java 类型
     *
     * @see ObjectMapper#addMixIn(Class, Class)
     */
    @JsonIgnoreType
    private class JacksonMixinIgnoreType {

    }

    public static String toJsonStrWithoutException(Object object, boolean pretty) {
        try {
            return toJsonStr(object, pretty);
        } catch (Exception e) {
            LOGGER.error("Jackson toJsonString com.auraxy.kaipiao.base.exception.");
            return null;
        }
    }

    public static HashMap toHashMap(Object object) {
        if (object instanceof String) {
            return JsonUtil.parse(object.toString(), HashMap.class);
        }
        return JsonUtil.parse(JsonUtil.toJsonStr(object), HashMap.class);
    }

    public static <T> T mapToObj(Map map, Class<T> c) {
        if (map == null || c == null) {
            throw new RuntimeException("json转换异常，数据不能为空");
        }
        return JsonUtil.parse(JsonUtil.toJsonStr(map), c);
    }

    public static <T> T objToObj(Object obj, Class<T> c) {
        if (obj == null || c == null) {
            throw new RuntimeException("json转换异常，数据不能为空");
        }
        return JsonUtil.parse(JsonUtil.toJsonStr(obj), c);
    }
}
