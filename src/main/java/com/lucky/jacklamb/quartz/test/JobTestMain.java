package com.lucky.jacklamb.quartz.test;

import com.lucky.jacklamb.quartz.QuartzProxy;
import org.quartz.SchedulerException;

public class JobTestMain {

    public static void main(String[] args) throws SchedulerException {
        MyJob job = QuartzProxy.getProxy(MyJob.class);
        job.showTime("50/2 * * * * ?");
    }
}
