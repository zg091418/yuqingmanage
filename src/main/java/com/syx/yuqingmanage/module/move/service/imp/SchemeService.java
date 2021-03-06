package com.syx.yuqingmanage.module.move.service.imp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alienlab.db.ExecResult;
import com.alienlab.response.JSONResponse;
import com.syx.yuqingmanage.module.move.service.ISchemeService;
import com.syx.yuqingmanage.utils.SqlEasy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.util.*;

/**
 * Created by Msater Zg on 2017/2/9.
 */
@Service
public class SchemeService implements ISchemeService {
    @Autowired
    private JSONResponse jsonResponse;

    @Override
    public ExecResult insertScheme(String schemeData, String tagIds, String areaId, String baseTag) {
        String sql = SqlEasy.insertObject(schemeData, "sys_scheme");
        ExecResult execResult = jsonResponse.getExecInsertId(sql, null, "", "");
        int schemeId = Integer.parseInt(execResult.getMessage());
        List<String> sqlList = new ArrayList<>();
        String[] ids = tagIds.split(",");
        int idsLen = ids.length;
        for (int i = 0; i < idsLen; i++) {
            sqlList.add("INSERT INTO sys_scheme_tag_dep (scheme_id,tag_id) VALUES(" + schemeId + "," + ids[i] + ")");
        }
        String[] baseTagS = baseTag.split(",");
        int baseTagSLen = baseTagS.length;
        Set<String> set = new HashSet<>();
        for (int i = 0; i < baseTagSLen; i++) {
            set.add(baseTagS[i]);
        }
        for (String str : set) {
            sqlList.add("INSERT INTO sys_scheme_tag_base (scheme_id,tag_id) VALUES(" + schemeId + "," + str + ")");
        }
        sqlList.add("INSERT INTO sys_scheme_area_dep (area_id,scheme_id) VALUES(" + areaId + "," + schemeId + ")");
        execResult = jsonResponse.getExecResult(sqlList, "", "");
        return execResult;
    }

