package io.renren.service;

import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import io.renren.utils.GenUtils;

/**
 * 代码生成器
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午3:33:38
 */
@Service
public class SysGeneratorService {

	public List<Map<String, Object>> queryList(Map<String, Object> map, Connection conn) {
		List<Map<String, Object>>  list = new ArrayList<Map<String, Object>>();
		StringBuilder sql = new StringBuilder();
		sql.append("select table_name tableName, engine, table_comment tableComment, create_time createTime from information_schema.tables");
		sql.append(" where table_schema = (select database())");
		if(map.get("tableName") != null && map.get("tableName") != "") {
			sql.append("and table_name like concat('%', '"+map.get("tableName")+"', '%')");
		}
		sql.append(" order by create_time desc");
		if(map.get("offset") != null && map.get("limit") != null) {
			sql.append(" limit " + map.get("offset") + "," + map.get("limit"));
		}
		PreparedStatement pstmt = null;
		try {
			pstmt = (PreparedStatement)conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
			//int col = rs.getMetaData().getColumnCount();
	        while (rs.next()) {
	        	Map<String,Object> tableMap = new HashMap<String, Object>();
	        	tableMap.put("tableName", rs.getString("tableName"));
	        	tableMap.put("engine", rs.getString("engine"));
	        	tableMap.put("tableComment", rs.getString("tableComment"));
	        	tableMap.put("createTime", rs.getString("createTime"));
	        	list.add(tableMap);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public int queryTotal(Map<String, Object> map, Connection conn) {
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) as count from information_schema.tables where table_schema = (select database())");
		if(map.get("tableName")!=null && map.get("tableName") != "") {
			sql.append("and table_name like concat('%','"+map.get("tableName")+",' '%')");
		}
		int result = 0;
		PreparedStatement pstmt = null;
		try {
			pstmt = (PreparedStatement)conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	result = rs.getInt(1);
	        	break;
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public Map<String, String> queryTable(String tableName, Connection conn) {
		StringBuilder sql = new StringBuilder();
		sql.append("select table_name tableName, engine, table_comment tableComment, create_time createTime from information_schema.tables");
		sql.append(" where table_schema = (select database()) and table_name = ").append("'"+tableName+"'");
		Map<String,String> tableMap = new HashMap<String, String>();
		PreparedStatement pstmt = null;
		try {
			pstmt = (PreparedStatement)conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	tableMap.put("tableName", rs.getString("tableName"));
	        	tableMap.put("engine", rs.getString("engine"));
	        	tableMap.put("tableComment", rs.getString("tableComment"));
	        	tableMap.put("createTime", rs.getString("createTime"));
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tableMap;
	}

	public List<Map<String, String>> queryColumns(String tableName, Connection conn) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		StringBuilder sql = new StringBuilder();
		sql.append("select column_name columnName, data_type dataType, column_comment columnComment, column_key columnKey, extra from information_schema.columns");
		sql.append(" where table_name = '"+ tableName +"' and table_schema = (select database()) order by ordinal_position");
		PreparedStatement pstmt = null;
		try {
			pstmt = (PreparedStatement)conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	        	Map<String,String> tableMap = new HashMap<String, String>();
	        	tableMap.put("columnName", rs.getString("columnName"));
	        	tableMap.put("dataType", rs.getString("dataType"));
	        	tableMap.put("columnComment", rs.getString("columnComment"));
	        	tableMap.put("columnKey", rs.getString("columnKey"));
	        	tableMap.put("extra", rs.getString("extra"));
	        	list.add(tableMap);
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public byte[] generatorCode(String[] tableNames, Connection conn, JSONObject conMsgJson) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ZipOutputStream zip = new ZipOutputStream(outputStream);

		for(String tableName : tableNames){
			//查询表信息
			Map<String, String> table = queryTable(tableName,conn);
			//查询列信息
			List<Map<String, String>> columns = queryColumns(tableName,conn);
			//生成代码
			GenUtils.generatorCode(table, columns, zip,conMsgJson);
		}
		IOUtils.closeQuietly(zip);
		return outputStream.toByteArray();
	}
}
