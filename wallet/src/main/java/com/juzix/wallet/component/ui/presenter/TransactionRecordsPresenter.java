package com.juzix.wallet.component.ui.presenter;

import com.juzhen.framework.network.ApiRequestBody;
import com.juzhen.framework.network.ApiResponse;
import com.juzhen.framework.network.ApiSingleObserver;
import com.juzix.wallet.component.ui.base.BasePresenter;
import com.juzix.wallet.component.ui.contract.TransactionRecordsContract;
import com.juzix.wallet.engine.ServerUtils;
import com.juzix.wallet.entity.Transaction;
import com.juzix.wallet.utils.RxUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Predicate;

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
    public void fetchTransactions(String direction, List<String> addressList,boolean isWalletChanged) {

        ServerUtils
                .getCommonApi()
                .getTransactionList(ApiRequestBody.newBuilder()
                        .put("walletAddrs", addressList)
                        .put("beginSequence", getBeginSequenceByDirection(direction))
                        .put("listSize", 20)
                        .put("direction", direction)
                        .build())
                .compose(RxUtils.getSingleSchedulerTransformer())
                .compose(RxUtils.bindToLifecycle(getView()))
                .subscribe(new ApiSingleObserver<List<Transaction>>() {
                    @Override
                    public void onApiSuccess(List<Transaction> transactions) {
                        if (isViewAttached()) {
                            //先进行排序
                            List<Transaction> newTransactionList = getNewList(mTransactionList, transactions, DIRECTION_OLD.equals(direction));
                            Collections.sort(newTransactionList);
                            getView().notifyDataSetChanged(mTransactionList, newTransactionList,isWalletChanged);

                            mTransactionList = newTransactionList;

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

    private List<Transaction> getNewList(List<Transaction> oldTransactionList, List<Transaction> curTransactionList, boolean isLoadMore) {
        List<Transaction> oldList = oldTransactionList == null ? new ArrayList<Transaction>() : oldTransactionList;
        List<Transaction> curList = curTransactionList;
        List<Transaction> newList = new ArrayList<>();
        if (isLoadMore) {
            newList.addAll(oldList);
            newList.addAll(curList);
        } else {
            newList = curList;
        }

        return newList;
    }


    private List<Transaction> addAll(List<Transaction> transactionList) {
        return Flowable
                .fromIterable(transactionList)
                .filter(new Predicate<Transaction>() {
                    @Override
                    public boolean test(Transaction transaction) throws Exception {
                        return !mTransactionList.contains(transaction);
                    }
                })
                .collectInto(mTransactionList, new BiConsumer<List<Transaction>, Transaction>() {
                    @Override
                    public void accept(List<Transaction> transactionList, Transaction transaction) throws Exception {
                        transactionList.add(transactionList.size(), transaction);
                    }
                })
                .blockingGet();
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
