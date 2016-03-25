package com.ebusbar.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.ebusbar.dao.BalancePayDao;
import com.ebusbar.utils.JsonUtil;
import com.ebusbar.utils.NetParam;
import com.jellycai.service.ResponseResultHandler;

/**
 * 余额支付
 * Created by Jelly on 2016/3/23.
 */
public class BalancePayDaoImpl extends BaseImpl{

    /**
     * 操作数据
     */
    public BalancePayDao balancePayDao;

    public BalancePayDaoImpl(Context context, Handler handler, int msg) {
        super(context, handler, msg);
        execmode = "evc.order.pay";
    }

    public BalancePayDaoImpl(Context context) {
        super(context);
    }

    /**
     * 获得数据
     */
    public void getBalancePayDao(String Token,String OrderNo,String PayPassword,String custid){
        if(NetParam.isEmpty(Token,OrderNo,custid)){
            return;
        }
        conditionMap.clear();
        timestamp = NetParam.getTime();
        conditionMap.put("Token",Token);
        conditionMap.put("OrderNo",OrderNo);
        conditionMap.put("Type","1");
        conditionMap.put("PayPassword",PayPassword);
        condition = NetParam.spliceCondition(conditionMap);
        param = NetParam.getParamMap(trancode, mode, timestamp, custid, sign_method, sign, execmode, fields, condition);
        service.doPost(path, param, new ResponseResultHandler() {
            @Override
            public void response(boolean b, String s) {
                Log.v("jsonpay",s.trim());
                if(NetParam.isSuccess(b,s)){
                    balancePayDao = JsonUtil.arrayFormJson(s, BalancePayDao[].class).get(0);
                }
                handler.sendEmptyMessage(msg);
            }

            @Override
            public void responseBitmap(boolean b, Bitmap bitmap) {

            }
        });
    }
}