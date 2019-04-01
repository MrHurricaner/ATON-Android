package com.juzix.wallet.component.ui.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jakewharton.rxbinding3.view.RxView;
import com.juzhen.framework.util.NumberParserUtils;
import com.juzix.wallet.R;
import com.juzix.wallet.app.Constants;
import com.juzix.wallet.component.ui.base.MVPBaseActivity;
import com.juzix.wallet.component.ui.contract.SendIndividualTransationContract;
import com.juzix.wallet.component.ui.dialog.SelectIndividualWalletDialogFragment;
import com.juzix.wallet.component.ui.presenter.SendIndividualTransationPresenter;
import com.juzix.wallet.component.widget.CommonTitleBar;
import com.juzix.wallet.component.widget.PointLengthFilter;
import com.juzix.wallet.component.widget.RoundedTextView;
import com.juzix.wallet.config.PermissionConfigure;
import com.juzix.wallet.entity.AddressEntity;
import com.juzix.wallet.entity.IndividualWalletEntity;
import com.juzix.wallet.utils.AddressFormatUtil;
import com.juzix.wallet.utils.BigDecimalUtil;
import com.juzix.wallet.utils.JZWalletUtil;
import com.juzix.wallet.utils.ToastUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import butterknife.Unbinder;
import io.reactivex.functions.Consumer;
import kotlin.Unit;

/**
 * @author matrixelement
 */
public class SendIndividualTransationActivity extends MVPBaseActivity<SendIndividualTransationPresenter> implements SendIndividualTransationContract.View, SelectIndividualWalletDialogFragment.OnItemClickListener {

    @BindView(R.id.iv_address_book)
    ImageView ivAddressBook;
    @BindView(R.id.et_wallet_address)
    EditText etWalletAddress;
    @BindView(R.id.tv_transation_amount)
    TextView tvTransationAmount;
    @BindView(R.id.et_wallet_amount)
    EditText etWalletAmount;
    @BindView(R.id.tv_transaction_type)
    TextView tvTransactionType;
    @BindView(R.id.tv_all_amount)
    TextView tvAllAmount;
    @BindView(R.id.tv_select_wallet)
    TextView tvSelectWallet;
    @BindView(R.id.tv_wallet_name)
    TextView tvWalletName;
    @BindView(R.id.tv_wallet_address)
    TextView tvWalletAddress;
    @BindView(R.id.tv_fee_amount)
    TextView tvFeeAmount;
    @BindView(R.id.seekbar)
    AppCompatSeekBar seekbar;
    @BindView(R.id.rtv_send_transation)
    RoundedTextView rtvSendTransation;
    @BindView(R.id.tv_wallet_balance)
    TextView tvWalletBalance;
    @BindView(R.id.layout_transation_amount)
    LinearLayout layoutTransationAmount;
    @BindView(R.id.tv_transfer_time)
    TextView tvTransferTime;
    @BindView(R.id.tv_to_address_error)
    TextView tvToAddressError;
    @BindView(R.id.tv_amount_error)
    TextView tvAmountError;
    @BindView(R.id.et_memo)
    EditText etMemo;

    private Unbinder unbinder;

