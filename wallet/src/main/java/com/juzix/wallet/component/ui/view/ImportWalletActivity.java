package com.juzix.wallet.component.ui.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.juzhen.framework.util.AndroidUtil;
import com.juzix.wallet.R;
import com.juzix.wallet.app.Constants;
import com.juzix.wallet.component.ui.base.BaseActivity;
import com.juzix.wallet.component.ui.base.BaseFragment;
import com.juzix.wallet.component.ui.dialog.BaseDialogFragment;
import com.juzix.wallet.component.ui.dialog.CommonGuideDialogFragment;
import com.juzix.wallet.component.widget.CommonTitleBar;
import com.juzix.wallet.component.widget.ViewPagerSlide;
import com.juzix.wallet.component.widget.table.PagerItem;
import com.juzix.wallet.component.widget.table.PagerItemAdapter;
import com.juzix.wallet.component.widget.table.PagerItems;
import com.juzix.wallet.component.widget.table.SmartTabLayout;
import com.juzix.wallet.config.AppSettings;
import com.juzix.wallet.entity.GuideType;
import com.juzix.wallet.utils.GZipUtil;
import com.juzix.wallet.utils.JZWalletUtil;
import com.juzix.wallet.utils.ToastUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import io.reactivex.functions.Consumer;

public class ImportWalletActivity extends BaseActivity {

    public static final int REQ_QR_CODE = 101;

    private ViewPagerSlide mVpContent;

    private @TabIndex
    int mTabIndex = TabIndex.IMPORT_KEYSTORE;

    @IntDef({
            TabIndex.IMPORT_KEYSTORE,
            TabIndex.IMPORT_MNEMONIC,
            TabIndex.IMPORT_PRIVATEKEY,
            TabIndex.IMPORT_OBSERVED
    })
    @interface TabIndex {
        int IMPORT_KEYSTORE = 0;
        int IMPORT_MNEMONIC = 1;
        int IMPORT_PRIVATEKEY = 2;
        int IMPORT_OBSERVED = 3;
    }

    public static void actionStart(Context context) {
        context.startActivity(new Intent(context, ImportWalletActivity.class));
    }

