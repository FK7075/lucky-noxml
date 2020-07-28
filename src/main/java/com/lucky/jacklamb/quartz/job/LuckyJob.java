package com.lucky.jacklamb.quartz.job;

import com.lucky.jacklamb.ioc.ApplicationBeans;
import com.lucky.jacklamb.quartz.TargetJobRun;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static com.lucky.jacklamb.quartz.constant.Constant.*;

public class LuckyJob implements Job {

    private static ApplicationBeans beans= ApplicationBeans.createApplicationBeans();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobRunBeanId = context.getJobDetail().getJobDataMap().getString(LUCKY_JOB);
        TargetJobRun targetJobRun = (TargetJobRun) beans.getBean(jobRunBeanId);
        beans.removeComponentBean(jobRunBeanId);
        targetJobRun.run();
    }
}
