package com.lucky.jacklamb.quartz.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class SerialJob extends LuckyJob {

}
