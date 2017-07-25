package com.syx.yuqingmanage.module.app.service.imp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alienlab.db.ExecResult;
import com.alienlab.response.JSONResponse;
import com.alienlab.utils.Md5Azdg;
import com.syx.yuqingmanage.module.app.service.IYuQingService;
import com.syx.yuqingmanage.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Msater Zg on 2017/7/13.
 */
@Service
public class YuQingAppService implements IYuQingService {
    @Autowired
    JSONResponse jsonResponse;

    @Override
    public JSONObject judgerAppUser(String loginName, String password, long timestamp) {
        String sqlGetUser = "SELECT * FROM app_user WHERE app_user_loginname = " + loginName;
        ExecResult execResult = jsonResponse.getSelectResult(sqlGetUser, null, "");
        JSONObject returnData = new JSONObject();
        if (execResult.getResult() == 1) {
            JSONObject jsonObject = ((JSONArray) execResult.getData()).getJSONObject(0);
            String userPwd = jsonObject.getString("app_user_pwd");
            String userOverDue = jsonObject.getString("app_user_overdue_time").substring(0, 10);
            if (Md5Azdg.md5s(password).equals(userPwd)) {
                String nowTime = DateTimeUtils.getNowTime("yyyy-MM-dd HH:mm:ss");
                if (userOverDue.compareTo(nowTime) > 0) {
                    returnData.put("success", false);
                    returnData.put("value", 11002);
                } else {
                    // 用户存在
                    returnData.put("success", true);
                    JSONObject jsonObjectUser = new JSONObject();
                    // 生成token
                    String token = Md5Azdg.md5s(loginName + ":" + password + ":" + nowTime);
                    jsonObjectUser.put("token", token);
                    returnData.put("value", jsonObjectUser);
                }
            } else {
                //不存在这个用户
                returnData.put("success", false);
                returnData.put("value", 11001);
            }
        } else {
            //不存在这个用户
            returnData.put("success", false);
            returnData.put("value", 11001);
        }
        return returnData;
    }

    @Override
    public JSONObject checkToken(String token) {
        String getUserByCheckToken = "SELECT * FROM app_user WHERE app_user_token = '" + token + "'";
        ExecResult execResult = jsonResponse.getSelectResult(getUserByCheckToken, null, "");
        JSONObject jsonObject = new JSONObject();
        if (execResult.getResult() == 1) {
            // 有数据
            JSONObject jsonObjectUser = ((JSONArray) execResult.getData()).getJSONObject(0);
            String overDueTime = jsonObjectUser.getString("app_user_overdue_time");
            String nowTime = DateTimeUtils.getNowTime("yyyy-MM-dd HH:mm:ss");
            if (overDueTime.compareTo(nowTime) > 0) {
                jsonObject.put("success", true);
                jsonObject.put("value", "");
            } else {
                jsonObject.put("success", false);
                jsonObject.put("value", 11002);
            }
        } else {
            // 没有数据，说明过期了
            jsonObject.put("success", false);
            jsonObject.put("value", 12001);
        }
        return jsonObject;
    }

    @Override
    public JSONObject searchMenus(String loginName) {
        String getSMenus = "SELECT a.id AS tag_id,a.app_program_name AS NAME,b.app_module_type AS TYPE  " +
                "FROM (SELECT a.*,b.app_module_id FROM app_user_program a  " +
                "LEFT JOIN app_user_program_module b ON a.id=b.app_program_id WHERE a.app_user_loginname = '" + loginName + "' ) a  " +
                "LEFT JOIN app_module b ON a.app_module_id = b.id GROUP BY a.id";
        ExecResult execResult = jsonResponse.getSelectResult(getSMenus, null, "");
        JSONObject jsonObjectReturn = new JSONObject();
        if (execResult.getResult() == 1) {
            // 有数据
            JSONArray jsonArray = (JSONArray) execResult.getData();
            JSONObject jsonObjectTag = new JSONObject();
            jsonObjectTag.put("tags", jsonArray);
            jsonObjectReturn.put("success", true);
            jsonObjectReturn.put("value", jsonObjectTag);
        } else {
            // 无数据
            jsonObjectReturn.put("success", false);
            jsonObjectReturn.put("value", 21001);
        }
        return jsonObjectReturn;
    }

    @Override
    public JSONObject searchFocus(int tag_id, int limit, String date, String loginName) {
        if ("".equals(date)) {

        } else {

        }
        // 根据id获取到焦点数据的
        String getFoucs = "SELECT b.* FROM (SELECT a.*,b.topic_context_id FROM (SELECT a.*,b.tag_id FROM  " +
                "(SELECT a.*,b.app_module_id FROM  app_user_program a  " +
                "LEFT JOIN app_user_program_module b  " +
                "ON a.id = b.app_program_id WHERE a.id = " + tag_id + ") a LEFT JOIN app_module_tag_dep b  " +
                "ON a.app_module_id = b.app_module_id) a , topic_context  b  " +
                "WHERE a.tag_id = b.topic_id) a LEFT JOIN sys_topic_context b " +
                "ON a.topic_context_id = b.id GROUP BY b.id ";
        ExecResult execResult = jsonResponse.getSelectResult(getFoucs, null, "");
        return null;
    }

