package com.lucky.jacklamb.quartz;

import net.sf.cglib.proxy.MethodProxy;

public class TargetJobRun{

    private Object job;

    private MethodProxy jobMethodProxy;

    private Object[] jobParams;

    public TargetJobRun(Object job, MethodProxy jobMethodProxy, Object[] jobParams) {
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
