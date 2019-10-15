package com.njits.iot.advert.tyx;

import java.sql.SQLException;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.njits.iot.advert.common.db.SqlHandler;

/**
 *  武汉GPS表分区job
 * @author 43797
 *
 */
public class RemoveDBClient implements Job {
	
	private static final Logger logger = LoggerFactory.getLogger(RemoveDBClient.class);
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// 创建工作详情
		JobDetail detail = context.getJobDetail();
		logger.info("task execute detail:{}", detail.toString());
		
		SqlHandler queryHandler = new SqlHandler();
		String sql = "";
		try {
			sql = "{CALL Create_Partition_XJCZC_SSWZXX_TAB_HISTORY}";
			queryHandler.execProcedure(sql);
		} catch (SQLException e) {
			logger.error("sql={}, msg={}", sql, e.getMessage());
		}
	}
}
