package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.Tellstick;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import static com.eitraz.tellstick.hazelcast.TellstickHazelcastClusterNodeGlobals.TELLSTICK;

public class TellstickHazelcastClusterNode {
    private final HazelcastInstance hazelcast;

    public TellstickHazelcastClusterNode(Tellstick tellstick, HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
        TellstickHazelcastClusterNodeGlobals.set(TELLSTICK, tellstick);

        ITopic<Object> rawDeviceEventsTopic = hazelcast.getTopic(TellstickHazelcastCluster.RAW_DEVICE_EVENTS_TOPIC);

        tellstick.getRawDeviceHandler()
                .addRawDeviceEventListener(event -> rawDeviceEventsTopic.publish(event.getParameters()));
    }
}
