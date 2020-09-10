package com.lucky.jacklamb.quartz;

import com.lucky.jacklamb.aop.proxy.TargetMethodSignature;
import com.lucky.jacklamb.quartz.job.LuckySchedulerListener;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TargetJobRun{

    private static final Logger log = LogManager.getLogger(TargetJobRun.class);

    private Object job;

    public boolean isFirst=true;

    private MethodProxy jobMethodProxy;

    private Object[] jobParams;

    public TargetJobRun(Object job,MethodProxy jobMethodProxy, Object[] jobParams) {
        this.job = job;
        this.jobMethodProxy = jobMethodProxy;
        this.jobParams = jobParams;
    }

    public Object getJob() {
        return job;
    }

    public void setJob(Object job) {
        this.job = job;
    }

    public MethodProxy getJobMethodProxy() {
        return jobMethodProxy;
    }

    public void setJobMethodProxy(MethodProxy jobMethodProxy) {
        this.jobMethodProxy = jobMethodProxy;
    }

    public Object[] getJobParams() {
        return jobParams;
    }

    public void setJobParams(Object[] jobParams) {
        this.jobParams = jobParams;
    }

    public void run(){
        try {
            jobMethodProxy.invokeSuper(job,jobParams);
        } catch (Throwable e) {
           throw new RuntimeException(e);
        }
    }
}
