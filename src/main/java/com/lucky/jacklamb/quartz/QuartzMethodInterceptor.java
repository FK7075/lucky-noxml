package com.lucky.jacklamb.quartz;

import com.lucky.jacklamb.aop.util.ASMUtil;
import com.lucky.jacklamb.quartz.ann.Job;
import com.lucky.jacklamb.utils.reflect.MethodUtils;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import static com.lucky.jacklamb.quartz.constant.Constant.LUCKY_JOB;
import static com.lucky.jacklamb.quartz.constant.Constant.LUCKY_JOB_GROUP;
import static org.quartz.DateBuilder.futureDate;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

public class QuartzMethodInterceptor implements MethodInterceptor {

    private static Scheduler scheduler;

    @Override
    public Object intercept(Object targetObj, Method method, Object[] params, MethodProxy methodProxy) throws Throwable {
        if(method.isAnnotationPresent(Job.class)){
            if(scheduler==null)
                scheduler = StdSchedulerFactory.getDefaultScheduler();
            String jtname= UUID.randomUUID().toString();
            Job quartzJob = method.getAnnotation(Job.class);
            //封装任务逻辑
            TargetJobRun targetJobRun=new TargetJobRun(targetObj,methodProxy,params);
            JobDetail jobDetail = JobBuilder.newJob(LuckyJob.class)
                    .withIdentity(jtname, LUCKY_JOB_GROUP)
                    .build();
            //将任务逻辑put到上下文中
            jobDetail.getJobDataMap().put(LUCKY_JOB,targetJobRun);
            Map<String, Object> paramKV = MethodUtils.getClassMethodParamsNV(method, params);
            Trigger trigger =getTrigger(paramKV,method,quartzJob,jtname);
            scheduler.scheduleJob(jobDetail,trigger);
            scheduler.start();
            return null;
        }
        return methodProxy.invokeSuper(targetObj,params);
    }

    //根据@Job注解的属性构造相应的Trigger
    private Trigger getTrigger(Map<String,Object> paramKV,Method method, Job job, String triggerName){
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger().withIdentity(triggerName, LUCKY_JOB_GROUP);
        String cron = job.cron();
        String dyCron=job.dyCron();
        if(!"".equals(dyCron)){
            if(!paramKV.containsKey(dyCron))
                throw new RuntimeException("定时任务 '"+method+" '缺少必要的参数：'(String)"+dyCron+"' ");
            cron=(String)paramKV.get(dyCron);
        }
        //Cron表达式构建Trigger
        if(!"".equals(cron)){
            Trigger trigger = triggerBuilder.startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
            return trigger;
        }
        long fixedDelay = job.fixedDelay();
        String dyDelay=job.dyDelay();
        if(!"".equals(dyDelay)){
            if(!paramKV.containsKey(dyDelay))
                throw new RuntimeException("定时任务 '"+method+" '缺少必要的参数：'(Long)"+dyDelay+"' ");
            fixedDelay=(long)paramKV.get(dyDelay);
        }

        int count = job.count();
        String dyCount=job.dyCount();
        if(!"".equals(dyCount)){
            if(!paramKV.containsKey(dyCount))
                throw new RuntimeException("定时任务 '"+method+" '缺少必要的参数：'(Long)"+dyCount+"' ");
            count=(int)paramKV.get(dyCount);
        }

        long interval = job.interval();
        String dyInterval=job.dyInterval();
        if(!"".equals(dyInterval)){
            if(!paramKV.containsKey(dyInterval))
                throw new RuntimeException("定时任务 '"+method+" '缺少必要的参数：'(Long)"+dyInterval+"' ");
            interval=(long)paramKV.get(dyInterval);
        }



        //构建一个固定延时且只会执行一次的Trigger
        if(fixedDelay!=-1L){
            Trigger trigger =triggerBuilder
                    .startAt(futureDate((int) fixedDelay, DateBuilder.IntervalUnit.MILLISECOND))
                    .build();
            return trigger;
        }

        //构建一个相隔固定时间一次执行的Trigger(永不结束)
        if(count<1){
            Trigger trigger =triggerBuilder
                    .withSchedule(simpleSchedule()
                            .withIntervalInMilliseconds(interval)
                            .repeatForever())
                    .build();
            return trigger;
        }

        //构建一个相隔固定时间一次执行,且执行count次后会结束的Trigger
        Trigger trigger = triggerBuilder
                .withSchedule(simpleSchedule().withIntervalInMilliseconds(interval).withRepeatCount((int)(count-1)))
                .build();
        return trigger;

    }
}
