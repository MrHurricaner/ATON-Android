package com.juzix.wallet.component.ui.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juzhen.framework.util.LogUtils;
import com.juzhen.framework.util.NumberParserUtils;
import com.juzhen.framework.util.RUtils;
import com.juzix.wallet.R;
import com.juzix.wallet.app.Constants;
import com.juzix.wallet.app.CustomObserver;
import com.juzix.wallet.component.adapter.RecycleViewProxyAdapter;
import com.juzix.wallet.component.adapter.TabAdapter;
import com.juzix.wallet.component.adapter.WalletHorizontalRecycleViewAdapter;
import com.juzix.wallet.component.ui.base.BaseFragment;
import com.juzix.wallet.component.ui.base.MVPBaseFragment;
import com.juzix.wallet.component.ui.contract.AssetsContract;
import com.juzix.wallet.component.ui.dialog.AssetsMoreDialogFragment;

import com.juzix.wallet.component.ui.dialog.BaseDialogFragment;

import com.juzix.wallet.component.ui.dialog.CommonGuideDialogFragment;
import com.juzix.wallet.component.ui.dialog.TransactionSignatureDialogFragment;

import com.juzix.wallet.component.ui.presenter.AssetsPresenter;
import com.juzix.wallet.component.widget.AmountTransformationMethod;
import com.juzix.wallet.component.widget.CustomImageSpan;
import com.juzix.wallet.component.widget.ShadowContainer;
import com.juzix.wallet.component.widget.ViewPagerSlide;
import com.juzix.wallet.component.widget.table.AssetsTabLayout;
import com.juzix.wallet.config.AppSettings;
import com.juzix.wallet.engine.WalletManager;
import com.juzix.wallet.entity.GuideType;
import com.juzix.wallet.entity.QrCodeType;
import com.juzix.wallet.entity.TransactionAuthorizationData;
import com.juzix.wallet.entity.TransactionSignatureData;
import com.juzix.wallet.entity.Wallet;
import com.juzix.wallet.event.Event;
import com.juzix.wallet.event.EventPublisher;
import com.juzix.wallet.netlistener.NetworkType;
import com.juzix.wallet.netlistener.NetworkUtil;
import com.juzix.wallet.utils.BigDecimalUtil;
import com.juzix.wallet.utils.GZipUtil;
import com.juzix.wallet.utils.JSONUtil;

import com.juzix.wallet.utils.QrCodeParser;

import com.juzix.wallet.utils.StringUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author matrixelement
 */
public class AssetsFragment extends MVPBaseFragment<AssetsPresenter> implements AssetsContract.View {


    @IntDef({
            MainTab.TRANSACTION_LIST,
            MainTab.SEND_TRANSACTION,
            MainTab.RECEIVE_TRANSACTION
    })
    public @interface MainTab {

        int TRANSACTION_LIST = 0;

        int SEND_TRANSACTION = 1;

