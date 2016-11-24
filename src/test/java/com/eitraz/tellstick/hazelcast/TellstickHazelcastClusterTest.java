package com.eitraz.tellstick.hazelcast;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public class TellstickHazelcastClusterTest {
    private static final Logger logger = LogManager.getLogger();

    @Test
    public void testOnOff() throws Exception {
        TellstickHazelcastCluster tellstick = new TellstickHazelcastCluster();

        TellstickHazelcastClusterDevice device = tellstick.getDevice("TestDevice");

        logger.info("Turning ON");
        device.on();
        logger.info("Turned ON");

        Thread.sleep(1000);

        logger.info("Turning OFF");
        device.off();
        logger.info("Turned OFF");

        Thread.sleep(10000);
    }
}