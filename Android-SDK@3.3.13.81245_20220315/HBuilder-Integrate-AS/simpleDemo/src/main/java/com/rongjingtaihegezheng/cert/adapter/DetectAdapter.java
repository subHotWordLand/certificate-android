package com.rongjingtaihegezheng.cert.adapter;

import android.widget.ImageView;

import com.aiwinn.base.log.LogUtils;
import com.rongjingtaihegezheng.cert.R;
import com.rongjingtaihegezheng.cert.bean.DetectFaceBean;
import com.rongjingtaihegezheng.cert.DetectPresenterImpl;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseItemDraggableAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * com.aiwinn.faceattendance.adapter
 * SDK_ATT
 * 2018/08/25
 * Created by LeoLiu on User
 */

public class DetectAdapter extends BaseItemDraggableAdapter<DetectFaceBean, BaseViewHolder> {

    public DetectAdapter(List<DetectFaceBean> data) {
        super(R.layout.item_detect, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DetectFaceBean item) {
        if (item != null) {
//            LogUtils.d(DetectPresenterImpl.HEAD,"item image path is "+item.getUserBean().localImagePath);
            Glide.with(helper.getConvertView()).load(item.getUserBean().localImagePath).into((ImageView) helper.getView(R.id.img));
            helper.setText(R.id.name,item.getUserBean().name == null ? "default":item.getUserBean().name);
            helper.setText(R.id.time,item.getTime());
        }else {
            LogUtils.w(DetectPresenterImpl.HEAD,"item is null");
        }
    }

}
