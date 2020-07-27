package com.lucky.jacklamb.quartz.test;

import com.lucky.jacklamb.quartz.ann.Job;
import com.lucky.jacklamb.utils.base.LuckyUtils;

public class MyJob {

    @Job(cron = "1/2 * * * * ？")
    public void showTime(String k){
        System.out.println("showTime("+k+") || Run==>"+ LuckyUtils.time());
    }

    @Job(dyInterval = "interval", dyCount ="counte",onlyLast = true)
    public void time(Long interval,Integer counte,String fk){
        System.out.println("time("+fk+") || Run==>"+ LuckyUtils.time());
    }

    @Job(count = 5,interval = 3*1000L)
    public void ttt(){
        System.out.println(LuckyUtils.time()+" ==> kokokok");
    }
}
