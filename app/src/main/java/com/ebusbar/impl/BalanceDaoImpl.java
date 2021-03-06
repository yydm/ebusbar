package com.ebusbar.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.ebusbar.bean.Balance;
import com.ebusbar.utils.JsonUtil;
import com.ebusbar.param.NetParam;
import com.jellycai.service.ResponseResultHandler;

/**
 * Created by Jelly on 2016/3/23.
 */
public class BalanceDaoImpl extends BaseDaoImpl {
    /**
     * 操作数据
     */
    public Balance balanceDao;

    public BalanceDaoImpl(Context context, Handler handler, int msg) {
        super(context, handler, msg);
        execmode = "crm.balanceamt.get";
    }

    public BalanceDaoImpl(Context context) {
        super(context);
    }

    /**
     * 获得数据
     */
    public void getBalanceDao(String Token,String custid){
        if(NetParam.isEmpty(Token,custid)){
            return;
        }
        conditionMap.clear();
        timestamp = NetParam.getTime();
        conditionMap.put("Token",Token);
        condition = NetParam.spliceCondition(conditionMap);
        param = NetParam.getParamMap(trancode, mode, timestamp, custid, sign_method, sign, execmode, fields, condition);
        service.doPost(path, param, new ResponseResultHandler() {
            @Override
            public void response(boolean b, String s) {
                Log.v("jsonBalance",s.trim());
                if(NetParam.isSuccess(b,s)){
                    balanceDao = JsonUtil.arrayFormJson(s,Balance[].class).get(0);
                }
                handler.sendEmptyMessage(msg);
            }

            @Override
            public void responseBitmap(boolean b, Bitmap bitmap) {

            }
        });
    }
}
