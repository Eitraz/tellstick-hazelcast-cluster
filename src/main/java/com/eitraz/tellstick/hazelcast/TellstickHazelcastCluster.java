package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class TellstickHazelcastCluster {
    private static final Logger logger = LogManager.getLogger();

    public static final String DEVICE_COMMAND_EXECUTOR_SERVICE = "tellstick.device.command";
    public static final String RAW_DEVICE_EVENTS_TOPIC = "tellstick.rawDevice.events";

    private final HazelcastInstance hazelcast;
    private final List<RawDeviceEventListener> rawDeviceEventListeners = new CopyOnWriteArrayList<>();
    private final IExecutorService deviceCommandExecutorService;

    public TellstickHazelcastCluster(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
        deviceCommandExecutorService = hazelcast.getExecutorService(DEVICE_COMMAND_EXECUTOR_SERVICE);
        hazelcast.<Map<String, String>>getTopic(RAW_DEVICE_EVENTS_TOPIC)
                .addMessageListener(message -> fireRawDeviceEvent(new RawDeviceEvent(message.getMessageObject())));
    }

    public void addRawDeviceEventListener(RawDeviceEventListener listener) {
        if (!rawDeviceEventListeners.contains(listener))
            rawDeviceEventListeners.add(listener);
    }

    public boolean removeRawDeviceEventListener(RawDeviceEventListener listener) {
        return rawDeviceEventListeners.remove(listener);
    }

    private void fireRawDeviceEvent(RawDeviceEvent event) {
        rawDeviceEventListeners.forEach(listener -> listener.rawDeviceEvent(event));
    }

    public TellstickHazelcastClusterDevice getDevice(String deviceName) {
        return new TellstickHazelcastClusterDevice(this, deviceName);
    }

    synchronized void executeDeviceCommand(String deviceName, String command) {
        Map<Member, Future<Boolean>> futures = deviceCommandExecutorService
                .submitToAllMembers(new TellstickHazelcastClusterDeviceCommand(deviceName, command));

        long executedOnNodes = futures.values().stream()
                .map(f -> {
                    try {
                        return f.get(5000, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        logger.error("Error when waiting for device command", e);
                    }
                    return false;
                })
                .filter(Boolean::booleanValue)
                .count();
        logger.info("Command {} on device {} successfully executed on {} nodes", command, deviceName, executedOnNodes);
    }
}
