package com.juzix.wallet;


import com.juzix.wallet.utils.JSONUtil;

import org.web3j.platon.PlatOnFunction;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class Test {

    public static void main(String[] args) {

        QRCodeData qrCodeData = new QRCodeData();

        qrCodeData.setFrom("0x2e95e3ce0a54951eb9a99152a6d5827872dfb4fd");
        qrCodeData.setTo("0x2e95e3ce0a54951eb9a99152a6d5827872dfb4fd");
        qrCodeData.setAmount(BigInteger.valueOf(10000));
        qrCodeData.setChainId(100);
        qrCodeData.setGasPrice(BigInteger.valueOf(1000000000));
        qrCodeData.setGasLimit(BigInteger.valueOf(1000000000));
        qrCodeData.setNonce(BigInteger.valueOf(100));
        qrCodeData.setPlatOnFunction(new PlatOnFunction(0));

        System.out.println(JSONUtil.toJSONString(qrCodeData));


//        Flowable
//                .interval(1, TimeUnit.SECONDS)
//                .doOnNext(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
////                        System.out.println(aLong);
//                    }
//                })
//                .takeUntil(new Predicate<Long>() {
//                    @Override
//                    public boolean test(Long aLong) throws Exception {
//                        return aLong == 5;
//                    }
//                })
//                .filter(new Predicate<Long>() {
//                    @Override
//                    public boolean test(Long aLong) throws Exception {
//                        return aLong == 5;
//                    }
//                })
//                .subscribe(new Consumer<Long>() {
//                    @Override
//                    public void accept(Long aLong) throws Exception {
//                        System.out.println(aLong);
//                    }
//                });
//
//
//        try {
//            Thread.sleep(1000000000000L);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private static int getValue() {
        return Observable.fromArray(1, 2, 3)
                .filter(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer > 3;
                    }
                })
                .firstElement()
                .switchIfEmpty(new SingleSource<Integer>() {
                    @Override
                    public void subscribe(SingleObserver<? super Integer> observer) {
                        observer.onError(new Throwable());
                    }
                })
                .onErrorReturnItem(null)
                .blockingGet();
    }
}
