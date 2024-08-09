package com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.repository;

import com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.model.RedisEraEntity;
import com.redis.om.spring.repository.RedisDocumentRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RedisEraRepository extends RedisDocumentRepository<RedisEraEntity, Integer> {

    Optional<RedisEraEntity> findFirstByEraGreaterThanOrderByEraAsc(int era);
}
