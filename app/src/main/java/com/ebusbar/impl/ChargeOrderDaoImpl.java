package com.ebusbar.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.ebusbar.dao.ChargeOrderDao;
import com.ebusbar.utils.JsonUtil;
import com.ebusbar.utils.NetParam;
import com.jellycai.service.ResponseResultHandler;

/**
 * Created by Jelly on 2016/3/22.
 */
public class ChargeOrderDaoImpl extends BaseImpl{
    /**
     * 操作数据
     */
    public ChargeOrderDao chargeOrderDao;

    public ChargeOrderDaoImpl(Context context, Handler handler, int msg) {
        super(context, handler, msg);
        execmode = "evc.order.set";
    }

    public ChargeOrderDaoImpl(Context context) {
        super(context);
    }

    /**
     * 获取数据
     */
    public void getChargeOrderDao(String FacilityID,String Token,String custid){
        if(TextUtils.isEmpty(FacilityID) || TextUtils.isEmpty(Token)||TextUtils.isEmpty(custid)){
            return;
        }
        conditionMap.clear();
        timestamp = NetParam.getTime();
        conditionMap.put("Token",Token);
        conditionMap.put("FacilityID",FacilityID);
        conditionMap.put("OrderType","2");
        condition = NetParam.spliceCondition(conditionMap);
        param = NetParam.getParamMap(trancode,mode,timestamp,custid,sign_method,sign,execmode,fields,condition);
        service.doPost(path, param, new ResponseResultHandler() {
            @Override
            public void response(boolean b, String s) {
                Log.v("json", s.trim());
                if(b || TextUtils.isEmpty(s)) return;
                chargeOrderDao = JsonUtil.arrayFormJson(s, ChargeOrderDao[].class).get(0);
                handler.sendEmptyMessage(msg);
            }

            @Override
            public void responseBitmap(boolean b, Bitmap bitmap) {

            }
        });
    }

}