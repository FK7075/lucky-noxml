package com.lucky.jacklamb.quartz.test;

import com.lucky.jacklamb.cglib.CglibProxy;
import com.lucky.jacklamb.quartz.QuartzMethodInterceptor;
import com.lucky.jacklamb.quartz.QuartzProxy;
import org.quartz.SchedulerException;

public class JobTestMain {

    public static void main(String[] args) throws SchedulerException {
        MyJob job = QuartzProxy.getProxy(MyJob.class);
//        System.out.println("Start.....");
        job.time();
//        System.out.println("End.....");
//        job.showTime("FK");
//        job.showTime("JACK");
//        job.ttt();

    }
}
