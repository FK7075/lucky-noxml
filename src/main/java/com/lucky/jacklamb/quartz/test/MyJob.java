package com.lucky.jacklamb.quartz.test;

import com.lucky.jacklamb.quartz.ann.Scheduled;
import com.lucky.jacklamb.utils.base.LuckyUtils;

public class MyJob {

    @Scheduled(cron = "2/2 * * * * ?")
    public void showTime(String k){
        System.out.println("showTime("+k+") || Run==>"+ LuckyUtils.time());
    }

    @Scheduled(cron = "1/2 * * * * ?")
    public void time(){
        System.out.println("time() || Run==>"+ LuckyUtils.time());
    }

    public void ttt(){
        System.out.println("kokokok");
    }
}