        int RECEIVE_TRANSACTION = 2;
    }

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.v_tab_line)
    View vTabLine;
    @BindView(R.id.ll_assets_title)
    LinearLayout llAssetsTitle;
    @BindView(R.id.tv_total_assets_title)
    TextView tvTotalAssetsUnit;
    @BindView(R.id.layout_add)
    LinearLayout addLayout;
    @BindView(R.id.layout_scan)
    LinearLayout scanLayout;
    @BindView(R.id.tv_total_assets_amount)
    TextView tvTotalAssetsAmount;
    @BindView(R.id.stb_bar)
    AssetsTabLayout stbBar;
    @BindView(R.id.rv_wallet)
    RecyclerView rvWallet;
    @BindView(R.id.rl_wallet_detail)
    RelativeLayout rlWalletDetail;
    @BindView(R.id.iv_wallet_avatar)
    ImageView ivWalletAvatar;
    @BindView(R.id.tv_wallet_name)
    TextView tvWalletName;
    @BindView(R.id.tv_wallet_amount)
    TextView tvWalletAmount;
    @BindView(R.id.tv_backup)
    TextView tvBackup;
    @BindView(R.id.vp_content)
    ViewPagerSlide vpContent;
    @BindView(R.id.layout_empty)
    ConstraintLayout layoutEmpty;
    @BindString(R.string.transactions)
    String transaction;
    @BindString(R.string.action_send_transation)
    String send;
    @BindString(R.string.action_receive_transation)
    String receive;
    @BindView(R.id.sc_import_wallet)
    ShadowContainer scImportWallet;
    @BindView(R.id.sc_create_wallet)
    ShadowContainer scCreateWallet;
    @BindView(R.id.layout_refresh)
    SmartRefreshLayout layoutRefresh;
    @BindView(R.id.tv_restricted_amount)
    TextView tvRestrictedAmount;

    private WalletHorizontalRecycleViewAdapter mWalletAdapter;
    private Unbinder unbinder;
    private TabAdapter mTabAdapter;

    @Override
    protected AssetsPresenter createPresenter() {
        return new AssetsPresenter(this);
    }

    @Override
    protected void onFragmentPageStart() {
        mPresenter.fetchWalletsBalance();
        mPresenter.fetchWalletList();

    }

    @Override
    protected View onCreateFragmentPage(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_assets, container, false);
        unbinder = ButterKnife.bind(this, rootView);
        EventPublisher.getInstance().register(this);
        initViews();
        return rootView;
    }

    private void initViews() {
        initIndicator();
        initRefreshLayout();
        initHeader();
        initTab();
        showAssets(AppSettings.getInstance().getShowAssetsFlag());
        showContent(true);
        initGuide();
    }

    private void initGuide() {
        if (!AppSettings.getInstance().getRecordBoolean()) {
            CommonGuideDialogFragment.newInstance(GuideType.TRANSACTION_LIST).setOnDissmissListener(new BaseDialogFragment.OnDissmissListener() {
                @Override
                public void onDismiss() {
                    AppSettings.getInstance().setRecordBoolean(true);
                }
            }).show(getActivity().getSupportFragmentManager(), "showGuideDialogFragment");
        }

    }

    private void initIndicator() {
        List<BaseFragment> fragments = getFragments(null);
        stbBar.setCustomTabView(new AssetsTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                return new TabView(container.getContext(), position);
            }
        });

        int indicatorThickness = (int) getResources().getDimension(R.dimen.assetsCollapsIndicatorThickness);
        int indicatorCornerRadius = indicatorThickness / 2;

        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) stbBar.getLayoutParams();
        params.topMargin = 0;
        stbBar.setIndicatorCornerRadius(indicatorCornerRadius);
        stbBar.setIndicatorThickness(indicatorThickness);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                boolean collapsed = Math.abs(i) >= appBarLayout.getTotalScrollRange();
                vTabLine.setVisibility(collapsed ? View.GONE : View.VISIBLE);
                appBarLayout.setBackgroundColor(ContextCompat.getColor(getContext(), collapsed ? R.color.color_ffffff : R.color.color_f9fbff));
            }
        });
        vpContent.setOffscreenPageLimit(fragments.size());
        mTabAdapter = new TabAdapter(getChildFragmentManager(), getTitles(), fragments);
        vpContent.setAdapter(mTabAdapter);
        vpContent.setSlide(true);
        stbBar.setViewPager(vpContent);
    }

    private void initRefreshLayout() {

        layoutRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                mPresenter.fetchWalletList();
                mPresenter.fetchWalletsBalance();
            }
        });
    }

    private void saveStateToArguments() {
        Bundle saveState = saveState();
        LogUtils.e("saveStateToArguments");
        if (saveState != null) {
            Bundle bundle = getArguments(); // 获取之前初始化时候的bundle
            bundle.putBundle(Constants.Extra.EXTRA_BUNDLE, saveState);//将bundle传入数据
        }
    }

    private void reStoreStateFromArguments() {
        Bundle bundle = getArguments().getBundle(Constants.Extra.EXTRA_BUNDLE);
        LogUtils.e("reStoreStateFromArguments");
        if (bundle != null) {
            List<Wallet> walletList = bundle.getParcelableArrayList(Constants.Extra.EXTRA_WALLET_LIST);
            LogUtils.e("reStoreStateFromArguments" + walletList.size());
            showContent(walletList == null || walletList.isEmpty());
        }
    }

    private Bundle saveState() {
        Bundle outState = new Bundle();
        List<Wallet> walletList = mPresenter.getRecycleViewDataSource();
        if (walletList != null && walletList.size() > 0) {
            outState.putParcelableArrayList(Constants.Extra.EXTRA_WALLET_LIST, (ArrayList<? extends Parcelable>) walletList);
        }
        return outState;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveStateToArguments();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        reStoreStateFromArguments();
    }

    public void fetchWalletsBalance() {
        mPresenter.fetchWalletsBalance();
    }

    public void initTab() {

        stbBar.getTabAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((NetworkUtil.getNetWorkType(getContext()) == NetworkType.NETWORK_NO)) { //没有网络，调起相机界面
                    new RxPermissions(currentActivity())
                            .request(Manifest.permission.CAMERA)
                            .subscribe(new CustomObserver<Boolean>() {
                                @Override
                                public void accept(Boolean success) {
                                    if (success) {
                                        ScanQRCodeActivity.startActivityForResult(currentActivity(), MainActivity.REQ_ASSETS_TAB_QR_CODE);
                                    }
                                }
                            });
                } else {
                    vpContent.setCurrentItem(1);
                }
            }
        });

        stbBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                EventPublisher.getInstance().sendUpdateAssetsTabEvent(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case MainActivity.REQ_ASSETS_TAB_QR_CODE:
                String result = data.getStringExtra(Constants.Extra.EXTRA_SCAN_QRCODE_DATA);
                String unzip = GZipUtil.unCompress(result);
                if (TextUtils.isEmpty(unzip) && TextUtils.isEmpty(result)) {
                    showLongToast(currentActivity().string(R.string.unrecognized_content));
                    return;
                }
                @QrCodeType int qrCodeType = QrCodeParser.parseQrCode(TextUtils.isEmpty(unzip) ? result : unzip);

                if (qrCodeType == QrCodeType.NONE) {
                    showLongToast(currentActivity().string(R.string.unrecognized));
                    return;
                }

                if (qrCodeType == QrCodeType.TRANSACTION_AUTHORIZATION) {
                    TransactionAuthorizationDetailActivity.actionStart(currentActivity(), JSONUtil.parseObject(unzip, TransactionAuthorizationData.class));
                    return;
                }

                if (qrCodeType == QrCodeType.TRANSACTION_SIGNATURE) {
                    TransactionSignatureDialogFragment.newInstance(JSONUtil.parseObject(unzip, TransactionSignatureData.class)).show(getActivity().getSupportFragmentManager(), TransactionSignatureDialogFragment.TAG);
                    return;
                }

                if (qrCodeType == QrCodeType.WALLET_ADDRESS) {
                    if (vpContent.getVisibility() == View.VISIBLE) {
                        vpContent.setCurrentItem(1);
                        ((TabAdapter) vpContent.getAdapter()).getItem(1).onActivityResult(MainActivity.REQ_ASSETS_ADDRESS_QR_CODE, resultCode, data);
                    } else {
                        AddNewAddressActivity.actionStartWithAddress(getContext(), TextUtils.isEmpty(unzip) ? result : unzip);
                    }
                    return;
                }

                if (qrCodeType == QrCodeType.WALLET_KEYSTORE) {
                    ImportWalletActivity.actionStart(currentActivity(), ImportWalletActivity.TabIndex.IMPORT_KEYSTORE, TextUtils.isEmpty(unzip) ? result : unzip);
                    return;
                }

                if (qrCodeType == QrCodeType.WALLET_MNEMONIC) {
                    ImportWalletActivity.actionStart(currentActivity(), ImportWalletActivity.TabIndex.IMPORT_MNEMONIC, TextUtils.isEmpty(unzip) ? result : unzip);
                    return;
                }

                if (qrCodeType == QrCodeType.WALLET_PRIVATEKEY) {
                    ImportWalletActivity.actionStart(currentActivity(), ImportWalletActivity.TabIndex.IMPORT_PRIVATEKEY, TextUtils.isEmpty(unzip) ? result : unzip);
                    return;
                }
                break;
            case MainActivity.REQ_ASSETS_ADDRESS_QR_CODE:
            case MainActivity.REQ_ASSETS_SELECT_ADDRESS_BOOK:
                if (vpContent.getVisibility() == View.VISIBLE) {
                    ((TabAdapter) vpContent.getAdapter()).getItem(1).onActivityResult(requestCode, resultCode, data);
                }
                break;
            case Constants.RequestCode.REQUEST_CODE_TRANSACTION_SIGNATURE:
                getActivity().getSupportFragmentManager().findFragmentByTag(TransactionSignatureDialogFragment.TAG).onActivityResult(Constants.RequestCode.REQUEST_CODE_TRANSACTION_SIGNATURE, resultCode, data);
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.layout_scan, R.id.tv_total_assets_title, R.id.tv_backup, R.id.layout_add, R.id.tv_restricted_amount, R.id.rl_wallet_detail})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layout_scan://扫一扫功能
                new RxPermissions(currentActivity())
                        .request(Manifest.permission.CAMERA)
                        .subscribe(new CustomObserver<Boolean>() {
                            @Override
                            public void accept(Boolean success) {
                                if (success) {
                                    ScanQRCodeActivity.startActivityForResult(currentActivity(), MainActivity.REQ_ASSETS_TAB_QR_CODE);
                                }
                            }
                        });
                break;
            case R.id.layout_add:
                AssetsMoreDialogFragment.newInstance().setOnAssetMoreClickListener(new AssetsMoreDialogFragment.OnAssetMoreClickListener() {
                    @Override
                    public void onCreateWalletClick() {
                        CreateWalletActivity.actionStart(getContext());
                    }

                    @Override
                    public void onImportWalletClick() {
                        ImportWalletActivity.actionStart(getContext());
                    }
                }).show(getChildFragmentManager(), "showAssetsMore");
                break;
            case R.id.tv_total_assets_title:
                AppSettings.getInstance().setShowAssetsFlag(!AppSettings.getInstance().getShowAssetsFlag());
                showAssets(AppSettings.getInstance().getShowAssetsFlag());
                break;
            case R.id.tv_backup:
                mPresenter.backupWallet();
                break;
            case R.id.tv_restricted_amount:
                showLongToast(R.string.restricted_amount_tips);
                break;
            case R.id.rl_wallet_detail:
                ManageWalletActivity.actionStart(currentActivity(), mWalletAdapter.getSelectedWallet());
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWalletListOrderChangedEvent(Event.WalletListOrderChangedEvent event) {
        mWalletAdapter.notify();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveStateToArguments();
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventPublisher.getInstance().unRegister(this);
    }

    private String makeFragmentName(long id) {
        return "android:switcher:" + vpContent.getId() + ":" + id;
    }

    private void initHeader() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvWallet.setLayoutManager(linearLayoutManager);
        mWalletAdapter = new WalletHorizontalRecycleViewAdapter(getContext(), mPresenter.getRecycleViewDataSource());
        mWalletAdapter.setOnItemClickListener(new WalletHorizontalRecycleViewAdapter.OnRecycleViewItemClickListener() {
            @Override
            public void onContentViewClick(Wallet walletEntity) {
                mPresenter.clickRecycleViewItem(WalletManager.getInstance().getWalletByAddress(walletEntity.getPrefixAddress()));
            }
        });
        RecycleViewProxyAdapter proxyAdapter = new RecycleViewProxyAdapter(mWalletAdapter);
        rvWallet.setAdapter(proxyAdapter);
        proxyAdapter.addFooterView(getCreateWalletView());
        proxyAdapter.addFooterView(getImportWalletView());
    }

    private View getCreateWalletView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_list_footer, null);
        ((TextView) view.findViewById(R.id.tv_name)).setText(R.string.createIndividualWallet);
        ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.drawable.icon_assets_create);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateWalletActivity.actionStart(getContext());
            }
        });
        return view;
    }

    private View getImportWalletView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_list_footer, null);
        ((TextView) view.findViewById(R.id.tv_name)).setText(R.string.importIndividualWallet);
        ((ImageView) view.findViewById(R.id.iv_icon)).setImageResource(R.drawable.icon_assets_import);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImportWalletActivity.actionStart(getContext());
            }
        });
        return view;
    }

    private void showAssets(boolean visible) {
        tvTotalAssetsUnit.setCompoundDrawablesWithIntrinsicBounds(0, 0, visible ? R.drawable.icon_open_eyes : R.drawable.icon_close_eyes, 0);
        tvTotalAssetsAmount.setTransformationMethod(visible ? HideReturnsTransformationMethod.getInstance() : new AmountTransformationMethod(tvTotalAssetsAmount.getText().toString()));
        tvWalletAmount.setTransformationMethod(visible ? HideReturnsTransformationMethod.getInstance() : new AmountTransformationMethod(tvWalletAmount.getText().toString()));
        tvRestrictedAmount.setTransformationMethod(visible ? HideReturnsTransformationMethod.getInstance() : new AmountTransformationMethod(tvRestrictedAmount.getText().toString()));
    }

    private ArrayList<String> getTitles() {
        ArrayList<String> titleList = new ArrayList<>();
        titleList.add(string(R.string.transactions));
        titleList.add((NetworkUtil.getNetWorkType(getContext()) != NetworkType.NETWORK_NO) ? string(R.string.action_send_transation) : string(R.string.wallet_send_offline_signature));
        titleList.add(string(R.string.action_receive_transation));
        return titleList;
    }

    private ArrayList<Integer> getCollapsIcons() {
        ArrayList<Integer> titleList = new ArrayList<>();
        titleList.add(R.drawable.assets_tab_transactions_icon);
        titleList.add(R.drawable.assets_tab_send_icon);
        titleList.add(R.drawable.assets_tab_receive_icon);
        return titleList;
    }

    private List<BaseFragment> getFragments(Wallet walletEntity) {
        List<BaseFragment> list = new ArrayList<>();
        list.add(getFragment(MainTab.TRANSACTION_LIST, walletEntity));
        list.add(getFragment(MainTab.SEND_TRANSACTION, walletEntity));
        list.add(getFragment(MainTab.RECEIVE_TRANSACTION, walletEntity));
        return list;
    }

    private BaseFragment getFragment(@MainTab int tab, Wallet walletEntity) {
        BaseFragment fragment = null;
        switch (tab) {
            case MainTab.TRANSACTION_LIST:
                fragment = new TransactionsFragment();
                break;
            case MainTab.SEND_TRANSACTION:
                fragment = new SendTransactionFragment();
                break;
            case MainTab.RECEIVE_TRANSACTION:
                fragment = new ReceiveTransactionFragment();
                break;
            default:
                break;
        }
        if (walletEntity != null) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.Extra.EXTRA_WALLET, walletEntity);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void showTotalBalance(String totalBalance) {//显示总资产
        boolean visible = AppSettings.getInstance().getShowAssetsFlag();
        tvTotalAssetsAmount.setText(StringUtil.formatBalance(BigDecimalUtil.div(totalBalance, "1E18")));
        tvTotalAssetsAmount.setTransformationMethod(visible ? HideReturnsTransformationMethod.getInstance() : new AmountTransformationMethod(tvTotalAssetsAmount.getText().toString()));
    }

    @Override
    public void showFreeBalance(String balance) {//当前钱包的资产

        tvWalletAmount.setText(string(R.string.amount_with_unit, StringUtil.formatBalance(BigDecimalUtil.div(balance, "1E18"), false)));
        tvWalletAmount.setTransformationMethod(AppSettings.getInstance().getShowAssetsFlag() ? HideReturnsTransformationMethod.getInstance() : new AmountTransformationMethod(tvWalletAmount.getText().toString()));

        if (vpContent.getCurrentItem() == MainTab.SEND_TRANSACTION) {
            SendTransactionFragment sendTransactionFragment = (SendTransactionFragment) mTabAdapter.getItem(MainTab.SEND_TRANSACTION);
            if (sendTransactionFragment != null) {
                sendTransactionFragment.updateWalletBalance(balance);
            }
        }
    }

    @Override
    public void showLockBalance(String balance) { //当前选中钱包的锁仓金额
        tvRestrictedAmount.setVisibility(BigDecimalUtil.isBiggerThanZero(balance) ? View.VISIBLE : View.GONE);
        tvRestrictedAmount.setText(getRestrictedAmount(string(R.string.restricted_amount_with_unit, StringUtil.formatBalance(BigDecimalUtil.div(balance, "1E18")))));
        tvRestrictedAmount.setTransformationMethod(AppSettings.getInstance().getShowAssetsFlag() ? HideReturnsTransformationMethod.getInstance() : new AmountTransformationMethod(tvRestrictedAmount.getText().toString()));
    }

    @Override
    public void showWalletList(Wallet walletEntity) {
        mWalletAdapter.notifyDataSetChanged(walletEntity);
    }

    @Override
    public void showWalletInfo(Wallet wallet) {
        tvBackup.setVisibility(wallet.isBackedUp() ? View.GONE : View.VISIBLE);
        int resId = RUtils.drawable(wallet.getAvatar());
        if (resId < 0) {
            resId = R.drawable.avatar_15;
        }
        ivWalletAvatar.setImageResource(resId);
        tvWalletName.setText(wallet.getName());
        showFreeBalance(wallet.getFreeBalance());
        showLockBalance(wallet.getLockBalance());
    }

    @Override
    public void showContent(boolean isEmpty) {
        rlWalletDetail.setVisibility(!isEmpty ? View.VISIBLE : View.GONE);
        stbBar.setVisibility(!isEmpty ? View.VISIBLE : View.GONE);
        vpContent.setVisibility(!isEmpty ? View.VISIBLE : View.GONE);
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        scCreateWallet.setVisibility(View.GONE);
        scImportWallet.setVisibility(View.GONE);
        AppBarLayout.LayoutParams layoutParams = ((AppBarLayout.LayoutParams) llAssetsTitle.getLayoutParams());
        layoutParams.setScrollFlags(isEmpty ? 0 : AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        mWalletAdapter.notifyDataSetChanged();
    }

    @Override
    public void showCurrentItem(int index) {
        vpContent.setCurrentItem(index, true);
    }

    @Override
    public void setArgument(Wallet entity) {
        List<BaseFragment> fragments = ((TabAdapter) vpContent.getAdapter()).getFragments();
        for (BaseFragment fragment : fragments) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.Extra.EXTRA_WALLET, entity);
            fragment.setArguments(bundle);
        }
    }

    @Override
    public void finishRefresh() {
        layoutRefresh.finishRefresh();
    }

    private SpannableString getRestrictedAmount(String text) {
        SpannableString spannableString = new SpannableString(text);
        CustomImageSpan imageSpan = new CustomImageSpan(getActivity(), R.drawable.icon_restricted_amount);
        int index = TextUtils.indexOf(text, "(");
        if (index != -1) {
            spannableString.setSpan(imageSpan, index + 1, index + 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetWorkStateChangedEvent(Event.NetWorkStateChangedEvent event) {
        ((TabView) stbBar.getTabAt(1)).setTitle((NetworkUtil.getNetWorkType(getContext()) != NetworkType.NETWORK_NO) ? string(R.string.action_send_transation) : string(R.string.wallet_send_offline_signature));
        mPresenter.fetchWalletList();
    }

    class TabView extends LinearLayout {

        private ImageView mIconIv;
        private TextView mTitleTv;

        public TabView(Context context, int position) {
            super(context);
            LayoutInflater.from(context).inflate(R.layout.layout_app_tab_item1, this);

            setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

            mIconIv = findViewById(R.id.iv_icon);
            mTitleTv = findViewById(R.id.tv_title);

            mIconIv.setImageResource(getCollapsIcons().get(position));
            mTitleTv.setText(getTitles().get(position));
            mTitleTv.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.color_app_tab_text2));
        }

        public void setTitle(String text) {
            mTitleTv.setText(text);
        }
    }
}
