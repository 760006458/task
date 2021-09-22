package com.github.xuan.task.util;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 参数校验. 参考guava的Preconditions
 */
public class Validates {

    /**
     * 校验入参是非负数
     */
    public static void checkNonnegative(long requestParam) {
        if (requestParam < 0) {
            throw new InvalidParameterException();
        }
    }

    /**
     * 校验入参是非负数
     */
    public static void checkNonnegative(long requestParam, String message) {
        if (requestParam < 0) {
            throw new InvalidParameterException(message);
        }
    }

    /**
     * 校验入参是正整数
     */
    public static void checkPositive(long requestParam) {
        if (requestParam <= 0) {
            throw new InvalidParameterException();
        }
    }

    /**
     * 校验入参是正整数
     */
    public static void checkPositive(long requestParam, String message) {
        if (requestParam <= 0) {
            throw new InvalidParameterException(message);
        }
    }

    public static void checkNotBlank(String requestParam) {
        if (StringUtil.isBlank(requestParam)) {
            throw new InvalidParameterException();
        }
    }

    public static void checkNotBlank(String requestParam, String message) {
        if (StringUtil.isBlank(requestParam)) {
            throw new InvalidParameterException(message);
        }
    }

    public static void check(boolean expression) {
        if (!expression) {
            throw new InvalidParameterException();
        }
    }

    public static void check(boolean expression, String message) {
        if (!expression) {
            throw new InvalidParameterException(message);
        }
    }

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new InvalidParameterException();
        }
    }

    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new InvalidParameterException(message);
        }
    }

    public static void checkState(boolean expression) {
        if (!expression) {
            throw new InvalidParameterException();
        }
    }

    public static void checkState(boolean expression, String message) {
        if (!expression) {
            throw new InvalidParameterException(message);
        }
    }

    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new InvalidParameterException();
        }
        return reference;
    }

    public static <T> T checkNotNull(T reference, String message) {
        if (reference == null) {
            throw new InvalidParameterException(message);
        }
        return reference;
    }

    public static String checkNotNullOrEmpty(String reference) {
        if (StringUtil.isBlank(reference)) {
            throw new InvalidParameterException();
        }
        return reference;
    }

    public static <T> Collection<T> checkNotNullOrEmpty(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            throw new InvalidParameterException();
        }
        return collection;
    }

    public static <K, V> Map<K, V> checkNotNullOrEmpty(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            throw new InvalidParameterException();
        }
        return map;
    }

    public static <K, V> Map<K, V> checkNotNullOrEmpty(Map<K, V> map, String message) {
        if (map == null || map.isEmpty()) {
            throw new InvalidParameterException(message);
        }
        return map;
    }

    public static <T> Collection<T> checkNotNullOrEmpty(Collection<T> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new InvalidParameterException(message);
        }
        return collection;
    }

    public static String checkNotNullOrEmpty(String reference, String message) {
        if (StringUtil.isBlank(reference)) {
            throw new InvalidParameterException(message);
        }
        return reference;
    }

    public static <T> void checkEqual(T obj1, T obj2) {
        if (!Objects.equals(obj1, obj2)) {
            throw new InvalidParameterException();
        }
    }

    public static <T> void checkEqual(T obj1, T obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throw new InvalidParameterException(message);
        }
    }

    public static void checkRequestParams(boolean expression, String message) {
        if (!expression) {
            throw new InvalidParameterException(message);
        }
    }
}
