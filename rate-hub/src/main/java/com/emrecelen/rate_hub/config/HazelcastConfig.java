package com.emrecelen.rate_hub.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    public static final String MAP_RATES = "rates";
    public static final String MAP_LAST_SEQ = "rate_seq";
    public static final String TOPIC_RATE_UPDATES = "rate-updates";
    public static final String MAP_FRESHNESS = "rate_freshness";


    @Bean
    public HazelcastInstance hazelcastInstance(
            @Value("${hazelcast.cluster-name}") String clusterName,
            @Value("${hazelcast.backup-count}") int backupCount,
            @Value("${hazelcast.members}") String members
    ) {
        Config config = new Config();
        config.setClusterName(clusterName);

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getMulticastConfig()
                .setEnabled(true)
                .setMulticastGroup("224.2.2.3")
                .setMulticastPort(54327);

        join.getAutoDetectionConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(false);


        config.getMapConfig(MAP_RATES)
                .setBackupCount(backupCount);
        config.getMapConfig(MAP_LAST_SEQ)
                .setBackupCount(backupCount);
        config.getMapConfig(MAP_FRESHNESS)
                .setBackupCount(backupCount);

        return Hazelcast.newHazelcastInstance(config);
    }
}

