package com.lucky.jacklamb.quartz;

import com.lucky.jacklamb.quartz.ann.Scheduled;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.lang.reflect.Method;
import java.util.UUID;

import static com.lucky.jacklamb.quartz.constant.Constant.*;

public class QuartzMethodInterceptor implements MethodInterceptor {

//    private Object targetObj;

//    public QuartzMethodInterceptor(Object targetObj) {
//        this.targetObj = targetObj;
//    }

    @Override
    public Object intercept(Object targetObj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        if(method.isAnnotationPresent(Scheduled.class)){
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            String jtname= UUID.randomUUID().toString();
            Scheduled scheduled = method.getAnnotation(Scheduled.class);
            TargetJobRun targetJobRun=new TargetJobRun(targetObj,methodProxy,params);
            JobDetail jobDetail = JobBuilder.newJob(LuckyJob.class)
                    .withIdentity(jtname, LUCKY_JOB_GROUP)
                    .build();
            jobDetail.getJobDataMap().put(LUCKY_JOB,targetJobRun);
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jtname, LUCKY_JOB_GROUP)
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(scheduled.cron()))
                    .build();
            scheduler.scheduleJob(jobDetail,trigger);
            scheduler.start();
            return null;
        }
        return methodProxy.invokeSuper(targetObj,params);
    }
}
