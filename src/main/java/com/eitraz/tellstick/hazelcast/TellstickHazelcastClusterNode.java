package com.eitraz.tellstick.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static com.eitraz.tellstick.hazelcast.TellstickHazelcastCluster.RAW_DEVICE_EVENTS_TOPIC;
import static com.eitraz.tellstick.hazelcast.TellstickHazelcastClusterNodeGlobals.TELLSTICK;

@SpringBootApplication
public class TellstickHazelcastClusterNode implements CommandLineRunner {
    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
    private TellstickBean tellstick;

    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
    private HazelcastInstance hazelcast;

    private ITopic<Object> rawDeviceEventsTopic;

    @Bean(name = "hazelcast")
    public HazelcastInstance hazelcast() {
        return Hazelcast.newHazelcastInstance();
    }

    @Bean(name = "tellstick")
    public TellstickBean tellstick() {
        TellstickBean tellstick = new TellstickBean();
        TellstickHazelcastClusterNodeGlobals.set(TELLSTICK, tellstick);
        return tellstick;
    }

    @Override
    public void run(String... strings) throws Exception {
        // Publish raw device events
        rawDeviceEventsTopic = hazelcast.getTopic(RAW_DEVICE_EVENTS_TOPIC);
        tellstick.getRawDeviceHandler()
                 .addRawDeviceEventListener(event -> rawDeviceEventsTopic.publish(event.getParameters()));
    }

    public static void main(String[] args) {
        SpringApplication.run(TellstickHazelcastClusterNode.class, args);
    }
}
