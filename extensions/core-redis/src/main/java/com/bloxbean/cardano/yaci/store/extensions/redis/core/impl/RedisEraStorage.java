package com.bloxbean.cardano.yaci.store.extensions.redis.core.impl;

import com.bloxbean.cardano.yaci.store.core.domain.CardanoEra;
import com.bloxbean.cardano.yaci.store.core.storage.api.EraStorage;
import com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.mapper.RedisEraMapper;
import com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.repository.RedisEraRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class RedisEraStorage implements EraStorage {

    private final RedisEraRepository eraRepository;
    private final RedisEraMapper eraMapper;

    @Override
    public void saveEra(CardanoEra era) {
        eraRepository.findById(era.getEra().getValue())
                .ifPresentOrElse(eraEntity -> {
                    //TODO -- Do nothing
                }, () -> eraRepository.save(eraMapper.toEraEntity(era)));
    }

    @Override
    public Optional<CardanoEra> findEra(int era) {
        return eraRepository.findById(era)
                .map(eraMapper::toEra);
    }

    @Override
    public Optional<CardanoEra> findFirstNonByronEra() {
        return eraRepository.findFirstByEraGreaterThanOrderByEraAsc(1)
                .map(eraMapper::toEra);
    }
}
