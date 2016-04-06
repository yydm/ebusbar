package com.ebusbar.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ebusbar.adpater.NearbyListItemAdapter;
import com.ebusbar.dao.NearbyStationDao;
import com.ebusbar.fragments.UtilFragment;
import com.ebusbar.impl.NearbyStationDaoImpl;
import com.ebusbar.myview.SlideSwitch;
import com.ebusbar.pile.MainActivity;
import com.ebusbar.pile.MyApplication;
import com.ebusbar.pile.R;
import com.ebusbar.pile.SearchActivity;
import com.ebusbar.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 附近电桩
 * Created by Jelly on 2016/3/24.
 */
public class NearbyStationFragment extends UtilFragment {
    /**
     * TAG
     */
    public String TAG = "NearbyStationFragment";
    /**
     * 上下文
     */
    private Context context;
    /**
     * 根布局
     */
    private View root;
    /**
     * 充电点的列表
     */
    private ListView list;
    /**
     * 地图切换
     */
    private TextView map;
    /**
     * 会员头像
     */
    private ImageView member;
    /**
     * 搜索
     */
    private TextView search;
    /**
     * 筛选
     */
    private RelativeLayout screen;
    /**
     * 箭头
     */
    private ImageView arrow;
    /**
     * ActionBar
     */
    private RelativeLayout actionBar_layout;
    /**
     * PositionDaoImpl
     */
    private NearbyStationDaoImpl nearbyStationDao;
    /**
     * 获取电桩位置集合消息
     */
    private final int msgPositionList = 0x001;
    /**
     * Application
     */
    private MyApplication application;
    /**
     * Adapter
     */
    private NearbyListItemAdapter adapter;
    /**
     * 筛选弹出框
     */
    private PopupWindow screenPw;
    /**
     * 是否弹出
     */
    private boolean isShow = false;
    /**
     * 隐藏的充电点
     */
    private List<NearbyStationDao> dismissList = new ArrayList<>();
    /**
     * 筛选可用的是否打开
     */
    private boolean isUse = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        init(inflater, container);
        loadObjectAttribute();
        setListener();
        setFragView();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void init(LayoutInflater inflater,ViewGroup container) {
        root = inflater.inflate(R.layout.nearby,container,false);
        list = (ListView) root.findViewById(R.id.list);
        map = (TextView) root.findViewById(R.id.map);
        member = (ImageView) root.findViewById(R.id.member);
        search = (TextView) root.findViewById(R.id.search);
        screen = (RelativeLayout) root.findViewById(R.id.screen);
        actionBar_layout = (RelativeLayout) root.findViewById(R.id.actionBar_layout);
        arrow = (ImageView) root.findViewById(R.id.arrow);
    }

    @Override
    public void loadObjectAttribute() {
        context = getContext();
        application = (MyApplication) getActivity().getApplication();
        nearbyStationDao = new NearbyStationDaoImpl(context,handler,msgPositionList);
    }

    @Override
    public void setListener() {
        setMapListener();
        setOpenDrawerListener();
        setSearchListener();
        setScreenListener();
    }

    @Override
    public void setFragView() {
        if(application.getLocation() != null){
            String adCode = application.getLocation().getAdCode();
            adCode = adCode.substring(0,adCode.length()-2) + "00";
            LogUtil.v(TAG,adCode);
            nearbyStationDao.getDaos(adCode);
        }
    }


    /**
     * 当点击会员头像的时候,打开抽屉
     */
    public void setOpenDrawerListener(){
        member.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.drawerLayout.openDrawer(Gravity.LEFT); //打开左边抽屉
            }
        });
    }

    /**
     * 设置筛选按钮的监听器
     */
    public void setScreenListener(){
        screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(screenPw != null && isShow){
                    screenPw.dismiss();
                    arrow.setImageResource(R.drawable.down);
                    isShow = false;
                    return;
                }else if(screenPw != null && !isShow){
                    screenPw.showAsDropDown(v);
                    arrow.setImageResource(R.drawable.up);
                    isShow = true;
                    return;
                }
                View root = LayoutInflater.from(context).inflate(R.layout.nearby_screen_layout,null);
                final RelativeLayout use = (RelativeLayout) root.findViewById(R.id.use);
                RelativeLayout enough = (RelativeLayout) root.findViewById(R.id.enough);
                final SlideSwitch use_switch = (SlideSwitch) root.findViewById(R.id.use_switch);
                final SlideSwitch enough_switch = (SlideSwitch) root.findViewById(R.id.enough_switch);
                use_switch.setEnabled(false);
                enough_switch.setEnabled(false);
                if(isUse){
                    use_switch.changeSwitchStatus();
                }
                use.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        use_switch.changeSwitchStatus();
                    }
                });

                enough.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enough_switch.changeSwitchStatus();
                    }
                });

                use_switch.setOnSwitchChangedListener(new SlideSwitch.OnSwitchChangedListener() {
                    @Override
                    public void onSwitchChanged(SlideSwitch obj, int status) {
                        if (status == 1) {
                            isUse = true;
                            nearbyStationDao.daos = screenUse(nearbyStationDao.daos);
                            LogUtil.v(TAG,nearbyStationDao.daos.size()+"");
                            adapter = new NearbyListItemAdapter(context,nearbyStationDao.daos);
                            list.setAdapter(adapter);
                        } else {
                            isUse = false;
                            nearbyStationDao.daos.addAll(dismissList);
                            adapter = new NearbyListItemAdapter(context,nearbyStationDao.daos);
                            list.setAdapter(adapter);
                        }
                    }
                });



                ImageView bg = (ImageView) root.findViewById(R.id.bg);
                bg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (screenPw != null && screenPw.isShowing()) {
                            screenPw.dismiss();
                            isShow = false;
                        }
                    }
                });
                screenPw = popupWindowUtil.getPopupWindow(context, root, windowUtil.getScreenWidth(getActivity()), windowUtil.getScreenHeight(getActivity()) - actionBar_layout.getHeight() - screen.getHeight() - windowUtil.getStateBarHeight(context));
                screenPw.showAsDropDown(v);
                arrow.setImageResource(R.drawable.up);
                isShow = true;
            }
        });
    }


    public List<NearbyStationDao> screenUse(List<NearbyStationDao> list){
        dismissList.clear();
        List<NearbyStationDao> show = new ArrayList<>();
        for(int i=0;i<list.size();i++){
            if(TextUtils.equals("1", list.get(i).getEvc_stations_get().getIsAvailable())){
                show.add(list.get(i));
            }else{
                dismissList.add(list.get(i));
            }
        }
        return show;
    }

    /**
     * 设置切换Map的点击事件
     */
    public void setMapListener(){
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(NearbyStationFragment.this);
                ft.add(R.id.content, new AllStationFragment());
                ft.commit();
            }
        });
    }

    /**
     * 设置搜索监听
     */
    public void setSearchListener(){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.startAppActivity(context);
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case msgPositionList:
                    if(nearbyStationDao.daos == null){
                        return;
                    }
                    adapter = new NearbyListItemAdapter(context, nearbyStationDao.daos);
                    list.setAdapter(adapter);
                    break;
            }
        }
    };

    @Override
    public String getTAG() {
        return TAG;
    }
}