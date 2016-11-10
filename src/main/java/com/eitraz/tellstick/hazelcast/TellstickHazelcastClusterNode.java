package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.Tellstick;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;

import static com.eitraz.tellstick.hazelcast.TellstickHazelcastClusterNodeGlobals.TELLSTICK;

public class TellstickHazelcastClusterNode {
    public TellstickHazelcastClusterNode() {
        this(new Tellstick(), Hazelcast.newHazelcastInstance());
    }

    public TellstickHazelcastClusterNode(Tellstick tellstick, HazelcastInstance hazelcast) {
        tellstick.start();

        // Stop on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                tellstick.stop();
            }
        });

        // Make Tellstick available from executor tasks
        TellstickHazelcastClusterNodeGlobals.set(TELLSTICK, tellstick);

        // Publish raw device events
        ITopic<Object> rawDeviceEventsTopic = hazelcast.getTopic(TellstickHazelcastCluster.RAW_DEVICE_EVENTS_TOPIC);
        tellstick.getRawDeviceHandler()
                .addRawDeviceEventListener(event -> rawDeviceEventsTopic.publish(event.getParameters()));
    }

    public static void main(String[] args) {
        new TellstickHazelcastClusterNode();
    }
}
