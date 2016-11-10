package com.eitraz.tellstick.hazelcast;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TellstickHazelcastClusterNodeGlobals {
    public final static String TELLSTICK = "tellstick";

    private static final Map<String, Object> values = new ConcurrentHashMap<>();

    public static void set(String key, Object value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> get(String key) {
        return Optional.ofNullable((T) values.get(key));
    }
}
