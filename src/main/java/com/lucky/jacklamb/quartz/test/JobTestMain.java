package com.lucky.jacklamb.quartz.test;

import com.lucky.jacklamb.quartz.QuartzProxy;
import org.quartz.SchedulerException;

public class JobTestMain {

    public static void main(String[] args) throws SchedulerException, InterruptedException {
        MyJob job = QuartzProxy.getProxy(MyJob.class);
        job.showTime("K");
//        job.time(2*1000L,100,"TEST-1");
////        job.ttt();
//        Thread.sleep(1000*5);
//        job.time(3*1000L,10,"TEST-2");
//        Thread.sleep(1000*3);
//        job.time(3*1000L,6,"TEST-3");
//        job.ttt();
    }
}
