package com.lucky.jacklamb.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static com.lucky.jacklamb.quartz.constant.Constant.*;

public class LuckyJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        TargetJobRun targetJobRun = (TargetJobRun) context.getJobDetail().getJobDataMap().get(LUCKY_JOB);
        targetJobRun.run();
    }
}
