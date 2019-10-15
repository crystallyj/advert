package com.njits.iot.advert.common;

import com.njits.iot.advert.common.db.MongodbSource;
import com.njits.iot.advert.common.entity.Constant;
import com.njits.iot.advert.common.entity.GpsBean;
import com.njits.iot.advert.util.DateUtil;
import com.njits.iot.advert.util.FileUtil;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * 武汉，同步三杰GPS数据到交通局
 *
 * @author 43797
 */
public class GpsSendClient implements Job
{
    
    private static final Logger logger = LoggerFactory.getLogger(GpsSendClient.class);
    
    private MongodbSource mongodbSource = new MongodbSource();
    
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        // 创建工作详情
        JobDetail detail = context.getJobDetail();
        
        GpsHandler sendHandler = (GpsHandler) detail.getJobDataMap().get("handler");
        String key = (String) detail.getJobDataMap().get("key");
        //记录上次读取数据位置
        File positionFile = new File(Constant.positionMap.get(key));
        String startDate = FileUtil.readTxt(positionFile, "utf-8");
        long timeStamp = System.currentTimeMillis() - 1000 * 10;//延时10秒
        String endDate = DateUtil.milliSecond2Date(timeStamp);
        
        List<GpsBean> list = mongodbSource.queryGpsList(startDate, endDate);
        
        int totalSize = list.size();
        if (totalSize == 0)
        {
            logger.info("{} task execute success, list is null", detail.getJobDataMap().get("key"));
            return;
        }
        int batchSize = Constant.BIG_BATCH_SIZE;//限制条数
        int part = totalSize / batchSize;//分批数
        int surplus = totalSize % batchSize;//余数
        if (surplus != 0)
        {
            part++;
        }
        boolean isRet = true;
        //分批读取数据库表记录
        for (int m = 0; m < part; m++)
        {
            int start = m * batchSize;
            int end = (m + 1) * batchSize;
            end = end > totalSize ? totalSize : end;
            
            List<GpsBean> subList = list.subList(start, end);
            if (subList != null && !subList.isEmpty())
            {
                if (!sendHandler.sendMsg(subList))
                {
                    isRet = false;
                    logger.error("============= 发送异常 ============== key:{}, list size:{} ", key, subList.size());
                    break;
                }
            }
        }
        if (isRet)
        {
            FileUtil.writeFile(endDate, positionFile, false);
            logger.info("{} task execute success, size:{}", detail.getJobDataMap().get("key"), list.size());
        }
    }
}
