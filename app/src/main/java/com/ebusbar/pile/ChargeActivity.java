package com.ebusbar.pile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ebusbar.dao.LoginDao;
import com.ebusbar.dao.PileInfoDao;
import com.ebusbar.impl.ChargeOrderDaoImpl;
import com.ebusbar.impl.FinishChargeDaoImpl;
import com.ebusbar.impl.OrderInfoDaoImpl;
import com.ebusbar.impl.PileInfoDaoImpl;
import com.ebusbar.myview.MyDialog;
import com.ebusbar.utils.ActivityControl;
import com.ebusbar.utils.PopupWindowUtil;
import com.ebusbar.utils.WindowUtil;
import com.jellycai.service.ThreadManage;


/**
 * 充电界面
 * Created by Jelly on 2016/3/9.
 */
public class ChargeActivity extends BaseActivity{
    /**
     * TAG
     */
    public String TAG="ChargeActivity";
    /**
     * 充电点
     */
    private TextView position_text;
    /**
     * 电桩ID
     */
    private TextView EPid_text;
    /**
     * 充电时间
     */
    private TextView charge_time_text;
    /**
     * 充电度数
     */
    private TextView charge_degress_text;
    /**
     * 充电花费
     */
    private TextView charge_money_text;
    /**
     * 开始充电按钮
     */
    private ImageView charge_btn;
    /**
     * 从上一个界面传递下来的数据
     */
    private Intent intent;
    /**
     * FinishChargeDaoImpl
     */
    private FinishChargeDaoImpl finishChargeDao;
    /**
     * 完成充电的消息
     */
    private final int msgFinishCharge = 0x002;
    /**
     * 准备充电，点击开始充电
     */
    public static final String NOCHARGE = "开始充电";
    /**
     * 正在充电，点击结束充电
     */
    public static final String CHARGEING = "正在充电";
    /**
     * 完成充电，点击支付
     */
    public static final String FINISHCHARGE = "完成充电";
    /**
     * 完成支付，点击无效，显示状态
     */
    public static final String FINISHPAY = "完成支付";
    /**
     * 充电状态
     */
    private String chargeState = NOCHARGE;
    /**
     * 确定结束的弹出框
     */
    private MyDialog sureDialog;
    /**
     * 弹出窗操作工具
     */
    private PopupWindowUtil popupWindowUtil;
    /**
     * 窗体操作工具
     */
    private WindowUtil windowUtil;
    /**
     * 请求码，支付
     */
    private final int payResquest = 0x001;
    /**
     * PileInfoDaoImpl
     */
    private PileInfoDaoImpl pileInfoDao;
    /**
     * 二维码获取电桩详情消息
     */
    private final int msgPileInfo = 0x003;
    /**
     * ChargeOrderDaoImpl
     */
    private ChargeOrderDaoImpl chargeOrderDao;
    /**
     * 充电消息
     */
    private final int msgCharge = 0x004;
    /**
     * 订单号
     */
    private String OrderNo;
    /**
     * 电桩号
     */
    private String FacilityID;
    /**
     * Application
     */
    private MyApplication application;
    /**
     * 进度条
     */
    private PopupWindow loading;
    /**
     * 通知进度条消失
     */
    private final int msgLoading = 0x005;
    /**
     * OrderInfoDaoImpl
     */
    private OrderInfoDaoImpl orderInfoDao;
    /**
     * 获取详情消息
     */
    private final int msgInfo = 0x006;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.charge);
        init();
        loadObjectAttribute();
        setListener();
        setActivityView();
    }

    @Override
    public void init() {
        position_text = (TextView) this.findViewById(R.id.position_text);
        EPid_text = (TextView) this.findViewById(R.id.EPid_text);
        charge_time_text = (TextView) this.findViewById(R.id.charge_time_text);
        charge_degress_text = (TextView) this.findViewById(R.id.charge_degress_text);
        charge_money_text = (TextView) this.findViewById(R.id.charge_money_text);
        charge_btn = (ImageView) this.findViewById(R.id.charge_btn);
    }

    @Override
    public void loadObjectAttribute() {
        intent = getIntent();
        finishChargeDao = new FinishChargeDaoImpl(this,handler,msgFinishCharge);
        chargeOrderDao = new ChargeOrderDaoImpl(this,handler,msgCharge);
        pileInfoDao = new PileInfoDaoImpl(this,handler,msgPileInfo);
        orderInfoDao = new OrderInfoDaoImpl(this,handler,msgInfo);
        popupWindowUtil = PopupWindowUtil.getInstance();
        windowUtil = WindowUtil.getInstance();
        application = (MyApplication) getApplication();
        OrderNo = intent.getStringExtra("OrderNo");
    }

    @Override
    public void setListener() {
    }

    @Override
    public void setActivityView() {
        if(!TextUtils.isEmpty(intent.getStringExtra("QRId"))){ //读取二维码数据
            pileInfoDao.getPileInfoDao(intent.getStringExtra("QRId"));
            return;
        }
        position_text.setText(intent.getStringExtra("OrgName"));
        EPid_text.setText(intent.getStringExtra("FacilityID"));
        if(TextUtils.equals(intent.getStringExtra("OrderStatus"), "2")){
            chargeState = CHARGEING;
            charge_btn.setImageResource(R.drawable.click_finish);
        }else if(TextUtils.equals(intent.getStringExtra("OrderStatus"),"4")){
            chargeState = FINISHCHARGE;
            charge_btn.setImageResource(R.drawable.click_pay);
        }
    }

    /**
     * 充电按钮的点击事件
     * @param view
     * @return
     */
    public View charge(View view){
        if (TextUtils.equals(chargeState, NOCHARGE)) { //如果电桩还没有还是充电，点击开始充电
            if(TextUtils.isEmpty(FacilityID)){
                return view;
            }
            LoginDao.CrmLoginEntity entity = application.getLoginDao().getCrm_login();
            chargeOrderDao.getChargeOrderDao(FacilityID,entity.getToken(),entity.getCustID());
        } else if (TextUtils.equals(chargeState, CHARGEING)) { //充电桩正在充电，结束充电
            showSureFinishDialog("电桩正在充电,确定结束本轮充电吗？");
        } else if (TextUtils.equals(chargeState, FINISHCHARGE)) { //已经完成充电等待支付
            PayActivity.startPayActivity(ChargeActivity.this,intent.getStringExtra("OrderNo"));
        }
        return view;
    }

    public void showSureFinishDialog(String hint){
        MyDialog.Builder builder = new MyDialog.Builder(this).setMessage(hint);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginDao.CrmLoginEntity data = application.getLoginDao().getCrm_login();
                if(!TextUtils.isEmpty(OrderNo)){
                    finishChargeDao.getFinishChargeDao(data.getToken(),OrderNo,data.getCustID());
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        sureDialog = builder.create();
        sureDialog.show();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case msgCharge: //点击充电
                    if(chargeOrderDao.chargeOrderDao == null || TextUtils.equals(chargeOrderDao.chargeOrderDao.getEvc_order_set().getIsSuccess(), "N")){
                        return;
                    }
                    if(TextUtils.equals(chargeOrderDao.chargeOrderDao.getEvc_order_set().getOrderStatus(),"2")) {
                        chargeState = CHARGEING;
                        charge_btn.setImageResource(R.drawable.click_finish);
                        OrderNo = chargeOrderDao.chargeOrderDao.getEvc_order_set().getOrderNo();
                    }
                    break;
                case msgFinishCharge: //完成充电
                    if(finishChargeDao.finishChargeDao == null || TextUtils.equals(finishChargeDao.finishChargeDao.getEvc_order_change().getIsSuccess(),"N")){
                        Toast.makeText(ChargeActivity.this, "充电桩充电结束错误", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sureDialog.dismiss();
                    ThreadManage.start(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(15000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            handler.sendEmptyMessage(msgLoading);
                        }
                    });
//                    chargeState = FINISHCHARGE;
//                    charge_btn.setImageResource(R.drawable.click_pay);
                    //充电完成后跳到支付界面
//                    PayActivity.startPayActivity(ChargeActivity.this,finishChargeDao.finishChargeDao.getEvc_order_change().getOrderNo());
                    break;
                case msgPileInfo: //扫码二维码进入充电界面
                    if(pileInfoDao.pileInfoDao == null || TextUtils.equals(pileInfoDao.pileInfoDao.getEvc_facility_get().getIsSuccess(), "N")){
                        Toast.makeText(ChargeActivity.this,"二维码编号无法识别,请重新输入或者扫码",Toast.LENGTH_SHORT).show();
                        ActivityControl.finishAct(ChargeActivity.this);
                        return;
                    }
                    PileInfoDao.EvcFacilityGetEntity entity = pileInfoDao.pileInfoDao.getEvc_facility_get();
                    position_text.setText(entity.getOrgName());
                    EPid_text.setText(entity.getFacilityID());
                    FacilityID = entity.getFacilityID();
                    break;
                case msgLoading:
                    LoginDao.CrmLoginEntity loginEntity = application.getLoginDao().getCrm_login();
                    orderInfoDao.getOrderInfoDaoImpl(loginEntity.getToken(),finishChargeDao.finishChargeDao.getEvc_order_change().getOrderNo(),loginEntity.getCustID());
                    break;
                case msgInfo:
                    if(orderInfoDao.orderInfoDao == null || !TextUtils.equals(orderInfoDao.orderInfoDao.getEvc_order_get().getOrderStatus(),"4")){
                        return;
                    }
                    loading.dismiss();
                    chargeState = FINISHCHARGE;
                    charge_btn.setImageResource(R.drawable.click_pay);
//                    充电完成后跳到支付界面
                    PayActivity.startPayActivity(ChargeActivity.this,finishChargeDao.finishChargeDao.getEvc_order_change().getOrderNo());
                    break;
            }
        }
    };


    /**
     * 开始进度条
     */
    public void startLoading(){
        PopupWindowUtil popupWindowUtil = PopupWindowUtil.getInstance();
        WindowUtil windowUtil = WindowUtil.getInstance();
        View pw_layout = getLayoutInflater().inflate(R.layout.loading, null);
        TextView text = (TextView) this.findViewById(R.id.text);
        text.setText("系统正在处理中...");
        loading = popupWindowUtil.getPopopWindow(this, pw_layout, windowUtil.getScreenWidth(this), windowUtil.getScreenHeight(this));
        loading.showAtLocation(charge_btn, Gravity.CENTER, 0, 0);
    }

    /**
     * 返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("request", requestCode + "");
        Log.v("result", resultCode + "");
    }

    /**
     * 开启充电界面
     * @param context
     * @param OrgName 充电点
     * @param FacilityID 电桩号
     */
    public static void startAppActivity(Context context,String OrgName,String FacilityID,String OrderStatus,String OrderNo){
        Intent intent = new Intent(context,ChargeActivity.class);
        intent.putExtra("OrgName",OrgName);
        intent.putExtra("FacilityID", FacilityID);
        intent.putExtra("OrderStatus",OrderStatus);
        intent.putExtra("OrderNo",OrderNo);
        context.startActivity(intent);
    }

    /**
     * 开启充电界面
     * @param context
     */
    public static void startAppActivity(Context context,String QRId){
        Intent intent = new Intent(context,ChargeActivity.class);
        intent.putExtra("QRId", QRId);
        context.startActivity(intent);
    }

    @Override
    public String getTAG() {
        return TAG;
    }
}
