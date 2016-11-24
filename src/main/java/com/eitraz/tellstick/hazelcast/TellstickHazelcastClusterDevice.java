package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.TellstickCoreLibrary;

public class TellstickHazelcastClusterDevice {
    private final TellstickHazelcastCluster cluster;
    private final String name;

    public TellstickHazelcastClusterDevice(TellstickHazelcastCluster cluster, String name) {
        this.cluster = cluster;
        this.name = name;
    }

    public void setOn(boolean on) {
        if (on)
            on();
        else
            off();
    }

    public void on() {
        cluster.executeDeviceCommand(name, TellstickCoreLibrary.TELLSTICK_TURNON);
    }

    public void off() {
        cluster.executeDeviceCommand(name, TellstickCoreLibrary.TELLSTICK_TURNOFF);
    }
}
