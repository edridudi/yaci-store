package com.bloxbean.cardano.yaci.store.account.storage;

import com.bloxbean.cardano.yaci.store.account.domain.AddressBalance;
import com.bloxbean.cardano.yaci.store.account.domain.StakeAddressBalance;

import java.util.List;
import java.util.Optional;

/**
 * Storage for address balance and stake address balance
 */
public interface AccountBalanceStorage {
    /**
     * Get address balance for the given address, unit at the given slot
     * @param address address
     * @param unit unit
     * @param slot slot
     * @return AddressBalance
     */
    Optional<AddressBalance> getAddressBalance(String address, String unit, long slot);

    /**
     * Get latest address balance for the given address at highest slot
     * @param address address
     * @return List of AddressBalance
     */
    List<AddressBalance> getAddressBalance(String address);

    /**
     * Save address balances
     * @param addressBalances
     */
    void saveAddressBalances(List<AddressBalance> addressBalances);

    /**
     * Delete all address balances before the slot except the top one
     * This is called to clean history data
     * @param address address
     * @param unit unit
     * @param slot slot
     * @return number of records deleted
     */
    int deleteAddressBalanceBeforeSlotExceptTop(String address, String unit, long slot);

    /**
     * Delete all address balances after the slot
     * This is used for rollback event
     * @param slot slot
     * @return number of records deleted
     */
    int deleteAddressBalanceBySlotGreaterThan(Long slot);

    /**
     * Get stake address balance for the given address, unit at the given slot
     * @param address address
     * @param unit unit
     * @param slot slot
     * @return StakeAddressBalance
     */
    Optional<StakeAddressBalance> getAddressStakeBalance(String address, String unit, long slot);

    /**
     * Get latest stake address balance for the given address at highest slot
     * @param address address
     * @return List of StakeAddressBalance
     */
    List<StakeAddressBalance> getStakeAddressBalance(String address);

    /**
     * Save stake address balances
     * @param stakeBalances
     */
    void saveStakeAddressBalances(List<StakeAddressBalance> stakeBalances);

    /**
     * Delete all stake address balances before the slot except the top one
     * This is called to clean history data
     * @param address address
     * @param unit unit
     * @param slot slot
     * @return number of records deleted
     */
    int deleteStakeBalanceBeforeSlotExceptTop(String address, String unit, long slot);

    /**
     * Delete all stake address balances after the slot
     * This is used for rollback event
     * @param slot  slot
     * @return number of records deleted
     */
    int deleteStakeAddressBalanceBySlotGreaterThan(Long slot);
}