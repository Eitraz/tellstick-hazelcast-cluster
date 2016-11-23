package com.eitraz.tellstick.hazelcast;

import com.eitraz.tellstick.core.Tellstick;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class TellstickBean extends Tellstick {
    @PostConstruct
    public void setup() {
        start();
    }

    @PreDestroy
    public void tearDown() {
        stop();
    }
}
