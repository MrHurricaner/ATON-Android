package com.juzix.wallet.component.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.Barrier;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juzhen.framework.util.NumberParserUtils;
import com.juzix.wallet.R;
import com.juzix.wallet.app.Constants;
import com.juzix.wallet.component.adapter.SigningMemberAdapter;
import com.juzix.wallet.component.ui.base.MVPBaseActivity;
import com.juzix.wallet.component.ui.contract.SigningContract;
import com.juzix.wallet.component.ui.presenter.SigningPresenter;
import com.juzix.wallet.component.widget.RoundedTextView;
import com.juzix.wallet.component.widget.SpacesItemDecoration;
import com.juzix.wallet.entity.SharedTransactionEntity;
import com.juzix.wallet.entity.TransactionResult;
import com.juzix.wallet.event.EventPublisher;
import com.juzix.wallet.utils.CommonUtil;
import com.juzix.wallet.utils.DateUtil;
import com.juzix.wallet.utils.DensityUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author matrixelement
 */
public class SigningActivity extends MVPBaseActivity<SigningPresenter> implements SigningContract.View {

    @BindView(R.id.tv_transaction_status_desc)
    TextView tvTransactionStatusDesc;
    @BindView(R.id.gv_members)
    RecyclerView gvMembers;
    @BindView(R.id.tv_transaction_value_title)
    TextView tvTransactionValueTitle;
    @BindView(R.id.tv_transaction_value)
    TextView tvTransactionValue;
    @BindView(R.id.rtv_refuse)
    RoundedTextView rtvRefuse;
    @BindView(R.id.rtv_agree)
    RoundedTextView rtvAgree;
    @BindView(R.id.iv_copy_from_address)
    ImageView ivCopyFromAddress;
    @BindView(R.id.tv_from_address)
    TextView tvFromAddress;
    @BindView(R.id.layout_from_address)
    RelativeLayout layoutFromAddress;
    @BindView(R.id.iv_copy_to_address)
    ImageView ivCopyToAddress;
    @BindView(R.id.tv_to_address)
    TextView tvToAddress;
    @BindView(R.id.layout_to_address)
    RelativeLayout layoutToAddress;
    @BindView(R.id.tv_transaction_type_title)
    TextView tvTransactionTypeTitle;
    @BindView(R.id.tv_transaction_time_title)
    TextView tvTransactionTimeTitle;
    @BindView(R.id.tv_transaction_amount_title)
    TextView tvTransactionAmountTitle;
    @BindView(R.id.tv_transaction_energon_title)
    TextView tvTransactionEnergonTitle;
    @BindView(R.id.tv_transaction_wallet_name_title)
    TextView tvTransactionWalletNameTitle;
    @BindView(R.id.barrier)
    Barrier barrier;
    @BindView(R.id.tv_transaction_type)
    TextView tvTransactionType;
    @BindView(R.id.tv_transaction_time)
    TextView tvTransactionTime;
    @BindView(R.id.tv_transaction_amount)
    TextView tvTransactionAmount;
    @BindView(R.id.tv_transaction_energon)
    TextView tvTransactionEnergon;
    @BindView(R.id.tv_transaction_wallet_name)
    TextView tvTransactionWalletName;
    @BindView(R.id.tv_memo)
    TextView tvMemo;
    @BindView(R.id.layout_transation_hash)
    RelativeLayout layoutTransationHash;
    @BindView(R.id.tv_transation_hash_title)
    TextView tvTransationHashTitle;

    private Unbinder unbinder;
    private GridLayoutManager gridLayoutManager;
    private SigningMemberAdapter signingMemberAdapter;

    @Override
    protected SigningPresenter createPresenter() {
        return new SigningPresenter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);
        unbinder = ButterKnife.bind(this);
        EventPublisher.getInstance().register(this);
        initViews();
        mPresenter.init();
        mPresenter.fetchTransactionDetail();
    }

    public void initViews() {
        tvTransationHashTitle.setVisibility(View.GONE);
        layoutTransationHash.setVisibility(View.GONE);

        gridLayoutManager = new GridLayoutManager(this, 1);
        gvMembers.setLayoutManager(gridLayoutManager);
        gvMembers.addItemDecoration(new SpacesItemDecoration(DensityUtil.dp2px(this, 11f), DensityUtil.dp2px(this, 4f)));
        signingMemberAdapter = new SigningMemberAdapter();
        gvMembers.setAdapter(signingMemberAdapter);
    }

    @OnClick({R.id.iv_copy_from_address, R.id.iv_copy_to_address, R.id.rtv_refuse, R.id.rtv_agree})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_copy_from_address:
                CommonUtil.copyTextToClipboard(this, tvFromAddress.getText().toString());
                break;
            case R.id.iv_copy_to_address:
                CommonUtil.copyTextToClipboard(this, tvToAddress.getText().toString());
                break;
            case R.id.rtv_refuse:
                mPresenter.revoke();
                break;
            case R.id.rtv_agree:
                mPresenter.confirm();
                break;
            default:
                break;
        }
    }

    @Override
    public SharedTransactionEntity getTransactionFromIntent() {
        return getIntent().getParcelableExtra(Constants.Extra.EXTRA_TRANSACTION);
    }

    @Override
    public void setTransactionDetailInfo(SharedTransactionEntity transactionEntity, String statusDesc) {

        tvTransactionValue.setText(string(R.string.amount_with_unit, String.format("%s%s", transactionEntity.isReceiver(transactionEntity.getContractAddress()) ? "+" : "-", NumberParserUtils.getPrettyBalance(transactionEntity.getValue()))));
        tvTransactionStatusDesc.setText(statusDesc);
        tvFromAddress.setText(transactionEntity.getFromAddress());
        tvToAddress.setText(transactionEntity.getToAddress());

        tvTransactionType.setText(transactionEntity.isReceiver(transactionEntity.getContractAddress()) ? R.string.receive : R.string.send);
        tvTransactionTime.setText(DateUtil.format(transactionEntity.getCreateTime(), DateUtil.DATETIME_FORMAT_PATTERN));
        tvTransactionAmount.setText(string(R.string.amount_with_unit, String.format("%s%s", transactionEntity.isReceiver(transactionEntity.getContractAddress()) ? "+" : "-", NumberParserUtils.getPrettyBalance(transactionEntity.getValue()))));
        tvTransactionEnergon.setText("-");
        tvTransactionWalletName.setText(transactionEntity.getWalletName());
        tvMemo.setText(transactionEntity.getMemo());
    }

    @Override
    public void showTransactionResult(ArrayList<TransactionResult> transactionResultList) {
        int len = transactionResultList.size();
        gridLayoutManager.setSpanCount(Math.min(len, 5));
        signingMemberAdapter.notifyDataSetChanged(transactionResultList);

    }

    @Override
    public void enableButtons(boolean enabaled) {
        rtvRefuse.setVisibility(enabaled ? View.VISIBLE : View.GONE);
        rtvAgree.setVisibility(enabaled ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateSigningStatus(String address, int operation) {
        signingMemberAdapter.notifyItemChanged(address, operation);
    }

    @Override
    public void signFinished() {
        enableButtons(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventPublisher.getInstance().unRegister(this);
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    public static void actionStart(Activity activity, SharedTransactionEntity transactionEntity) {
        Intent intent = new Intent(activity, SigningActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TRANSACTION, transactionEntity);
        activity.startActivity(intent);
    }
}
