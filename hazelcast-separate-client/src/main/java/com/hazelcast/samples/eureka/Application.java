package com.hazelcast.samples.eureka;

import com.hazelcast.config.Config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@EnableDiscoveryClient
@SpringBootApplication
public class Application {

    @Value("${hazelcast.port:5701}")
    private int hazelcastPort;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.getCPSubsystemConfig().setCPMemberCount(4);
        config.getCPSubsystemConfig().setGroupSize(3);
        config.getNetworkConfig().setPort(hazelcastPort);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getEurekaConfig()
              .setEnabled(true)
              .setProperty("self-registration", "true")
              .setProperty("namespace", "hazelcast");
        config.getNetworkConfig().getInterfaces().setEnabled(true);
        config.getNetworkConfig().getInterfaces().setInterfaces(Arrays.asList("10.0.*.*"));
        return config;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
