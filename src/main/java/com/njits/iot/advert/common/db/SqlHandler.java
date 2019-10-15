package com.njits.iot.advert.common.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.util.JSONUtil;

/**
 *  执行sql查询服务
 * @author 43797
 * @date   2018年12月20日 下午9:58:31
 */
public class SqlHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SqlHandler.class);
	
	public String execQuery(String sql, Map<String, String> paramMap) throws SQLException {
		return JSONUtil.toJson(execQueryList(sql, paramMap));
	}
	
	public List<JSONObject> execQueryList(String sql, Map<String, String> paramMap) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DataSource.getConnection(DataSource.MYSQL_ADV);
			if(paramMap!=null && !paramMap.isEmpty()) {
				for(Map.Entry<String, String> entry : paramMap.entrySet()) {
					if(this.isNumeric(entry.getValue())) {
						sql = sql.replaceAll("@"+entry.getKey(), entry.getValue());
					} else {
						sql = sql.replaceAll("@"+entry.getKey(), "'"+entry.getValue()+"'");
					}
				}
			}
			logger.debug("Execute query sql ==> {}", sql);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			return JSONUtil.resultSetToJsonList(rs);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("close connection failed, {}", e.getMessage());
			}
		}
	}
	
	
	/**
	 * 武汉交通局专用
	 * @param sql
	 * @param list
	 * @throws SQLException
	 */
	/*public void execInsertList(String sql, List<GpsBean> list) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		long beginTime  = (new Date()).getTime();
		try {
			logger.debug("Execute insert sql ==> {}", sql);
			conn = DataSource.getConnection(DataSource.MYSQL_TYX);
			ps = conn.prepareStatement(sql);
			int size = list.size();
			int n = 0;
			for(GpsBean gspBean : list) {
				ps.setString(1, gspBean.getDevName());
				ps.setDouble(2, gspBean.getLng());
				ps.setDouble(3, gspBean.getLat());
				ps.setLong(4, gspBean.getCreateDate());
				ps.addBatch();
				n++;
				//批量插入
				if (n % 1000 == 0 || n == size) {
					ps.executeBatch();
					conn.commit();
				}
			}
			long endTime  = (new Date()).getTime();
			logger.info("Execute insert sql completion, execution time (milliseconds) ==> {}, count ==> {}", endTime - beginTime, size);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("close psmt or conn failed, {}", e.getMessage());
			}
		}
	}*/
	
	/**
	 * 武汉天宇星专用
	 * @param sql
	 * @param list
	 * @throws SQLException
	 */
	public void execInsertTYXList(String sql, List<GpsBean> list) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		long beginTime  = (new Date()).getTime();
		try {
			logger.debug("TYX Execute insert sql ==> {}", sql);
			conn = DataSource.getConnection(DataSource.MYSQL_TYX);
			ps = conn.prepareStatement(sql);
			int size = list.size();
			int n = 0;
			for(GpsBean gspBean : list) {
				ps.setString(1, gspBean.getDtuCode());
				ps.setString(2, gspBean.getDevName());
				ps.setTimestamp(3, gspBean.getStamp());
				ps.setDouble(4, gspBean.getLng());
				ps.setDouble(5, gspBean.getLat());
				ps.addBatch();
				n++;
				//批量插入
				if (n % 1000 == 0 || n == size) {
					ps.executeBatch();
					conn.commit();
				}
			}
			long endTime  = (new Date()).getTime();
			logger.info("TYX Execute insert tyx sql completion, execution time (milliseconds) ==> {}, count ==> {}", endTime - beginTime, size);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("TYX close psmt or conn failed, {}", e.getMessage());
			}
		}
	}
	
	/**
	 * 武汉天宇星专用
	 * @param sql
	 * @param list
	 * @throws SQLException
	 */
	public void execInsertCarTYXList(String sql, List<GpsBean> list) throws SQLException {
		Connection conn = null;
		PreparedStatement ps = null;
		long beginTime  = (new Date()).getTime();
		try {
			logger.debug("TYX Execute insert sql ==> {}", sql);
			conn = DataSource.getConnection(DataSource.MYSQL_TYX);
			ps = conn.prepareStatement(sql);
			int size = list.size();
			int n = 0;
			for(GpsBean gspBean : list) {
				ps.setString(1, gspBean.getDevName());
				ps.addBatch();
				n++;
				//批量插入
				if (n % 1000 == 0 || n == size) {
					ps.executeBatch();
					conn.commit();
				}
			}
			long endTime  = (new Date()).getTime();
			logger.info("TYX Execute insert car sql completion, execution time (milliseconds) ==> {}, count ==> {}", endTime - beginTime, size);
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.error("TYX close psmt or conn failed, {}", e.getMessage());
			}
		}
	}
	
	/**
	 *  表分区job执行
	 * @param sql
	 * @param list
	 * @throws SQLException
	 */
	public void execProcedure(String sql) throws SQLException {
		Connection conn = null;
		CallableStatement cstm = null;
		try {
			logger.debug("TYX Execute sql ==> {}", sql);
			conn = DataSource.getConnection(DataSource.MYSQL_TYX);
			cstm = conn.prepareCall(sql);
			cstm.execute();
		} finally {
			try {
				if (cstm != null) {
					cstm.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.error("TYX close cstm or conn failed, {}", e.getMessage());
			}
		}
	}
	
	/**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    public boolean isNumeric(String str){
           Pattern pattern = Pattern.compile("[0-9]*");
           Matcher isNum = pattern.matcher(str);
           if( !isNum.matches() ){
               return false;
           }
           return true;
    }
}
