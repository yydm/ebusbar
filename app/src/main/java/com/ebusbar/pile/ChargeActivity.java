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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ebusbar.impl.FinishChargeDaoImpl;
import com.ebusbar.impl.QRChargeDaoImpl;
import com.ebusbar.myview.MyDialog;
import com.ebusbar.utils.PopupWindowUtil;
import com.ebusbar.utils.WindowUtil;


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
     * QRChargeDaoImpl
     */
    private QRChargeDaoImpl qrChargeDao;
    /**
     * 二维码充电消息
     */
    private int msgQRCharge = 0x001;
    /**
     * FinishChargeDaoImpl
     */
    private FinishChargeDaoImpl finishChargeDao;
    /**
     * 完成充电的消息
     */
    private int msgFinishCharge = 0x002;
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
    private int payResquest = 0x001;

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
        qrChargeDao = new QRChargeDaoImpl(this,handler,msgQRCharge);
        finishChargeDao = new FinishChargeDaoImpl(this,handler,msgFinishCharge);
        popupWindowUtil = PopupWindowUtil.getInstance();
        windowUtil = WindowUtil.getInstance();
    }

    @Override
    public void setListener() {
    }

    @Override
    public void setActivityView() {
        position_text.setText(intent.getStringExtra("position"));
        EPid_text.setText(intent.getIntExtra("EPId", 0) + "");
    }

    /**
     * 充电按钮的点击事件
     * @param view
     * @return
     */
    public View charge(View view){
        if (TextUtils.equals(chargeState, NOCHARGE)) { //如果电桩还没有还是充电，点击开始充电
            qrChargeDao.getNetQRChargeDao(intent.getIntExtra("EPId", 0) + "");
        } else if (TextUtils.equals(chargeState, CHARGEING)) { //充电桩正在充电，结束充电
//                    showSureFinishPw(v, "电桩正在充电,确定结束本轮充电吗？");
            showSureFinishDialog("电桩正在充电,确定结束本轮充电吗？");
        } else if (TextUtils.equals(chargeState, FINISHCHARGE)) { //已经完成充电等待支付
            PayActivity.startPayActivity(ChargeActivity.this, finishChargeDao.finishChargeDao.getPayType(), finishChargeDao.finishChargeDao.getPayPrice());
        }
        return view;
    }

    public void showSureFinishDialog(String hint){
        MyDialog.Builder builder = new MyDialog.Builder(this).setMessage(hint);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishChargeDao.getNetFinishChargeDao(intent.getIntExtra("EPId", 0) + "");
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
            if(msg.what == msgQRCharge){
                if(qrChargeDao.qrChargeDao == null || !TextUtils.equals(qrChargeDao.qrChargeDao.getMessage(),"1")){
                    Log.v(TAG,"充电桩启动充电错误");
                    return;
                }
                if(TextUtils.equals(chargeState,NOCHARGE)) {
                    chargeState = CHARGEING;
                    charge_btn.setImageResource(R.drawable.click_finish);
                }
            }else if(msg.what == msgFinishCharge){
                if(finishChargeDao.finishChargeDao == null || !TextUtils.equals(finishChargeDao.finishChargeDao.getMessage(),"1")){
                    return;
                }
                sureDialog.dismiss();
                chargeState = FINISHCHARGE;
                charge_btn.setImageResource(R.drawable.click_pay);
                //充电完成后跳到支付界面
                PayActivity.startPayActivity(ChargeActivity.this,finishChargeDao.finishChargeDao.getPayType(),finishChargeDao.finishChargeDao.getPayPrice());
            }
        }
    };


    /**
     * 返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("request",requestCode+"");
        Log.v("result",resultCode+"");
    }

    /**
     * 开启充电界面
     * @param context
     * @param position 充电点
     * @param EPId 电桩号
     */
    public static void startAppActivity(Context context,String position,int EPId){
        Intent intent = new Intent(context,ChargeActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("EPId",EPId);
        context.startActivity(intent);
    }

    @Override
    public String getTAG() {
        return TAG;
    }
}