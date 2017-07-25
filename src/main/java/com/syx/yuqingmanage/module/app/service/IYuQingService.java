package com.syx.yuqingmanage.module.app.service;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Msater Zg on 2017/7/13.
 */
public interface IYuQingService {
    public JSONObject judgerAppUser(String loginName, String password, long timestamp);

    public JSONObject checkToken(String token);

    // 带cookie，根据cookie获取用户名
    public JSONObject searchMenus(String loginName);

    // 需要先判断
    public JSONObject searchFocus(int tag_id, int limit, String date, String loginName);

    public JSONObject searchTagInfo(String filters, int limit, String data, String loginName);

    public JSONObject getInfodetail(String id, String loginName);

    public JSONObject searchFavor(int limit, String date, String loginName);

    // 根据id判断是否移除所有的数据
    public JSONObject removeFavor(String id, String loginName);

    public JSONObject addFavor(String id, String loginName);

    public JSONObject checkFavor(int id, String loginName);

    public JSONObject updatePwd(String oldPwd, String newPwd, String loginName);

    public JSONObject updateConfig(String type, String tag_id, String name, String push, String loginName);

    public JSONObject getConfig(String loginName);

}