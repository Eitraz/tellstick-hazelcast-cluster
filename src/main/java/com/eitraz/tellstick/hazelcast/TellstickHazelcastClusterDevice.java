package com.eitraz.tellstick.hazelcast;

public class TellstickHazelcastClusterDevice {
    private final TellstickHazelcastCluster cluster;
    private final String name;

    public TellstickHazelcastClusterDevice(TellstickHazelcastCluster cluster, String name) {
        this.cluster = cluster;
        this.name = name;
    }

    public void on() {
        cluster.executeDeviceCommand(name, TellstickHazelcastClusterDeviceCommand.ON);
    }

    public void off() {
        cluster.executeDeviceCommand(name, TellstickHazelcastClusterDeviceCommand.OFF);
    }
}