    @Override
    public JSONObject getAllScheme(String areaId) {
        String[] areaIds = areaId.split(",");
        int areaIdsLen = areaIds.length;
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < areaIdsLen; i++) {
            if (i == 0) {
                idList.add("" + areaIds[i]);
            } else {
                idList.add(" OR a.area_id = " + areaIds[i]);
            }
        }
        List<String> sqlList = new ArrayList<>();
        sqlList.add("SELECT a.* ,GROUP_CONCAT(a.name) AS tag_names,GROUP_CONCAT(a.tag_id) AS tag_ids ");
        sqlList.add("FROM (SELECT c.*,d.name,d.id AS tag_id,e.user_loginname,e.user_name ,f.plan_name FROM sys_scheme_area_dep a , sys_scheme_tag_dep b ,sys_scheme c,sys_tag d,sys_user e, sys_plan f ");
        sqlList.add("WHERE a.scheme_id = c.id AND b.scheme_id = c.id  AND b.tag_id = d.id AND c.scheme_creater=e.user_loginname AND c.scheme_plan_id = f.id  AND (a.area_id = " + StringUtils.join(idList, "") + ")) a " +
                "GROUP BY a.id");
        String sql = StringUtils.join(sqlList, "");
        ExecResult execResult = jsonResponse.getSelectResult(sql, null, "");
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = (JSONArray) execResult.getData();
        jsonObject.put("data", jsonArray);
        if (jsonArray == null) {
            jsonObject.put("total", 0);
        } else {
            jsonObject.put("total", jsonArray.size());
        }
        return jsonObject;
    }

    @Override
    public ExecResult deleteSchemeId(String schemeId) {
        String[] idS = schemeId.split(",");
        int idSLen = idS.length;
        List<String> deleteList = new ArrayList<>();
        for (int i = 0; i < idSLen; i++) {
            deleteList.add("DELETE FROM sys_scheme WHERE id = " + idS[i]);
            deleteList.add("DELETE FROM sys_scheme_area_dep WHERE scheme_id=" + idS[i]);
            deleteList.add("DELETE FROM sys_scheme_tag_dep WHERE scheme_id=" + idS[i]);
            deleteList.add("DELETE FROM sys_scheme_tag_base WHERE scheme_id=" + idS[i]);
            // 删除客户相关信息
            deleteList.add("DELETE FROM post_customer_area WHERE post_customer_id = DELETE  FROM sys_post_customer WHERE id = (SELECT b.id FROM (SELECT b.id FROM sys_scheme a, sys_post_customer b WHERE a.id = " + idS[i] + " " +
                    "AND a.id = b.customer_scheme)b) ");
            deleteList.add("DELETE FROM sys_customer_get WHERE post_customer_id = DELETE  FROM sys_post_customer WHERE id = (SELECT b.id FROM (SELECT b.id FROM sys_scheme a, sys_post_customer b WHERE a.id = " + idS[i] + " " +
                    "AND a.id = b.customer_scheme)b) ");
            deleteList.add("DELETE FROM sys_post_customer WHERE id = DELETE  FROM sys_post_customer WHERE id = (SELECT b.id FROM (SELECT b.id FROM sys_scheme a, sys_post_customer b WHERE a.id = " + idS[i] + " " +
                    "AND a.id = b.customer_scheme)b) ");
        }
        ExecResult execResult = jsonResponse.getExecResult(deleteList, "", "");
        return execResult;
    }

    @Override
    public ExecResult updateScheme(String schemeId, String tagId, String schemeData, String baseTag) {
        List<String> list = new ArrayList<>();
        String sql = SqlEasy.updateObject(schemeData, "sys_scheme", "id = " + schemeId);
        String[] tagIds = tagId.split(",");
        list.add(sql);
        list.add("DELETE FROM sys_scheme_tag_dep WHERE scheme_id = " + schemeId);
        list.add("DELETE FROM sys_scheme_tag_base WHERE scheme_id = " + schemeId);
        int tagIdsLen = tagIds.length;
        for (int i = 0; i < tagIdsLen; i++) {
            list.add("INSERT INTO sys_scheme_tag_dep (scheme_id,tag_id) VALUES(" + schemeId + "," + tagIds[i] + ")");
        }
        String[] baseTagS = baseTag.split(",");
        int baseTagSLen = baseTagS.length;
        Set<String> set = new HashSet<>();
        for (int i = 0; i < baseTagSLen; i++) {
            set.add(baseTagS[i]);
        }
        for (String str : set) {
            list.add("INSERT INTO sys_scheme_tag_base (scheme_id,tag_id) VALUES(" + schemeId + "," + str + ")");
        }
        ExecResult execResult = jsonResponse.getExecResult(list, "", "");
        return execResult;
    }

    @Override
    public JSONObject getAllSchemeChoose(String areaId, String tagId, String chooseSchemeData) {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObjectChoose = JSON.parseObject(chooseSchemeData);
        String[] areaIds = areaId.split(",");
        int areaIdsLen = areaIds.length;
        List<String> areaIdList = new ArrayList<>();
        for (int i = 0; i < areaIdsLen; i++) {
            if (i == 0) {
                areaIdList.add(" a.area_id = " + areaIds[i]);
            } else {
                areaIdList.add(" OR a.area_id = " + areaIds[i]);
            }
        }
        String[] tagIds = tagId.split(",");
        int tagIdsLen = tagIds.length;
        List<String> tagIdList = new ArrayList<>();
        for (int i = 0; i < tagIdsLen; i++) {
            if (i == 0) {
                tagIdList.add(" b.`tag_id` = " + tagIds[i]);
            } else {
                tagIdList.add(" OR b.`tag_id` = " + tagIds[i]);
            }
        }

        Set<String> set = jsonObjectChoose.keySet();
        Iterator<String> iterator = set.iterator();
        int j = 0;
        List<String> whereList = new ArrayList<>();
        while (iterator.hasNext()) {
            String iteratorValue = iterator.next();
            if (j == 0) {
                whereList.add("WHERE a." + iteratorValue + " LIKE '%" + jsonObjectChoose.getString(iteratorValue) + "%' ");
            } else {
                whereList.add("AND a." + iteratorValue + " LIKE '%" + jsonObjectChoose.getString(iteratorValue) + "%' ");
            }
            j++;
        }
        List<String> list = new ArrayList<>();
        list.add(" SELECT *,GROUP_CONCAT(a.name) AS tag_names,GROUP_CONCAT(a.tagId) AS tag_ids  ");
        list.add(" FROM(SELECT b.*, d.name, d.id AS tagId, e.area_id,f.user_name, g.plan_name  ");
        list.add(" FROM (SELECT a.id FROM sys_scheme a,sys_scheme_tag_dep b  ");
        if ("".equals(tagId)) {
            list.add(" WHERE a.id = b.scheme_id ");
        } else {
            list.add(" WHERE a.id = b.scheme_id AND (" + StringUtils.join(tagIdList, "") + ")  ");
        }
        list.add(" GROUP BY a.id) a,sys_scheme b,sys_scheme_tag_dep c,sys_tag d,sys_scheme_area_dep e,sys_user f, sys_plan g ");
        list.add(" WHERE a.id = b.id AND b.id = c.scheme_id AND c.tag_id = d.id AND a.id = e.scheme_id ");
        list.add(" AND b.scheme_creater = f.user_loginname AND b.scheme_plan_id = g.id ) a  ");
        list.add(" " + StringUtils.join(whereList, ""));
        if (chooseSchemeData == null || "{}".equals(chooseSchemeData)) {
            list.add(" WHERE " + StringUtils.join(areaIdList, "") + " ");
        } else {
            list.add("  AND (" + StringUtils.join(areaIdList, "") + ") ");
        }
        list.add(" GROUP BY a.id  ");
        ExecResult execResult = jsonResponse.getSelectResult(StringUtils.join(list, ""), null, "");
        JSONArray jsonArray = (JSONArray) execResult.getData();
        jsonObject.put("data", jsonArray);
        jsonObject.put("total", jsonArray.size());
        return jsonObject;
    }
}
