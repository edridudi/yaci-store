CREATE INDEX idx_transaction_block
    ON transaction(block);

CREATE INDEX idx_transaction_block_hash
    ON transaction(block_hash);

-- transaction_witness

CREATE INDEX idx_transaction_witness_tx_hash
    ON transaction_witness(tx_hash);
