package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.Tellstick;
import com.eitraz.tellstick.core.device.Device;
import com.eitraz.tellstick.core.device.DeviceException;
import com.eitraz.tellstick.core.device.OnOffDevice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.concurrent.Callable;

import static com.eitraz.tellstick.hazelcast.TellstickHazelcastClusterNodeGlobals.TELLSTICK;

public class TellstickHazelcastClusterDeviceCommand implements Callable<Boolean>, Serializable {
    private static final Logger logger = LogManager.getLogger();

    public static final String ON = "on";
    public static final String OFF = "off";

    private final String deviceName;
    private final String command;

    public TellstickHazelcastClusterDeviceCommand(String deviceName, String command) {
        this.deviceName = deviceName;
        this.command = command;
    }

    @Override
    public Boolean call() throws Exception {
        return TellstickHazelcastClusterNodeGlobals.<Tellstick>get(TELLSTICK)
                .map(tellstick -> tellstick.getDeviceHandler()
                        .getDeviceByName(deviceName)
                        .map(this::runCommand)
                        .orElse(false))
                .orElse(false);
    }

    private boolean runCommand(Device device) {
        try {
            // On
            if (ON.equals(command) && device instanceof OnOffDevice) {
                ((OnOffDevice) device).on();
                return true;
            }
            // Off
            else if (OFF.equals(command) && device instanceof OnOffDevice) {
                ((OnOffDevice) device).off();
                return true;
            }
        } catch (DeviceException e) {
            logger.error("Failed to run device command '{}', error: {}", command, e.getMessage());
        }
        return false;
    }
}
