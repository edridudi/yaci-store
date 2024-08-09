package com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.model;

import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Document("era")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisEraEntity {

    @Id
    @Indexed
    private int era;
    private long startSlot;
    private long block;
    private String blockHash;
}
