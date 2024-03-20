package com.bloxbean.cardano.yaci.store.script.storage.impl.model;

import com.bloxbean.cardano.yaci.store.common.model.BaseEntity;
import com.bloxbean.cardano.yaci.store.script.domain.ScriptType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "script")
@EqualsAndHashCode(callSuper = false)
public class ScriptEntity extends BaseEntity {

    @Id
    @Column(name = "script_hash")
    private String scriptHash;

    @Column(name = "script_type")
    @Enumerated(EnumType.STRING)
    private ScriptType scriptType;

    @Type(JsonType.class)
    @Column(name = "content")
    private String content;
}