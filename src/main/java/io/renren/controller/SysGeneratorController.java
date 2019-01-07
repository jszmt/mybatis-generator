package io.renren.controller;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mysql.jdbc.Connection;

import io.renren.service.SysGeneratorService;
import io.renren.utils.PageUtils;
import io.renren.utils.Query;
import io.renren.utils.R;

/**
 * 代码生成器
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2016年12月19日 下午9:12:58
 */
@Controller
@RequestMapping("/sys/generator")
public class SysGeneratorController {
	@Autowired
	private SysGeneratorService sysGeneratorService;
	
	private static final Map<String,Connection> coonMap = new HashMap<>();
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/getConnection")
	public R getConnection(@RequestBody Map<String, Object> params,HttpSession session) {
		Connection conn = null;
		try {
			conn = (Connection) DriverManager.getConnection("jdbc:mysql://"+(String)params.get("url")+":"+(String)params.get("port")+"/"+(String)params.get("database"), (String)params.get("usrName"), (String)params.get("password"));
			if(conn == null||conn.isClosed()) {
				return R.error("连接异常！");
			}else {
				coonMap.put(session.getId(), conn);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return R.error("连接异常！");
		}
		return R.ok();
	}
	
	/**
	 * 列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	public R list(@RequestParam Map<String, Object> params,HttpSession session){
		String sessionId = session.getId();
		Connection conn = coonMap.get(sessionId);
		try {
			if(conn == null||conn.isClosed()) {
				return R.error("连接异常,请重新获取连接！");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return R.error("连接异常,请重新获取连接！");
		}
		//查询列表数据
		Query query = new Query(params);
		List<Map<String, Object>> list = sysGeneratorService.queryList(query,conn);
		int total = sysGeneratorService.queryTotal(query,conn);
		
		PageUtils pageUtil = new PageUtils(list, total, query.getLimit(), query.getPage());
		
		return R.ok().put("page", pageUtil);
	}
	
	/**
	 * 生成代码
	 */
	@RequestMapping("/code")
	public void code(HttpServletRequest request, HttpServletResponse response,HttpSession session) throws IOException{
		String[] tableNames = new String[]{};
		String tables = request.getParameter("tables");
		tableNames = JSON.parseArray(tables).toArray(tableNames);
		
		//连接信息
		String conMsg = request.getParameter("conMsg");
		JSONObject conMsgJson = JSON.parseObject(conMsg);
		
		String sessionId = session.getId();
		Connection conn = coonMap.get(sessionId);
		try {
			if(conn == null||conn.isClosed()) {
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		byte[] data = sysGeneratorService.generatorCode(tableNames,conn,conMsgJson);
		
		String fileName = tableNames[0];
		if(tableNames.length > 1) {
			fileName = "mybatis生成文件列表";
		}
		
		response.reset();  
        response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+".zip\"");  
        response.addHeader("Content-Length", "" + data.length);  
        response.setContentType("application/octet-stream; charset=UTF-8");  
  
        IOUtils.write(data, response.getOutputStream());  
	}
	
	/***
	 *20分钟刷新一次，将缓存的连接清理掉 ,有可能刚登陆就赶上这一波了 页面会空白重新刷新就好
	 *添加con的时候 添加一个lastOptime 每次定时获取这个lastOptime 和当前时间比较  看看是否超过session时间（最后操作时间如果超过session失效时间则视为失效）
	 *
	 */
	@Scheduled(cron="0 0/20 * * * ?") 
	public void clearSessionConnection() {
		 for(Map.Entry<String, Connection> entry:coonMap.entrySet()) {
			 String sessionId = entry.getKey();
			 System.out.println("清理连接缓存，sessionId="+sessionId);
			 Connection con = entry.getValue();
			 coonMap.remove(sessionId);
			 try {
				con.close();
				con = null;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 }
	}
}
