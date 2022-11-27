package com.bloxbean.cardano.yaci.indexer.protocolparams.entity;

import com.bloxbean.carano.yaci.indexer.common.entity.BaseEntity;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "protocol_params")
public class ProtocolParamsEntity extends BaseEntity {
    @Id
    private long id;

    @Type(type = "json")
    @Column(name = "params")
    private ProtocolParams protocolParams;
}