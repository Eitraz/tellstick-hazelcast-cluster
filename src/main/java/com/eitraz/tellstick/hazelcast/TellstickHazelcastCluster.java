package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.rawdevice.RawDeviceEventListener;
import com.eitraz.tellstick.core.rawdevice.events.RawDeviceEvent;
import com.hazelcast.core.Hazelcast;
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

    private static final String DEVICE_COMMAND_EXECUTOR_SERVICE = "tellstick.device.command";
    static final String RAW_DEVICE_EVENTS_TOPIC = "tellstick.rawDevice.events";

    private final List<RawDeviceEventListener> rawDeviceEventListeners = new CopyOnWriteArrayList<>();
    private final IExecutorService deviceCommandExecutorService;

    @SuppressWarnings("FieldCanBeLocal")
    private final Thread deviceCommandQueueExecuteThread;
    private final BlockingQueue<TellstickHazelcastClusterDeviceCommand> deviceCommandQueue = new LinkedBlockingDeque<>();
    private final Map<String, Integer> lastDeviceCommands = new ConcurrentHashMap<>();

    public TellstickHazelcastCluster() {
        this(Hazelcast.newHazelcastInstance());
    }

    public TellstickHazelcastCluster(HazelcastInstance hazelcast) {
        deviceCommandExecutorService = hazelcast.getExecutorService(DEVICE_COMMAND_EXECUTOR_SERVICE);
        hazelcast.<Map<String, String>>getTopic(RAW_DEVICE_EVENTS_TOPIC)
                .addMessageListener(message -> fireRawDeviceEvent(new RawDeviceEvent(message.getMessageObject())));

        deviceCommandQueueExecuteThread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    // Execute next available queued command
                    internalExecuteDeviceCommand(deviceCommandQueue.take());

                    // Wait a bit
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting to execute command", e);
                }
            }
        });
        deviceCommandQueueExecuteThread.start();
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

    synchronized void executeDeviceCommand(String deviceName, Integer command) {
        if (command.equals(lastDeviceCommands.get(deviceName)))
            return;

        lastDeviceCommands.put(deviceName, command);

        // Remove any currently queued commands for device
        deviceCommandQueue.removeIf(c -> c.getDeviceName().equals(deviceName));

        // Execute command
        internalExecuteDeviceCommand(new TellstickHazelcastClusterDeviceCommand(deviceName, command));
    }

    private synchronized void internalExecuteDeviceCommand(TellstickHazelcastClusterDeviceCommand command) {
        Map<Member, Future<Boolean>> futures = deviceCommandExecutorService
                .submitToAllMembers(command);

        command.increaseCallCounter();

        // Queue to run command again
        if (command.getCallCounter() <= 3) {
            deviceCommandQueue.offer(command);
        }

        long executedOnNodes = futures
                .values().stream()
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

        logger.info("Command {} on device {} successfully executed on {} nodes",
                command.getCommand(), command.getDeviceName(), executedOnNodes);
    }
}
