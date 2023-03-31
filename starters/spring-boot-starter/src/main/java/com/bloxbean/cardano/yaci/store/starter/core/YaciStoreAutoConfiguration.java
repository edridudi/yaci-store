package com.bloxbean.cardano.yaci.store.starter.core;

import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.helper.*;
import com.bloxbean.cardano.yaci.store.core.service.ApplicationStartListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@EnableConfigurationProperties(YaciStoreProperties.class)
@ComponentScan(basePackages = {"com.bloxbean.cardano.yaci.store.core", "com.bloxbean.cardano.yaci.store.common", "com.bloxbean.cardano.yaci.store.events"})
@EnableJpaRepositories( basePackages = {"com.bloxbean.cardano.yaci.store.core", "com.bloxbean.cardano.yaci.store.common", "com.bloxbean.cardano.yaci.store.events"})
@EntityScan(basePackages = {"com.bloxbean.cardano.yaci.store.core", "com.bloxbean.cardano.yaci.store.common", "com.bloxbean.cardano.yaci.store.events"})
@EnableTransactionManagement
@EnableScheduling
@Slf4j
public class YaciStoreAutoConfiguration {
    @Autowired
    YaciStoreProperties properties;

    //configuration

    @Bean
    public TipFinder tipFinder() {
        TipFinder tipFinder = new TipFinder(properties.getCardano().getHost(), properties.getCardano().getPort(),
                Point.ORIGIN, properties.getCardano().getProtocolMagic());
        tipFinder.start();
        return tipFinder;
    }

    @Bean
    @Scope("prototype")
    public BlockRangeSync blockRangeSync() {
        log.info("Creating BlockRangeSync to fetch blocks");
        BlockRangeSync blockRangeSync = new BlockRangeSync(properties.getCardano().getHost(), properties.getCardano().getPort(), properties.getCardano().getProtocolMagic());
        return blockRangeSync;
    }

    @Bean
    public BlockSync blockSync() {
        BlockSync blockSync = new BlockSync(properties.getCardano().getHost(), properties.getCardano().getPort(), properties.getCardano().getProtocolMagic(), Point.ORIGIN);
        return blockSync;
    }

    @Bean
    public GenesisBlockFinder genesisBlockFinder() {
        GenesisBlockFinder genesisBlockFinder = new GenesisBlockFinder(properties.getCardano().getHost(), properties.getCardano().getPort(), properties.getCardano().getProtocolMagic());
        return genesisBlockFinder;
    }

    @Bean(name = "localClientProvider")
    @ConditionalOnProperty(prefix = "store.cardano", name = "n2c-node-socket-path")
    @Primary
    public LocalClientProvider localClientProviderNodeSocketPath() {
        log.info("LocalStateQueryClient ---> Configured --> " + properties.getCardano().getN2cNodeSocketPath());
        return new LocalClientProvider(properties.getCardano().getN2cNodeSocketPath(), properties.getCardano().getProtocolMagic());
    }

    @Bean(name = "localClientProvider")
    @ConditionalOnProperty(prefix = "store.cardano", name = "n2c-host")
    public LocalClientProvider localClientProviderNodeSocketPort() {
        if (properties.getCardano().getN2cPort() == 0)
            throw new IllegalArgumentException("Invalid cardano.n2c.port " + properties.getCardano().getN2cPort() );
        log.info("LocalStateQueryClient ---> Configured (n2c host/port)--> " + properties.getCardano().getN2cHost() + ", " + properties.getCardano().getN2cPort());
        return new LocalClientProvider(properties.getCardano().getN2cHost(), properties.getCardano().getN2cPort());

    }

    @Bean
    @ConditionalOnBean(name = {"localClientProvider"})
    public ApplicationStartListener applicationStartListener(LocalClientProvider localClientProvider) {
        log.info("ApplicationStartListener with LocalClientProvider created >>");
        return new ApplicationStartListener(localClientProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}