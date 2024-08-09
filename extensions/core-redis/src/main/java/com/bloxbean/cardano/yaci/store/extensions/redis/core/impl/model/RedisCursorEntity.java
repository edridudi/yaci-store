package com.bloxbean.cardano.yaci.store.extensions.redis.core.impl.model;

import com.bloxbean.cardano.yaci.store.common.model.BaseEntity;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;
import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@Builder
@Document("cursor")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class RedisCursorEntity extends BaseEntity {

    @Id
    private String id;
    @Indexed
    private Long eventPublisherId;
    @Indexed
    private String blockHash;
    @Indexed
    private Long slot;
    @Indexed
    private Long block;
    private String prevBlockHash;
    private Integer era;
}
