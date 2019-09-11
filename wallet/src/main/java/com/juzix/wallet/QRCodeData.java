package com.juzix.wallet;

import org.web3j.platon.PlatOnFunction;

import java.math.BigInteger;

public class QRCodeData {

    /**
     * nonce
     */
    private BigInteger nonce;
    /**
     * gasPrice
     */
    private BigInteger gasPrice;
    /**
     * gasLimit
     */
    private BigInteger gasLimit;
    /**
     * 链id
     */
    private long chainId;
    /**
     * txtType为0时指接收地址
     * txtType为1004指合约地址
     * txtType为1005值合约地址
     */
    private String to;
    /**
     * 发送钱包地址
     */
    private String from;
    /**
     * 交易金额，txtType为0时，表示转账金额
     * 委托和赎回改值都为0
     */
    private BigInteger amount;
    /**
     * 参与交易的data，包括交易类型(转账为0,委托为1004，赎回为1005)
     *
     */
    private PlatOnFunction platOnFunction;

    public QRCodeData() {

    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }

    public PlatOnFunction getPlatOnFunction() {
        return platOnFunction;
    }

    public void setPlatOnFunction(PlatOnFunction platOnFunction) {
        this.platOnFunction = platOnFunction;
    }
}
