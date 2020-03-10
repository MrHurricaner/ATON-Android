package com.platon.wallet.component.ui.contract;

import com.platon.wallet.component.ui.base.IPresenter;
import com.platon.wallet.component.ui.base.IView;
import com.platon.wallet.entity.Wallet;

/**
 * @author matrixelement
 */
public class SendTransationContract {

    public interface View extends IView {

        void updateWalletBalance(String balance);

        void setToAddress(String toAddress);

        void setTransferAmount(double amount);

        void setTransferFeeAmount(String feeAmount);

        String getTransferAmount();

        String getToAddress();

        Wallet getWalletEntityFromIntent();

        void showToAddressError(String errMsg);

        void showAmountError(String errMsg);

        void setSendTransactionButtonEnable(boolean enable);

        void setSendTransactionButtonVisible(boolean isVisible);

        void setSaveAddressButtonEnable(boolean enable);

        void resetView(String feeAmount);

        void showSaveAddressDialog();

        void setProgress(float progress);

        String getGasLimit();

        void setGasLimit(String gasLimit);

        void showGasLimitError(boolean isShow);

        boolean isShowAdvancedFunction();
    }

    public interface Presenter extends IPresenter<View> {

        void init();

        void fetchDefaultWalletInfo();

        void transferAllBalance();

        void calculateFee();

        void calculateFeeAndTime(float progress);

        boolean checkToAddress(String toAddress);

        boolean checkTransferAmount(String transferAmount);

        void submit();

        void updateSendTransactionButtonStatus();

        void checkAddressBook(String address);

        void saveWallet(String name, String address);

        void updateAssetsTab(int tabIndex);

        void setGasLimit(String gasLimit);
    }
}
