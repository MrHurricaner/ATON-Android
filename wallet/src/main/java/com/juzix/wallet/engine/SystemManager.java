package com.juzix.wallet.engine;

import com.juzix.wallet.entity.IndividualWalletEntity;
import com.juzix.wallet.entity.SharedWalletEntity;
import com.juzix.wallet.event.EventPublisher;

import java.util.ArrayList;

public class SystemManager {
    private volatile boolean mIsFinished;
    private          Monitor mMonitor;
    private static int sRefreshTime = 5000;

    private SystemManager() {

    }

    public static SystemManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void start() {
        stop();
        mIsFinished = false;
        mMonitor = new Monitor();
        mMonitor.start();
    }

    public void stop() {
        mIsFinished = true;
        if (mMonitor != null) {
            try {
                mMonitor.interrupt();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mMonitor = null;
        }
    }

    void refreshSharedTransactionList(){
        new Thread(){
            @Override
            public void run() {
                SharedWalletTransactionManager.getInstance().updateTransactions();
                EventPublisher.getInstance().sendUpdateSharedWalletTransactionEvent();
                EventPublisher.getInstance().sendUpdateMessageTipsEvent(SharedWalletTransactionManager.getInstance().unRead());
            }
        }.start();
    }

    void refreshShareWalletBalance(){
        new Thread(){
            @Override
            public void run() {
                ArrayList<SharedWalletEntity> walletList = SharedWalletManager.getInstance().getWalletList();
                for (SharedWalletEntity entity : walletList){
                    entity.setBalance(Web3jManager.getInstance().getBalance(entity.getPrefixContractAddress()));
                }
                EventPublisher.getInstance().sendUpdateSharedWalletBlanceEvent();
            }
        }.start();
    }

    void refreshIndividualTransactionList(){
        new Thread(){
            @Override
            public void run() {
                EventPublisher.getInstance().sendUpdateIndividualWalletTransactionEvent();
            }
        }.start();
    }

    void refreshIndividualWalletBalance(){
        new Thread(){
            @Override
            public void run() {
                ArrayList<IndividualWalletEntity> walletList = IndividualWalletManager.getInstance().getWalletList();
                for (IndividualWalletEntity entity : walletList){
                    entity.setBalance(Web3jManager.getInstance().getBalance(entity.getPrefixAddress()));
                }
                EventPublisher.getInstance().sendUpdateIndividualWalletBlanceEvent();
            }
        }.start();
    }

    private class Monitor extends Thread {
        @Override
        public void run() {
            while (!mIsFinished) {
                try {
                    refreshShareWalletBalance();
                    refreshSharedTransactionList();
                    refreshIndividualWalletBalance();
                    refreshIndividualTransactionList();
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
                try {
                    Thread.sleep(sRefreshTime);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }
        }
    }


    private static class InstanceHolder {
        private static volatile SystemManager INSTANCE = new SystemManager();
    }
}
