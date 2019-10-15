package com.njits.iot.advert.tyx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.njits.iot.advert.common.db.DataSource;

public class CarInfoHandler implements Job {
	
	private static final Logger logger = LoggerFactory.getLogger(CarInfoHandler.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 创建工作详情
		JobDetail detail = context.getJobDetail();
		logger.info("task execute detail:{}", detail.toString());
		CarInfoHandler.execQueryCarInfo();
	}
	
	public static Map<String, String> carInfoMap = new HashMap<>();
	
	public static void execQueryCarInfo() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		String sql = "select t.DTU_CODE,t.DEV_NAME from AD_ODS_DEVICE t where t.ACTIVE_FLAG=1 and t.GROUP_CODE = 'GD180801'";
		try {
			conn = DataSource.getConnection(DataSource.MYSQL_ADV);
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				carInfoMap.put(rs.getString("DTU_CODE"), rs.getString("DEV_NAME"));
			}
			logger.info("============ 初始化车辆基本信息成功 ============");
		} catch (SQLException e) {
			e.printStackTrace();
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
}