    @Override
    public JSONObject searchTagInfo(String filters, int limit, String date, String loginName) {
        if ("".equals(date)) {

        } else {

        }
        String getSql = "SELECT b.* FROM (SELECT a.*,b.infor_id FROM (SELECT a.*,b.tag_id FROM  " +
                "(SELECT a.*,b.app_module_id FROM  app_user_program a  " +
                "LEFT JOIN app_user_program_module b  " +
                "ON a.id = b.app_program_id  WHERE a.id = '37') a LEFT JOIN app_module_tag_dep b  " +
                "ON a.app_module_id = b.app_module_id) a LEFT JOIN infor_tag b ON a.tag_id = b.tag_id) a,sys_infor b  " +
                "WHERE a.infor_id = b.id GROUP BY b.id ";
        return null;
    }

    @Override
    public JSONObject getInfodetail(String id, String loginName) {
        return null;
    }

    @Override
    public JSONObject searchFavor(int limit, String date, String loginName) {
        String getFavorInfo = "SELECT b.infor_type AS level_id,b.infor_title " +
                "AS title,b.infor_context AS content,b.infor_link  " +
                "AS source_url,b.infor_createtime AS pub_time,b.infor_source  " +
                "AS source_id, b.infor_site AS site FROM  app_user_favor a  " +
                "LEFT JOIN sys_infor b ON a.app_favor_infor_id = b.id " +
                "WHERE a.app_user_loginname = '" + loginName + "' ";

        return null;
    }

    @Override
    public JSONObject removeFavor(String id, String loginName) {
        String removeFavorSql = "";
        if ("".equals(id)) {
            removeFavorSql = "DELETE FROM app_user_favor WHERE app_user_loginname = '" + loginName + "'";
            ExecResult execResult = jsonResponse.getExecResult(removeFavorSql, null);
            JSONObject jsonObject = new JSONObject();
            if (execResult.getResult() == 1) {
                jsonObject.put("success", true);
                jsonObject.put("value", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("value", 33002);
            }
        } else {
            removeFavorSql = "DELETE FROM app_user_favor WHERE id = " + id;
            ExecResult execResult = jsonResponse.getExecResult(removeFavorSql, null);
            JSONObject jsonObject = new JSONObject();
            if (execResult.getResult() == 1) {
                jsonObject.put("success", true);
                jsonObject.put("value", true);
            } else {
                jsonObject.put("success", false);
                jsonObject.put("value", 33001);
            }
        }
        return null;
    }

    @Override
    public JSONObject addFavor(String id, String loginName) {
        String insertSql = "INSERT INTO  app_user_favor (app_user_loginname, app_favor_infor_id) VALUES ('" + loginName + "','" + id + "')";
        ExecResult execResult = jsonResponse.getSelectResult(insertSql, null, "");
        JSONObject jsonObject = new JSONObject();
        if (execResult.getResult() == 1) {
            jsonObject.put("success", true);
            jsonObject.put("value", true);
        } else {
            jsonObject.put("success", false);
            jsonObject.put("value", 33001);
        }
        return null;
    }

    @Override
    public JSONObject checkFavor(int id, String loginName) {
        String checkSql = "SELECT * FROM app_user_favor a WHERE a.app_user_loginname = '" + loginName + "' AND a.app_favor_infor_id = '" + id + "'";
        ExecResult execResult = jsonResponse.getSelectResult(checkSql, null, "");
        JSONObject jsonObject = new JSONObject();
        if (execResult.getResult() == 1) {
            jsonObject.put("success", true);
            jsonObject.put("value", true);
        } else {
            jsonObject.put("success", true);
            jsonObject.put("value", false);
        }
        return jsonObject;
    }

    @Override
    public JSONObject updatePwd(String oldPwd, String newPwd, String loginName) {
        String sqlPwd = "UPDATE app_user a SET a.app_user_pwd = '" + newPwd + "' " +
                "WHERE a.app_user_loginname = '" + loginName + "' AND a.app_user_pwd = '" + oldPwd + "'";
        JSONObject jsonObject = new JSONObject();
        ExecResult execResult = jsonResponse.getSelectResult(sqlPwd, null, "");
        if (execResult.getResult() == 1) {
            jsonObject.put("success", true);
            jsonObject.put("value", true);
        } else {
            jsonObject.put("success", false);
            jsonObject.put("value", 41001);
        }
        return jsonObject;
    }

    @Override
    public JSONObject updateConfig(String type, String tag_id, String name, String push, String loginName) {
        return null;
    }

    @Override
    public JSONObject getConfig(String loginName) {
        return null;
    }
}