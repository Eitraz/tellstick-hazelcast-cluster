package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.device.Device;
import com.eitraz.tellstick.core.device.DeviceException;
import com.eitraz.tellstick.core.device.OnOffDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static com.eitraz.tellstick.core.TellstickCoreLibrary.TELLSTICK_TURNOFF;
import static com.eitraz.tellstick.core.TellstickCoreLibrary.TELLSTICK_TURNON;
import static com.eitraz.tellstick.hazelcast.TellstickHazelcastClusterNodeGlobals.TELLSTICK;

public class TellstickHazelcastClusterDeviceCommand implements Callable<Boolean>, Serializable {
    private static final Logger logger = LogManager.getLogger();

    private final String deviceName;
    private final int command;
    private int callCounter = 0;

    public TellstickHazelcastClusterDeviceCommand(String deviceName, int command) {
        this.deviceName = deviceName;
        this.command = command;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public int getCommand() {
        return command;
    }

    public void increaseCallCounter() {
        callCounter++;
    }

    public int getCallCounter() {
        return callCounter;
    }

    @Override
    public Boolean call() throws Exception {
        return TellstickHazelcastClusterNodeGlobals.<TellstickBean>get(TELLSTICK)
                .map(tellstick -> tellstick
                        .getDeviceHandler()
                        .getDeviceByName(deviceName)
                        .map(this::runCommand)
                        .orElse(false))
                .orElse(false);
    }

    private boolean runCommand(Device device) {
        logger.info("RUN COMMAND");
        try {
            // On
            if (command == TELLSTICK_TURNON && device instanceof OnOffDevice) {
                ((OnOffDevice) device).on();
                return true;
            }
            // Off
            else if (command == TELLSTICK_TURNOFF && device instanceof OnOffDevice) {
                ((OnOffDevice) device).off();
                return true;
            }
        } catch (DeviceException e) {
            logger.error("Failed to run device command '{}', error: {}", command, e.getMessage());
        }
        return false;
    }
}
