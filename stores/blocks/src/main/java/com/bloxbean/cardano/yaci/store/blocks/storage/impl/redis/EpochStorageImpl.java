package com.bloxbean.cardano.yaci.store.blocks.storage.impl.redis;

import com.bloxbean.cardano.yaci.store.blocks.domain.Epoch;
import com.bloxbean.cardano.yaci.store.blocks.domain.EpochsPage;
import com.bloxbean.cardano.yaci.store.blocks.storage.api.EpochStorage;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.redis.mapper.EpochMapper;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.redis.model.EpochEntity;
import com.bloxbean.cardano.yaci.store.blocks.storage.impl.redis.repository.EpochRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EpochStorageImpl implements EpochStorage {

    private final EpochRepository epochRepository;
    private final EpochMapper epochMapper;

    @Override
    public Optional<Epoch> findRecentEpoch() {
        return epochRepository.findTopByOrderByNumberDesc()
                .map(epochMapper::toEpoch);
    }

    @Override
    public void save(Epoch epoch) {
        EpochEntity updatedEpochEntity = epochRepository.findById(epoch.getNumber())
                .map(epochEntity -> {
                    epochMapper.updateEntity(epoch, epochEntity);
                    return epochEntity;
                }).orElse(epochMapper.toEpochEntity(epoch));

        epochRepository.save(updatedEpochEntity);
    }

    @Override
    public Optional<Epoch> findByNumber(int number) {
        return epochRepository.findById((long) number)
                .map(epochMapper::toEpoch);
    }

    @Override
    public EpochsPage findEpochs(int page, int count) {
        Pageable sortedByEpoch =
                PageRequest.of(page, count, Sort.by("number").descending());

        Page<EpochEntity> epochsEntityPage = epochRepository.findAll(sortedByEpoch);
        long total = epochsEntityPage.getTotalElements();
        int totalPage = epochsEntityPage.getTotalPages();

        List<Epoch> epochList = epochsEntityPage.stream()
                .map(epochMapper::toEpoch)
                .collect(Collectors.toList());

        return EpochsPage.builder()
                .total(total)
                .totalPages(totalPage)
                .epochs(epochList)
                .build();
    }
}