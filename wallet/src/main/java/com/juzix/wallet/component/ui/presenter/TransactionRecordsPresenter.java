package com.juzix.wallet.component.ui.presenter;

import com.juzhen.framework.network.ApiRequestBody;
import com.juzhen.framework.network.ApiResponse;
import com.juzhen.framework.network.ApiSingleObserver;
import com.juzix.wallet.component.ui.base.BasePresenter;
import com.juzix.wallet.component.ui.contract.TransactionRecordsContract;
import com.juzix.wallet.engine.WalletManager;
import com.juzix.wallet.engine.NodeManager;
import com.juzix.wallet.engine.ServerUtils;
import com.juzix.wallet.entity.Transaction;
import com.juzix.wallet.utils.RxUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author matrixelement
 */
public class TransactionRecordsPresenter extends BasePresenter<TransactionRecordsContract.View> implements TransactionRecordsContract.Presenter {

    public final static String DIRECTION_OLD = "old";
    public final static String DIRECTION_NEW = "new";

    private List<Transaction> mTransactionList;

    public TransactionRecordsPresenter(TransactionRecordsContract.View view) {
        super(view);
    }

    @Override
    public void fetchTransactions(String direction) {

        ServerUtils
                .getCommonApi()
                .getTransactionList(NodeManager.getInstance().getChainId(), ApiRequestBody.newBuilder()
                        .put("walletAddrs", WalletManager.getInstance().getAddressList())
                        .put("beginSequence", getBeginSequenceByDirection(direction))
                        .put("listSize", 10)
                        .put("direction", direction)
                        .build())
                .compose(RxUtils.getSingleSchedulerTransformer())
                .compose(RxUtils.bindToLifecycle(getView()))
                .subscribe(new ApiSingleObserver<List<Transaction>>() {
                    @Override
                    public void onApiSuccess(List<Transaction> transactions) {
                        if (isViewAttached()) {
                            //先进行排序
                            Collections.sort(transactions);
                            if (DIRECTION_OLD.equals(direction)) {
                                //累加,mTransactionList不可能为null,放在最后面
                                mTransactionList.addAll(mTransactionList.size(),transactions);
                            } else {
                                mTransactionList = transactions;
                            }

                            getView().showTransactions(mTransactionList);

                            if (DIRECTION_OLD.equals(direction)) {
                                getView().finishLoadMore();
                            } else {
                                getView().finishRefresh();
                            }
                        }
                    }

                    @Override
                    public void onApiFailure(ApiResponse response) {
                        if (isViewAttached()) {
                            if (DIRECTION_OLD.equals(direction)) {
                                getView().finishLoadMore();
                            } else {
                                getView().finishRefresh();
                            }
                        }
                    }
                });

    }

    private long getBeginSequenceByDirection(String direction) {
        //拉最新的
        if (mTransactionList == null || mTransactionList.isEmpty() || DIRECTION_NEW.equals(direction)) {
            return -1;
        }
        //分页加载取最后一个
        int size = mTransactionList.size();
        return mTransactionList.get(size - 1).getSequence();

    }


}
