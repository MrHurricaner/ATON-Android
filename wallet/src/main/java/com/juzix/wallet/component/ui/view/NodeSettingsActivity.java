package com.juzix.wallet.component.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.juzhen.framework.util.AndroidUtil;
import com.juzix.wallet.BuildConfig;
import com.juzix.wallet.R;
import com.juzix.wallet.component.adapter.NodeListAdapter;
import com.juzix.wallet.component.ui.base.MVPBaseActivity;
import com.juzix.wallet.component.ui.contract.NodeSettingsContract;
import com.juzix.wallet.component.ui.presenter.NodeSettingsPresenter;
import com.juzix.wallet.component.widget.CommonTitleBar;
import com.juzix.wallet.component.widget.NodeListDecoration;
import com.juzix.wallet.component.widget.WrapContentLinearLayoutManager;
import com.juzix.wallet.entity.Node;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NodeSettingsActivity extends MVPBaseActivity<NodeSettingsPresenter> implements NodeSettingsContract.View {

    @BindView(R.id.list_nodes)
    RecyclerView listNodes;
    @BindView(R.id.tv_add_node)
    TextView tvAddNode;
    @BindView(R.id.ctb)
    CommonTitleBar ctb;

    private Unbinder unbinder;
    private NodeListAdapter nodeListAdapter;

    @Override
    protected NodeSettingsPresenter createPresenter() {
        return new NodeSettingsPresenter(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_settings);
        unbinder = ButterKnife.bind(this);
        initView();
        mPresenter.fetchNodes();
    }

    @Override
    protected boolean immersiveBarViewEnabled() {
        return true;
    }

    @Override
    public void showTitleView(boolean isEdit) {
        hideSoftInput();
        ctb.setRightText(string(isEdit ? R.string.save : R.string.edit));
        ctb.setRightTextVisibility(View.GONE);
        tvAddNode.setVisibility(isEdit ? View.VISIBLE : View.GONE);
        nodeListAdapter.setEditable(isEdit);
    }

    @Override
    public void updateNodeList(List<Node> nodeEntityList) {
        nodeListAdapter.updateNodeList(nodeEntityList);
    }

    @Override
    public void notifyDataChanged(List<Node> nodeEntityList) {
        nodeListAdapter.notifyDataChanged(nodeEntityList);
    }

    @Override
    public String getNodeAddress(long id) {
        return nodeListAdapter.getNodeAddress(id);
    }

    @Override
    public List<Node> getNodeList() {
        return nodeListAdapter.getNodeList();
    }

    @Override
    public void removeNodeList(List<Node> nodeEntityList) {
        nodeListAdapter.removeNodeList(nodeEntityList);
    }

    @Override
    public void setChecked(int position) {
        nodeListAdapter.setChecked(position);
    }

    private void initView() {

//        ctb.setRightTextVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
        ctb.setRightTextVisibility(View.GONE);

        int padding = AndroidUtil.dip2px(this, 16);
        nodeListAdapter = new NodeListAdapter(this, null);
        WrapContentLinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(this);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        listNodes.setLayoutManager(layoutManager);
        listNodes.addItemDecoration(new NodeListDecoration(this, padding, 0, padding, 0));
        listNodes.setAdapter(nodeListAdapter);

        nodeListAdapter.setOnItemRemovedListener(nodeEntity -> mPresenter.delete(nodeEntity));

        nodeListAdapter.setOnItemCheckedListener((nodeEntity, isChecked) -> mPresenter.updateNode(nodeEntity, isChecked));

        ctb.setRightTextClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.edit();
            }
        });

        ctb.setLeftDrawableClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPresenter.isEdit()) {
                    mPresenter.cancel();
                } else {
                    finish();
                }
            }
        });

        showTitleView(false);

    }

    @OnClick({R.id.tv_add_node})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_add_node:
                nodeListAdapter.addNode();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, NodeSettingsActivity.class);
        context.startActivity(intent);
    }
}
