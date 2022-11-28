package com.bloxbean.cardano.yaci.indexer.utxo;

import com.bloxbean.cardano.yaci.core.model.*;
import com.bloxbean.cardano.yaci.helper.model.Transaction;
import com.bloxbean.cardano.yaci.helper.model.Utxo;
import com.bloxbean.cardano.yaci.indexer.events.EventMetadata;
import com.bloxbean.cardano.yaci.indexer.events.TransactionEvent;
import com.bloxbean.cardano.yaci.indexer.utxo.entity.AddressUtxo;
import com.bloxbean.cardano.yaci.indexer.utxo.processor.UtxoProcessor;
import com.bloxbean.cardano.yaci.indexer.utxo.repository.InvalidTransactionRepository;
import com.bloxbean.cardano.yaci.indexer.utxo.repository.UtxoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.bloxbean.cardano.yaci.core.util.Constants.LOVELACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class UtxoProcessorTest {

    @Mock
    private UtxoRepository utxoRepository;

    @Mock
    private InvalidTransactionRepository invalidTransactionRepository;

    @InjectMocks
    private UtxoProcessor utxoProcessor;

    @Captor
    ArgumentCaptor<List<AddressUtxo>> argCaptor;


    @BeforeEach
    public void setup() {
        openMocks(this);
        utxoProcessor = new UtxoProcessor(utxoRepository, invalidTransactionRepository);
    }

    @Test
    public void givenTransactionEvent_whenInputsNotResolved_createBothSpentAndUnspentOutputs() {
        List<Transaction> transactions = transactions();
        TransactionEvent transactionEvent = TransactionEvent.builder()
                .metadata(EventMetadata.builder()
                        .block(100)
                        .blockHash("c2f69d97f3a11684f2a97f7c75bc50dcb18c4a01a17625d32561a7ebc219aa5e")
                        .slot(200000)
                        .era(Era.Shelley)
                        .isSyncMode(true)
                        .build())
                .transactions(transactions)
                .build();

        utxoProcessor.handleTransactionEvent(transactionEvent);
        verify(utxoRepository, times(2)).saveAll(argCaptor.capture());

        List<AddressUtxo> spentUtxos = argCaptor.getAllValues().get(0);
        List<AddressUtxo> unspentUtxos = argCaptor.getAllValues().get(1);

        assertThat(spentUtxos).hasSize(3);
        assertThat(unspentUtxos).hasSize(2);

        assertThat(spentUtxos.stream().map(addressUtxo -> addressUtxo.getTxHash()))
                .contains("aaaae529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8",
                        "bbbbe529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8",
                        "cccce529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");

        assertThat(spentUtxos.stream().map(addressUtxo -> addressUtxo.getOutputIndex()))
                .contains(1, 2, 0);
        assertThat(spentUtxos.stream().map(addressUtxo -> addressUtxo.isSpent()))
                .contains(true, true, true);
        assertThat(spentUtxos.get(0).getSpentTxHash()).isEqualTo("f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(spentUtxos.get(1).getSpentTxHash()).isEqualTo("f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(spentUtxos.get(2).getSpentTxHash()).isEqualTo("f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(spentUtxos.stream().filter(addressUtxo -> addressUtxo.getAmounts() != null).collect(Collectors.toList())).hasSize(0);

        assertThat(unspentUtxos.get(0).getTxHash()).isEqualTo("f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(unspentUtxos.get(1).getTxHash()).isEqualTo("f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(unspentUtxos.get(0).getSpentTxHash()).isNull();
        assertThat(unspentUtxos.get(1).getSpentTxHash()).isNull();
        assertThat(unspentUtxos.get(0).isSpent()).isFalse();
        assertThat(unspentUtxos.get(1).isSpent()).isFalse();
        assertThat(unspentUtxos.get(0).getAmounts().get(0).getQuantity()).isEqualTo(transactions.get(0).getBody().getOutputs().get(0).getAmounts().get(0).getQuantity());
    }

    @Test
    public void givenTransactionEvent_whenInvalidTxn_createBothSpentAndUnspentOutputsFromCollateral() {
        List<Transaction> transactions = transactions();
        transactions.get(0).setInvalid(true);
        transactions.get(0).setCollateralReturnUtxo(Optional.of(collateralReturnUtxo(transactions.get(0).getTxHash(),
                transactions.get(0).getBody().getCollateralReturn()))); //Set collateral return utxos
        TransactionEvent transactionEvent = TransactionEvent.builder()
                .metadata(EventMetadata.builder()
                        .block(100)
                        .blockHash("c2f69d97f3a11684f2a97f7c75bc50dcb18c4a01a17625d32561a7ebc219aa5e")
                        .slot(200000)
                        .era(Era.Shelley)
                        .isSyncMode(true)
                        .build())
                .transactions(transactions)
                .build();

        utxoProcessor.handleTransactionEvent(transactionEvent);
        verify(utxoRepository, times(2)).saveAll(argCaptor.capture());

        List<AddressUtxo> spentUtxos = argCaptor.getAllValues().get(0);
        List<AddressUtxo> unspentUtxos = argCaptor.getAllValues().get(1);

        assertThat(spentUtxos).hasSize(2);
        assertThat(spentUtxos.get(0).getTxHash()).isEqualTo("dddd529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(spentUtxos.get(0).getOutputIndex()).isEqualTo(4);
        assertThat(spentUtxos.get(0).isSpent()).isTrue();
        assertThat(spentUtxos.get(1).getTxHash()).isEqualTo("eeee529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(spentUtxos.get(1).getOutputIndex()).isEqualTo(0);
        assertThat(spentUtxos.get(1).isSpent()).isTrue();

        assertThat(unspentUtxos.get(0).getTxHash()).isEqualTo("f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8");
        assertThat(unspentUtxos.get(0).getSpentTxHash()).isNull();
        assertThat(unspentUtxos.get(0).isSpent()).isFalse();
        assertThat(unspentUtxos.get(0).getAmounts().get(0).getQuantity()).isEqualTo(transactions.get(0).getCollateralReturnUtxo().get().getAmounts().get(0).getQuantity());
    }

    private List<Transaction> transactions() {
        String txHash = "f0a6e529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8";
        Transaction transaction = Transaction.builder()
                .txHash(txHash)
                .body(TransactionBody.builder()
                        .inputs(Set.of(
                                new TransactionInput("aaaae529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8", 1),
                                new TransactionInput("bbbbe529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8", 2),
                                new TransactionInput("cccce529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8", 0)
                        ))
                        .outputs(transactionOutputs())
                        .collateralInputs(Set.of(
                                new TransactionInput("dddd529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8", 4),
                                new TransactionInput("eeee529be26c2326c447c39159e05bb904ff1f7900b6df3852dd539de0343e8", 0)
                        ))
                        .collateralReturn(TransactionOutput.builder()
                                .address("addr_test1vpfwv0ezc5g8a4mkku8hhy3y3vp92t7s3ul8g778g5yegsgalc6gc")
                                .amounts(List.of(Amount.builder()
                                        .unit(LOVELACE)
                                        .assetName(LOVELACE)
                                        .quantity(BigInteger.valueOf(500))
                                        .build()))
                                .build())
                        .build()
                )
                .utxos(utxos(txHash, transactionOutputs()))
                .build();

        return List.of(transaction);
    }

    private List<TransactionOutput> transactionOutputs() {
        TransactionOutput output1 = TransactionOutput.builder()
                .address("addr_test1vpfwv0ezc5g8a4mkku8hhy3y3vp92t7s3ul8g778g5yegsgalc6gc")
                .amounts(List.of(Amount.builder()
                        .unit(LOVELACE)
                        .assetName(LOVELACE)
                        .quantity(BigInteger.valueOf(20000))
                        .build()))
                .build();

        TransactionOutput output2 = TransactionOutput.builder()
                .address("addr_test1abcwv0ezc5g8a4mkku8hhy3y3vp92t7s3ul8g778g5yegsgalc6gc")
                .amounts(List.of(Amount.builder()
                        .unit(LOVELACE)
                        .assetName(LOVELACE)
                        .quantity(BigInteger.valueOf(20000))
                        .build()))
                .build();
        return List.of(output1, output2);
    }

    private List<Utxo> utxos(String txHash, List<TransactionOutput> txOutputs) {
        AtomicInteger index = new AtomicInteger();
        return txOutputs.stream()
                .map(txOut -> {
                    return Utxo.builder()
                            .address(txOut.getAddress())
                            .txHash(txHash)
                            .index(index.getAndIncrement())
                            .amounts(txOut.getAmounts())
                            .build();
                }).collect(Collectors.toList());
    }

    private Utxo collateralReturnUtxo(String txHash, TransactionOutput txOut) {
        return Utxo.builder()
                .address(txOut.getAddress())
                .txHash(txHash)
                .index(0)
                .amounts(txOut.getAmounts())
                .build();

    }
}
