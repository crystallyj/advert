package com.njits.iot.advert.common.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 执行sql查询服务
 * @author 43797
 * @date   2019年1月3日 下午8:44:14
 */
public class SynDataHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(SynDataHandler.class);
	
	public List<Map<String, Object>> execQuery(String dbType, String sql) throws SQLException {
		Connection conn = DataSource.getConnection(dbType);
		List<Map<String, Object>> list = this.execQuery(conn, sql);
		this.close(conn);
		return list;
	}
	
	/**
	 * 连接需要在外部创建并在外部关闭
	 * @param conn
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public List<Map<String, Object>> execQuery(Connection conn, String sql) throws SQLException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		try {
			logger.debug("Execute query sql ==> {}", sql);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			int count = rs.getMetaData().getColumnCount();
			List<Map<String, Object>> list = new ArrayList<>();
			while(rs.next()) {
				Map<String, Object> map = new HashMap<>();
				for(int i=1; i<=count; i++) {
					String colName = rs.getMetaData().getColumnName(i);
					String colTypeName = rs.getMetaData().getColumnTypeName(i);
					if("NUMBER".equals(colTypeName)) {
						map.put(colName, rs.getLong(i));
					} else {
						map.put(colName, rs.getString(i));
					}
				}
				list.add(map);
			}
			return list;
		} finally {
			this.close(rs, ps);
		}
	}
	
	public void execInsert(String dbType, String sql, List<Map<String, Object>> list) throws SQLException {
		Connection conn = DataSource.getConnection(dbType);
		this.execInsert(conn, sql, list);
		this.close(conn);
	}
	
	/**
	 * 连接需要在外部创建并在外部关闭
	 * @param conn
	 * @param sql
	 * @param list
	 * @throws SQLException
	 */
	public void execInsert(Connection conn, String sql, List<Map<String, Object>> list) throws SQLException {
		PreparedStatement ps = null;
		long beginTime  = (new Date()).getTime();
		try {
			logger.info("Execute insert sql ==> {}", sql);
			ps = conn.prepareStatement(sql);
			String subSql = sql.substring(sql.indexOf("(")+1, sql.indexOf(")"));
			String[] fields = subSql.split(",");
			for(int i=0; i<fields.length; i++) {
				String field = fields[i].trim().toUpperCase();
				fields[i] = field;
			}
			int size = list.size();
			int n = 0;
			for(Map<String, Object> map : list) {
				for(int i=0; i<fields.length; i++) {
					ps.setObject(i+1, map.get(fields[i]));
				}
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
			this.close(ps);
		}
	}
	
	public void execUpdate(String dbType, String sql) throws SQLException {
		Connection conn = DataSource.getConnection(dbType);
		this.execUpdate(conn, sql);
		this.close(conn);
	}

	/**
	 * 连接需要在外部创建并在外部关闭
	 * @param conn
	 * @param sql
	 * @throws SQLException
	 */
	public void execUpdate(Connection conn, String sql) throws SQLException {
		PreparedStatement ps = null;
		try {
			logger.info("Execute update or delete sql ==> {}", sql);
			ps = conn.prepareStatement(sql);
			ps.execute();
			conn.commit();
		} finally {
			this.close(ps);
		}
	}
	
	private void close(ResultSet rs, PreparedStatement ps) {
		try {
			if (rs != null) {
				rs.close();
			}
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("close rs or psmt failed, {}", e.getMessage());
		}
	}
	
	private void close(PreparedStatement ps) {
		try {
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("close psmt failed, {}", e.getMessage());
		}
	}
	
	public void close(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error("close connection failed, {}", e.getMessage());
		}
	}
}
