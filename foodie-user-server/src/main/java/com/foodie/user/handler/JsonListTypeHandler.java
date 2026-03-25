package com.foodie.user.handler;

/**
 * @Author: wanderer
 * @Date: 2026/1/24 11:14
 */

import com.alibaba.fastjson2.JSON;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Collections;
import java.util.List;

public class JsonListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(
            PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, JSON.toJSONString(parameter));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName)
            throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex)
            throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex)
            throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<String> parse(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        return JSON.parseArray(json, String.class);
    }
}