    @Override
    protected SendIndividualTransationPresenter createPresenter() {
        return new SendIndividualTransationPresenter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_transation);
        unbinder = ButterKnife.bind(this);
        initViews();
        mPresenter.init();
        mPresenter.fetchDefaultWalletInfo();
    }

    private void initViews() {

        etMemo.setVisibility(View.GONE);

//        new CommonTitleBar(this).setLeftDrawable(R.drawable.icon_back_black).setTitle(string(R.string.sendATP)).setRightDrawable(R.drawable.icon_scan, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                requestPermission(SendIndividualTransationActivity.this, 100, new PermissionConfigure.PermissionCallback() {
//                    @Override
//                    public void onSuccess(int what, @NonNull List<String> grantPermissions) {
//                        ScanQRCodeActivity.startActivityForResult(SendIndividualTransationActivity.this, Constants.RequestCode.REQUEST_CODE_SCAN_QRCODE);
//                    }
//
//                    @Override
//                    public void onHasPermission(int what) {
//                        ScanQRCodeActivity.startActivityForResult(SendIndividualTransationActivity.this, Constants.RequestCode.REQUEST_CODE_SCAN_QRCODE);
//                    }
//
//                    @Override
//                    public void onFail(int what, @NonNull List<String> deniedPermissions) {
//
//                    }
//                }, Manifest.permission.CAMERA);
//            }
//        }).build();

        etWalletAmount.setFilters(new InputFilter[]{new PointLengthFilter()});

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mPresenter.calculateFeeAndTime(BigDecimalUtil.div(progress, seekBar.getMax()));
                mPresenter.checkTransferAmount(etWalletAmount.getText().toString());
                mPresenter.updateSendTransactionButtonStatus();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        etWalletAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPresenter.checkTransferAmount(s.toString());
                mPresenter.updateSendTransactionButtonStatus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        etWalletAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPresenter.checkToAddressAndUpdateFee(s.toString());
                mPresenter.updateSendTransactionButtonStatus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RxView.clicks(rtvSendTransation)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Unit>() {
                    @Override
                    public void accept(Unit unit) throws Exception {
                        mPresenter.submit();
                    }
                });

    }

    @OnClick({R.id.iv_address_book, R.id.tv_all_amount, R.id.layout_change_wallet})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_address_book:
                SelectAddressActivity.actionStartForResult(this, Constants.Action.ACTION_GET_ADDRESS, Constants.RequestCode.REQUEST_CODE_GET_ADDRESS);
                break;
            case R.id.tv_all_amount:
                mPresenter.transferAllBalance();
                break;
            case R.id.layout_change_wallet:
                hideSoftInput();
                mPresenter.showSelectWalletDialogFragment();
                break;
            default:
                break;
        }

    }

    @OnTextChanged(value = R.id.et_wallet_amount, callback = OnTextChanged.Callback.TEXT_CHANGED)
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mPresenter.inputTransferAmount(s.toString());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case Constants.RequestCode.REQUEST_CODE_GET_ADDRESS:
                    AddressEntity addressEntity = data.getParcelableExtra(Constants.Extra.EXTRA_ADDRESS);
                    if (addressEntity != null) {
                        setToAddress(addressEntity.getAddress());
                        mPresenter.calculateFee();
                    }
                    break;
                case Constants.RequestCode.REQUEST_CODE_SCAN_QRCODE:
                    String address = data.getStringExtra(Constants.Extra.EXTRA_SCAN_QRCODE_DATA);
                    if (JZWalletUtil.isValidAddress(address)) {
                        setToAddress(address);
                        mPresenter.calculateFee();
                    } else {
                        ToastUtil.showLongToast(this, string(R.string.unrecognized));
                    }
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(IndividualWalletEntity walletEntity) {
        mPresenter.updateSendWalletInfoAndFee(walletEntity);
    }

    @Override
    public void updateWalletInfo(IndividualWalletEntity walletEntity) {
        tvWalletName.setText(walletEntity.getName());
        tvWalletAddress.setText(AddressFormatUtil.formatAddress(walletEntity.getPrefixAddress()));
        tvWalletBalance.setText(string(R.string.balance_text, NumberParserUtils.getPrettyBalance(walletEntity.getBalance())));
    }

    @Override
    public void setToAddress(String address) {
        etWalletAddress.setText(address);
    }

    @Override
    public void setTransferAmount(double amount) {
        etWalletAmount.setText(NumberParserUtils.getPrettyBalance(amount));
    }

    @Override
    public void setTransferAmountTextColor(boolean isBiggerThanBalance) {
        etWalletAmount.setTextColor(isBiggerThanBalance ? ContextCompat.getColor(this, R.color.color_ff4747) : ContextCompat.getColor(this, R.color.color_ffffff));
    }

    @Override
    public void setTransferFeeAmount(String feeAmount) {
        tvFeeAmount.setText(string(R.string.amount_with_unit, feeAmount));
    }

    @Override
    public void setTransferTime(String transferTime) {
        tvTransferTime.setText(transferTime);
    }

    @Override
    public String getTransferAmount() {
        return etWalletAmount.getText().toString();
    }

    @Override
    public String getToAddress() {
        return etWalletAddress.getText().toString();
    }

    @Override
    public IndividualWalletEntity getWalletEntityFromIntent() {
        return getIntent().getParcelableExtra(Constants.Extra.EXTRA_WALLET);
    }

    @Override
    public String getToAddressFromIntent() {
        return getIntent().getStringExtra(Constants.Extra.EXTRA_TO_ADDRESS);
    }

    @Override
    public void showToAddressError(String errMsg) {
        tvToAddressError.setVisibility(TextUtils.isEmpty(errMsg) ? View.GONE : View.VISIBLE);
        tvToAddressError.setText(errMsg);
    }

    @Override
    public void showAmountError(String errMsg) {
        tvAmountError.setVisibility(TextUtils.isEmpty(errMsg) ? View.GONE : View.VISIBLE);
        tvAmountError.setText(errMsg);
    }

    @Override
    public void setSendTransactionButtonEnable(boolean enable) {
        rtvSendTransation.setEnabled(enable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    public static void actionStart(Context context, String toAddress, IndividualWalletEntity individualWalletEntity) {
        Intent intent = new Intent(context, SendIndividualTransationActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TO_ADDRESS, toAddress);
        intent.putExtra(Constants.Extra.EXTRA_WALLET, individualWalletEntity);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, IndividualWalletEntity individualWalletEntity) {
        Intent intent = new Intent(context, SendIndividualTransationActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_WALLET, individualWalletEntity);
        context.startActivity(intent);
    }
}
