package com.lucky.jacklamb.quartz.test;

import com.lucky.jacklamb.quartz.ann.Job;
import com.lucky.jacklamb.utils.base.LuckyUtils;

public class MyJob {

    @Job(dyCron = "k")
    public void showTime(String k){
        System.out.println("showTime("+k+") || Run==>"+ LuckyUtils.time());
    }

    @Job(dyInterval = "interval", dyCount ="counte")
    public void time(Long interval,Integer counte,String fk){
        System.out.println("time() || Run==>"+ LuckyUtils.time());
    }

    public void ttt(){
        System.out.println("kokokok");
    }
}