    public static void actionStart(Context context, @TabIndex int tabIndex, String content) {
        Intent intent = new Intent(context, ImportWalletActivity.class);
        intent.putExtra(Constants.Extra.EXTRA_TAB_INDEX, tabIndex);
        intent.putExtra(Constants.Extra.EXTRA_SCAN_QRCODE_DATA, content);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_wallet);
        initView();
        initGuide();
    }

    private void initGuide() {

        if (!AppSettings.getInstance().getObservedWalletBoolean()) {
            CommonGuideDialogFragment.newInstance(GuideType.IMPORT_WALLET)
                    .setOnDissmissListener(new BaseDialogFragment.OnDissmissListener() {
                        @Override
                        public void onDismiss() {
                            AppSettings.getInstance().setObservedWalletBoolean(true);
                        }
                    })
                    .show(getSupportFragmentManager(), "showGuideDialogFragment");
        }

    }

    @Override
    protected boolean immersiveBarViewEnabled() {
        return true;
    }

    private void initView() {

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            mTabIndex = bundle.getInt(Constants.Extra.EXTRA_TAB_INDEX, TabIndex.IMPORT_KEYSTORE);
        }

        CommonTitleBar commonTitleBar = findViewById(R.id.commonTitleBar);
        commonTitleBar.setRightDrawableClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanQRCode();
            }
        });
        int indicatorThickness = AndroidUtil.dip2px(getContext(), 2.0f);
        SmartTabLayout stbBar = mRootView.findViewById(R.id.stb_bar);
        stbBar.setIndicatorThickness(indicatorThickness);
        stbBar.setIndicatorCornerRadius(indicatorThickness / 2);
        ArrayList<Class<? extends BaseFragment>> fragments = getFragments();


        stbBar.setCustomTabView(new SmartTabLayout.TabProvider() {
            @Override
            public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
                return getTableView(position, container);
            }
        });
        PagerItems pages = new PagerItems(getContext());
        int tabNum = fragments.size();
        for (int i = 0; i < tabNum; i++) {
            if (i == mTabIndex) {
                pages.add(PagerItem.of(getTitles().get(i), fragments.get(i), bundle == null ? new Bundle() : bundle));
            } else {
                pages.add(PagerItem.of(getTitles().get(i), fragments.get(i), new Bundle()));
            }
        }
        mVpContent = mRootView.findViewById(R.id.vp_content);
        mVpContent.setSlide(true);
        mVpContent.setOffscreenPageLimit(fragments.size());
        mVpContent.setAdapter(new PagerItemAdapter(getSupportFragmentManager(), pages));
        stbBar.setViewPager(mVpContent);
        setTableView(stbBar.getTabAt(mTabIndex), mTabIndex);
        mVpContent.setCurrentItem(mTabIndex);
    }

    private ArrayList<String> getTitles() {
        ArrayList<String> titleList = new ArrayList<>();
        titleList.add(getString(R.string.keystore));
        titleList.add(getString(R.string.mnemonicPhrase));
        titleList.add(getString(R.string.privateKey));
        titleList.add(getString(R.string.observed));
        return titleList;
    }

    private ArrayList<Class<? extends BaseFragment>> getFragments() {
        ArrayList<Class<? extends BaseFragment>> list = new ArrayList<>();
        list.add(ImportKeystoreFragment.class);
        list.add(ImportMnemonicPhraseFragment.class);
        list.add(ImportPrivateKeyFragment.class);
        list.add(ImportObservedFragment.class);
        return list;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == ImportWalletActivity.REQ_QR_CODE) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString(Constants.Extra.EXTRA_SCAN_QRCODE_DATA);
            String unzip = GZipUtil.unCompress(scanResult);
            if (TextUtils.isEmpty(unzip) && TextUtils.isEmpty(scanResult)) {
                ToastUtil.showLongToast(getContext(), R.string.unrecognized_content);
                return;
            }
            String newStr = TextUtils.isEmpty(unzip) ? scanResult : unzip;
            if (JZWalletUtil.isValidKeystore(newStr)) {
                mVpContent.setCurrentItem(0);
                ((PagerItemAdapter) mVpContent.getAdapter()).getPage(TabIndex.IMPORT_KEYSTORE).onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (JZWalletUtil.isValidMnemonic(newStr)) {
                mVpContent.setCurrentItem(1);
                ((PagerItemAdapter) mVpContent.getAdapter()).getPage(TabIndex.IMPORT_MNEMONIC).onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (JZWalletUtil.isValidPrivateKey(newStr)) {
                mVpContent.setCurrentItem(2);
                ((PagerItemAdapter) mVpContent.getAdapter()).getPage(TabIndex.IMPORT_PRIVATEKEY).onActivityResult(requestCode, resultCode, data);
                return;
            }
            //新增导入观察钱包的判断
            if (JZWalletUtil.isValidAddress(newStr)) {
                mVpContent.setCurrentItem(3);
                ((PagerItemAdapter) mVpContent.getAdapter()).getPage(TabIndex.IMPORT_OBSERVED).onActivityResult(requestCode, resultCode, data);
                return;
            }
            showLongToast(string(R.string.unrecognized));
        }
    }

    private void scanQRCode() {
        new RxPermissions(currentActivity())
                .request(Manifest.permission.CAMERA)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean success) throws Exception {
                        if (success) {
                            ScanQRCodeActivity.startActivityForResult(currentActivity(), REQ_QR_CODE);
                        }
                    }
                });
    }

    private View getTableView(int position, ViewGroup container) {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_app_tab_item1, container, false);
        setTableView(contentView, position);
        return contentView;
    }

    private void setTableView(View contentView, int position) {
        contentView.findViewById(R.id.iv_icon).setVisibility(View.GONE);
        TextView tvTitle = contentView.findViewById(R.id.tv_title);
        tvTitle.setText(getTitles().get(position));
        tvTitle.setTextColor(ContextCompat.getColorStateList(getContext(), R.color.color_app_tab_text2));
    }

}
