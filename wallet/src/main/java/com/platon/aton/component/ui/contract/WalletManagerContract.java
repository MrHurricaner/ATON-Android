package com.platon.aton.component.ui.contract;

import com.platon.aton.component.ui.base.IPresenter;
import com.platon.aton.component.ui.base.IView;
import com.platon.aton.entity.Wallet;

import java.util.ArrayList;

/**
 * @author matrixelement
 */
public class WalletManagerContract {

    public interface View extends IView {

        void notifyWalletListChanged();

        void showWalletList();

        void showEmpty();
    }

    public interface Presenter extends IPresenter<View> {

        ArrayList<Wallet> getDataSource();

        void fetchWalletList();

        void sortWalletList();

        void backupWallet(int position);

        void startAction(int position);
    }

}