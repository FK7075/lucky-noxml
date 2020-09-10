package com.lucky.jacklamb.quartz.job;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.quartz.TargetJobRun;
import com.lucky.jacklamb.servlet.core.BaseServlet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import static com.lucky.jacklamb.quartz.constant.Constant.*;

public class LuckyJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobRunBeanId = context.getJobDetail().getJobDataMap().getString(LUCKY_JOB_KEY);
        TargetJobRun targetJobRun = JobMap.get(jobRunBeanId);
        targetJobRun.run();
    }

}
