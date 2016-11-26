package com.eitraz.tellstick.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import static com.eitraz.tellstick.hazelcast.TellstickHazelcastCluster.RAW_DEVICE_EVENTS_TOPIC;
import static com.eitraz.tellstick.hazelcast.TellstickHazelcastClusterNodeGlobals.TELLSTICK;

@SpringBootApplication
public class TellstickHazelcastClusterNode implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger();
    public static final String IP_PROPERTY = "ip";

    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
    private TellstickBean tellstick;

    @SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
    private HazelcastInstance hazelcast;

    private ITopic<Object> rawDeviceEventsTopic;

    @Bean(name = "hazelcast")
    public HazelcastInstance hazelcast() {
        Config config = new Config();
        config.setProperty("hazelcast.local.localAddress", System.getProperty("ip"));

        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPublicAddress(System.getProperty("ip"));
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(false);

        return Hazelcast.newHazelcastInstance(config);
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
        setSystemIpProperty();
        SpringApplication.run(TellstickHazelcastClusterNode.class, args);
    }

    public static void setSystemIpProperty() {
        if (System.getProperty("ip") == null) {
            try {
                String address = InetAddress.getLocalHost().getHostAddress();
                System.setProperty("ip", address);
                logger.info(String.format("System property 'ip' set to %s", address));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info(String.format("System property 'ip' is set to %s", System.getProperty(IP_PROPERTY)));
        }
    }
}
