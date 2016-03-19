package com.ebusbar.pile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ebusbar.myview.MyDialog;
import com.ebusbar.utils.DefaultParam;
import com.ebusbar.utils.PopupWindowUtil;
import com.ebusbar.utils.ResourceUtil;
import com.ebusbar.utils.WindowUtil;

/**
 * 支付页面
 * Created by Jelly on 2016/3/11.
 */
public class PayActivity extends BaseActivity implements View.OnClickListener{
    /**
     * TAG
     */

    private Intent intent;
    /**
     * 支付种类
     */
    private TextView payType_text;
    /**
     * 支付价格
     */
    private TextView payPrice_text;
    /**
     * 支付按钮
     */
    private Button pay_btn;
    /**
     * 余额按钮
     */
    private ImageView tran_btn;
    /**
     * 支付宝按钮
     */
    private ImageView alipay_btn;
    /**
     * 微信支付按钮
     */
    private ImageView wchatpay_btn;
    /**
     * 输入支付密码
     */
    private PopupWindow payPw;
    /**
     * 弹出窗的操作事件
     */
    private PopupWindowUtil popupWindowUtil;
    /**
     * 资源操作工具
     */
    private ResourceUtil resourceUtil;
    /**
     * 窗体操作工具
     */
    private WindowUtil windowUtil;
    /**
     * 支付密码
     */
    private String payPassword = "";
    /**
     * 密码输入框
     */
    private ImageView[] input_ets;
    /**
     * 用户选择的支付方式
     */
    private ImageView selectPay;
    /**
     * 请求码,设置支付密码
     */
    public static int setPayPwdRequest = 0x001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.pay);
        init();
        loadObjectAttribute();
        setListener();
        setActivityView();
    }

    @Override
    public void init() {
        payPrice_text = (TextView) this.findViewById(R.id.payPrice_text);
        payType_text = (TextView) this.findViewById(R.id.payType_text);
        pay_btn = (Button) this.findViewById(R.id.pay_btn);
        tran_btn = (ImageView) this.findViewById(R.id.tran_btn);
        alipay_btn = (ImageView) this.findViewById(R.id.alipay_btn);
        wchatpay_btn = (ImageView) this.findViewById(R.id.wchatpay_btn);
    }

    @Override
    public void loadObjectAttribute() {
        intent = getIntent();
        popupWindowUtil = PopupWindowUtil.getInstance();
        windowUtil = WindowUtil.getInstance();
        resourceUtil = ResourceUtil.getInstance();
    }

    @Override
    public void setListener() {
        setPayRadioBtnListener();
    }

    @Override
    public void setActivityView() {
        payPrice_text.setText(resourceUtil.getResourceString(this, R.string.money_sign) + intent.getStringExtra("payPrice"));
        payType_text.setText(intent.getStringExtra("payType"));
        pay_btn.setText(resourceUtil.getResourceString(this, R.string.pay_btn_text) + intent.getStringExtra("payPrice") + "元");
    }

    /**
     * 设置支付选择的监听事件
     */
    public void setPayRadioBtnListener(){
        tran_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPay(((ImageView)v));
            }
        });
        alipay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPay(((ImageView)v));
            }
        });
        wchatpay_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPay(((ImageView)v));
            }
        });
    }

    /**
     * 设置支付
     */
    public void setPay(ImageView pay){
        if(selectPay != null){ //如果选择的支付方式不为空，改变为未选中
            selectPay.setImageResource(R.drawable.pay_noselect_rb);
        }
        pay.setImageResource(R.drawable.pay_select_rb);
        selectPay = pay;
    }

    /**
     * 支付
     * @param pay
     * @return
     */
    public View pay(View pay){
        if(selectPay == null){ //请选择支付方式
            Toast.makeText(this,R.string.nopayId_hint,Toast.LENGTH_SHORT).show();
            return pay;
        }else if(selectPay.getId() == R.id.alipay_btn){ //支付宝支付

        }else if(selectPay.getId() == R.id.wchatpay_btn){ //微信支付
            Toast.makeText(this,"对不起，暂不支持微信支付!",Toast.LENGTH_SHORT).show();
        }else if(selectPay.getId() == R.id.tran_btn){ //余额支付
            //模拟没有设置支付密码
            showSureFinishDialog("您还没有设置支付密码，是否前往设置?");
            //showPayPw(pay);
        }
        return pay;
    }



    /**
     * 弹出支付PopupWindow
     */
    public void showPayPw(View parent){
        View root = getLayoutInflater().inflate(R.layout.pay_keyboard,null);
        Button num0 = (Button) root.findViewById(R.id.num0);
        Button num1 = (Button) root.findViewById(R.id.num1);
        Button num2 = (Button) root.findViewById(R.id.num2);
        Button num3 = (Button) root.findViewById(R.id.num3);
        Button num4 = (Button) root.findViewById(R.id.num4);
        Button num5 = (Button) root.findViewById(R.id.num5);
        Button num6 = (Button) root.findViewById(R.id.num6);
        Button num7 = (Button) root.findViewById(R.id.num7);
        Button num8 = (Button) root.findViewById(R.id.num8);
        Button num9 = (Button) root.findViewById(R.id.num9);
        Button delete_btn = (Button) root.findViewById(R.id.delete_btn);
        Button dismiss_btn = (Button) root.findViewById(R.id.dismiss_btn);
        num0.setOnClickListener(this);
        num1.setOnClickListener(this);
        num2.setOnClickListener(this);
        num3.setOnClickListener(this);
        num4.setOnClickListener(this);
        num5.setOnClickListener(this);
        num6.setOnClickListener(this);
        num7.setOnClickListener(this);
        num8.setOnClickListener(this);
        num9.setOnClickListener(this);
        delete_btn.setOnClickListener(this);
        dismiss_btn.setOnClickListener(this);
        input_ets = new ImageView[DefaultParam.PAYPASSWOEDSUM];
        input_ets[0] = (ImageView) root.findViewById(R.id.input_et0);
        input_ets[1] = (ImageView) root.findViewById(R.id.input_et1);
        input_ets[2] = (ImageView) root.findViewById(R.id.input_et2);
        input_ets[3] = (ImageView) root.findViewById(R.id.input_et3);
        input_ets[4] = (ImageView) root.findViewById(R.id.input_et4);
        input_ets[5] = (ImageView) root.findViewById(R.id.input_et5);
        payPw = popupWindowUtil.getPopopWindow(this,root,windowUtil.getScreenWidth(this),windowUtil.getScreenHeight(this));
        payPw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                payPassword = "";
            }
        });
        payPw.setAnimationStyle(R.style.paypw_anim);
        payPw.showAtLocation(parent, Gravity.BOTTOM,0,0);
    }

    public void showSureFinishDialog(String hint){
        MyDialog.Builder builder = new MyDialog.Builder(this).setMessage(hint);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SetPayPwdActivity.startAppActivity(PayActivity.this,setPayPwdRequest);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        MyDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        switch (v.getId()){
            case R.id.dismiss_btn: //取消按钮
                payPw.dismiss();
                break;
            case R.id.delete_btn: //删除一位支付密码的方法
                if(payPassword.length() == 0){ //已经没有支付密码了，不能再减少了
                    return;
                }
                payPassword = payPassword.substring(0,payPassword.length()-1);
                reInputEt();
                break;
            default:
                if(payPassword.length() == 6){ //如果支付密码已经有6位，不能继续输入
                    return;
                }
                payPassword += btn.getText();
                reInputEt();
                if(payPassword.length() == 6){ //如果是最后一位输入6位，直接支付
                    Log.v(TAG, "支付");
                }
        }
    }

    /**
     * 更改密码框状态
     */
    public void reInputEt(){
        int index = payPassword.length();
        Log.v(TAG,index+"");
        Log.v(TAG,payPassword);
        for(int i=0;i<index;i++){
            input_ets[i].setImageResource(R.drawable.paypassword_input);
        }
        for(int i=index;i<DefaultParam.PAYPASSWOEDSUM;i++){
            input_ets[i].setImageResource(R.drawable.paypassword_noinput);
        }
    }

    /**
     * 返回结果
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != setPayPwdRequest || resultCode != SetPayPwdActivity.setPayPwdSuccess){
            return;
        }
        showPayPw(tran_btn);
    }

    /**
     * 开启支付界面
     */
    public static void startPayActivity(Context context,String payType,String payPrice){
        Intent intent = new Intent(context,PayActivity.class);
        intent.putExtra("payType",payType);
        intent.putExtra("payPrice", payPrice);
        context.startActivity(intent);
    }

    @Override
    public String getTAG() {
        return TAG;
    }
}