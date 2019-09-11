package com.juzix.wallet.component.ui.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.juzhen.framework.util.LogUtils;
import com.juzhen.framework.util.NumberParserUtils;
import com.juzix.wallet.R;
import com.juzix.wallet.app.Constants;
import com.juzix.wallet.app.CustomObserver;
import com.juzix.wallet.app.LoadingTransformer;
import com.juzix.wallet.component.adapter.base.RecycleHolder;
import com.juzix.wallet.component.adapter.base.RecyclerAdapter;
import com.juzix.wallet.component.ui.base.BaseFragment;
import com.juzix.wallet.component.ui.dialog.InputWalletPasswordDialogFragment;
import com.juzix.wallet.component.ui.dialog.OnDialogViewClickListener;
import com.juzix.wallet.component.widget.ShadowButton;
import com.juzix.wallet.component.widget.TextChangedListener;
import com.juzix.wallet.engine.NodeManager;
import com.juzix.wallet.engine.WalletManager;
import com.juzix.wallet.engine.Web3jManager;
import com.juzix.wallet.entity.Wallet;
import com.juzix.wallet.utils.JZWalletUtil;
import com.juzix.wallet.utils.RxUtils;
import com.juzix.wallet.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.web3j.crypto.Credentials;
import org.web3j.platon.BaseResponse;
import org.web3j.platon.bean.RestrictingPlan;
import org.web3j.platon.contracts.RestrictingPlanContract;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class RestrictingPlanFragment extends BaseFragment {

    @BindView(R.id.et_benifit_address)
    EditText etBenifitAddress;
    @BindView(R.id.iv_address_scan)
    ImageView ivAddressScan;
    @BindView(R.id.list_restricting_plan)
    RecyclerView listRestrictingPlan;
    @BindView(R.id.sbtn_create)
    ShadowButton sbtnCreate;
    @BindView(R.id.tv_add_restricting_plan)
    TextView tvAddRestrictingPlan;

    Unbinder unbinder;
    List<RestrictingPlanItem> mRestrictingPlanList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restricting_plan, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    private void initView() {

        RestrictingPlanAdapter restrictingPlanAdapter = new RestrictingPlanAdapter(getActivity(), mRestrictingPlanList, R.layout.item_restricting_plan);

        listRestrictingPlan.setLayoutManager(new LinearLayoutManager(getContext()));
        listRestrictingPlan.setAdapter(restrictingPlanAdapter);

        restrictingPlanAdapter.setItemRemoveListener(new OnItemRemoveListener() {
            @Override
            public void onItemRemove(int positon) {
                int size = mRestrictingPlanList.size();
                mRestrictingPlanList.remove(positon);
                restrictingPlanAdapter.notifyItemRangeChanged(positon, size - positon);
            }
        });

        RxView
                .clicks(tvAddRestrictingPlan)
                .compose(RxUtils.bindToLifecycle(this))
                .subscribe(new CustomObserver<Object>() {

                    @Override
                    public void accept(Object o) {
                        int size = mRestrictingPlanList.size();
                        mRestrictingPlanList.add(new RestrictingPlanItem());
                        restrictingPlanAdapter.notifyItemInserted(size);
                    }
                });

        RxView
                .clicks(ivAddressScan)
                .compose(RxUtils.bindToLifecycle(this))
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        new RxPermissions(getActivity())
                                .requestEach(Manifest.permission.CAMERA)
                                .subscribe(new CustomObserver<Object>() {
                                    @Override
                                    public void accept(Object object) {
                                        Permission permission = (Permission) object;
                                        if (permission.granted) {
                                            ScanQRCodeActivity.startActivityForResult(getActivity(), Constants.RequestCode.REQUEST_CODE_SCAN_QRCODE);
                                        }
                                    }
                                });

                    }
                });

        sbtnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRestrictingPlanList.isEmpty() || TextUtils.isEmpty(etBenifitAddress.getText().toString())) {
                    ToastUtil.showLongToast(getActivity(), "锁仓信息不能为空");
                    return;
                }

                Wallet wallet = WalletManager.getInstance().getWalletList().get(0);
                InputWalletPasswordDialogFragment.newInstance(wallet).setOnWalletPasswordCorrectListener(new InputWalletPasswordDialogFragment.OnWalletPasswordCorrectListener() {
                    @Override
                    public void onWalletPasswordCorrect(Credentials credentials) {
                        createRestrictingPlan(credentials, getRestrictingPlanList(mRestrictingPlanList), etBenifitAddress.getText().toString().trim())
                                .subscribe(new Consumer<BaseResponse>() {
                                    @Override
                                    public void accept(BaseResponse baseResponse) {
                                        LogUtils.e(baseResponse.toString());
                                        if (baseResponse.isStatusOk()) {
                                            showLongToast("创建锁仓成功");
                                        } else {
                                            showLongToast(baseResponse.errMsg);
                                        }
                                    }
                                });
                        ;
                    }
                }).show(currentActivity().getSupportFragmentManager(), "inputPassword");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.RequestCode.REQUEST_CODE_SCAN_QRCODE:
                    String address = data.getStringExtra(Constants.Extra.EXTRA_SCAN_QRCODE_DATA);
                    if (JZWalletUtil.isValidAddress(address)) {
                        etBenifitAddress.setText(address);
                    } else {
                        ToastUtil.showLongToast(getContext(), string(R.string.unrecognized));
                    }
                    break;
                default:
                    break;
            }

        }
    }

    static class RestrictingPlanAdapter extends RecyclerAdapter<RestrictingPlanItem> {

        private OnItemRemoveListener mItemRemoveListener;

        public OnItemRemoveListener getItemRemoveListener() {
            return mItemRemoveListener;
        }

        public void setItemRemoveListener(OnItemRemoveListener itemRemoveListener) {
            this.mItemRemoveListener = itemRemoveListener;
        }

        public RestrictingPlanAdapter(Context mContext, List<RestrictingPlanItem> mDatas, int mLayoutId) {
            super(mContext, mDatas, mLayoutId);
        }

        @Override
        public void convert(RecycleHolder holder, RestrictingPlanItem data, int position) {
            holder.setOnClickListener(R.id.iv_remove, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemRemoveListener != null) {
                        mItemRemoveListener.onItemRemove(position);
                    }
                }
            });

            EditText restrictingPlanEpochEt = holder.findView(R.id.et_restricting_plan_Epoch);
            EditText restrictingPlanAmountEt = holder.findView(R.id.et_restricting_plan_amount);

            restrictingPlanEpochEt.addTextChangedListener(new TextChangedListener() {
                @Override
                protected void onTextChanged(CharSequence s) {
                    data.setEpoch(s.toString().trim());
                }
            });

            restrictingPlanAmountEt.addTextChangedListener(new TextChangedListener() {
                @Override
                protected void onTextChanged(CharSequence s) {
                    data.setAmount(s.toString().trim());
                }
            });
        }
    }

    static class RestrictingPlanItem {

        /**
         * 表示结算周期的倍数。与每个结算周期出块数的乘积表示在目标区块高度上释放锁定的资金。如果 account 是激励池地址，
         * 那么 period 值是 120（即，30*4） 的倍数。另外，period * 每周期的区块数至少要大于最高不可逆区块高度
         */
        private String epoch;

        /**
         * 表示目标区块上待释放的金额
         */
        private String amount;

        public String getEpoch() {
            return epoch;
        }

        public void setEpoch(String epoch) {
            this.epoch = epoch;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public RestrictingPlan toRestrictingPlan() {
            return new RestrictingPlan(new BigInteger(epoch), Convert.toVon(amount, Convert.Unit.LAT).toBigInteger());
        }

        @Override
        public String toString() {
            return "RestrictingPlanItem{" +
                    "epoch='" + epoch + '\'' +
                    ", amount='" + amount + '\'' +
                    '}';
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    private Single<BaseResponse> createRestrictingPlan(Credentials credentials, List<RestrictingPlan> restrictingPlanList, String benifitAddress) {

        return Single
                .fromCallable(new Callable<BaseResponse>() {
                    @Override
                    public BaseResponse call() throws Exception {
                        RestrictingPlanContract restrictingPlanContract = RestrictingPlanContract.load(Web3jManager.getInstance().getWeb3j(), credentials, NumberParserUtils.parseLong(NodeManager.getInstance().getChainId()));
                        return restrictingPlanContract.createRestrictingPlan(benifitAddress, restrictingPlanList).send();
                    }
                })
                .compose(RxUtils.getSingleSchedulerTransformer())
                .compose(LoadingTransformer.bindToSingleLifecycle(currentActivity()))
                .compose(bindToLifecycle());


    }

    private List<RestrictingPlan> getRestrictingPlanList(List<RestrictingPlanItem> restrictingPlanItemList) {
        return Flowable
                .fromIterable(restrictingPlanItemList)
                .map(new Function<RestrictingPlanItem, RestrictingPlan>() {
                    @Override
                    public RestrictingPlan apply(RestrictingPlanItem restrictingPlanItem) throws Exception {
                        return restrictingPlanItem.toRestrictingPlan();
                    }
                })
                .toList()
                .blockingGet();
    }

    public interface OnItemRemoveListener {

        void onItemRemove(int positon);
    }
}
