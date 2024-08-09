package com.bloxbean.cardano.yaci.store.extensions.redis.core.impl;

import com.bloxbean.cardano.yaci.store.core.domain.Cursor;
import com.bloxbean.cardano.yaci.store.core.storage.api.CursorStorage;
import com.bloxbean.cardano.yaci.store.core.storage.impl.EraMapper;
import com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.model.RedisCursorEntity;
import com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.repository.RedisCursorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RedisCursorStorage implements CursorStorage {

    private final RedisCursorRepository cursorRepository;

    @Override
    public void saveCursor(long eventPublisherId, Cursor cursor) {
        RedisCursorEntity cursorEntity = RedisCursorEntity
                .builder()
                .id(eventPublisherId + "_" + cursor.getBlockHash())
                .eventPublisherId(eventPublisherId)
                .slot(cursor.getSlot())
                .blockHash(cursor.getBlockHash())
                .block(cursor.getBlock())
                .prevBlockHash(cursor.getPrevBlockHash())
                .era(cursor.getEra() != null ? cursor.getEra().getValue() : null)
                .build();

        cursorRepository.save(cursorEntity);
    }

    @Override
    public Optional<Cursor> getCurrentCursor(long eventPublisherId) {
        return cursorRepository.findTopByEventPublisherIdOrderBySlotDesc(eventPublisherId)
                .map(RedisCursorStorage::cursorEntityToCursor);
    }

    @Override
    public Optional<Cursor> getPreviousCursor(long eventPublisherId, long slot) {
        return cursorRepository.findTopByEventPublisherIdAndSlotBeforeOrderBySlotDesc(eventPublisherId, slot)
                .map(RedisCursorStorage::cursorEntityToCursor);
    }

    @Override
    public Optional<Cursor> findByBlockHash(long eventPublisherId, String blockHash) {
        return cursorRepository.findByEventPublisherIdAndBlockHash(eventPublisherId, blockHash)
                .map(RedisCursorStorage::cursorEntityToCursor);
    }

    @Override
    public int deleteBySlotGreaterThan(long eventPublisherId, long slot) {
        Integer count = cursorRepository.deleteByEventPublisherIdAndSlotGreaterThan(eventPublisherId, slot);
        return count == null ? 0 : count;
    }

    @Override
    public int deleteCursorBefore(long eventPublisherId, long blockNumber) {
        Integer count = cursorRepository.deleteByEventPublisherIdAndBlockLessThan(eventPublisherId, blockNumber);
        return count == null ? 0 : count;
    }

    private static Cursor cursorEntityToCursor(RedisCursorEntity cursorEntity) {
        return Cursor.builder()
                .slot(cursorEntity.getSlot())
                .blockHash(cursorEntity.getBlockHash())
                .block(cursorEntity.getBlock())
                .prevBlockHash(cursorEntity.getPrevBlockHash())
                .era(cursorEntity.getEra() != null ? EraMapper.intToEra(cursorEntity.getEra()) : null)
                .build();
    }
}
