package com.zeaho.flink.source;

import com.zeaho.flink.model.domain.Machine;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.quartz.*;
import org.quartz.core.jmx.JobDataMapSupport;
import org.quartz.impl.StdSchedulerFactory;

import javax.management.openmbean.TabularData;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luzhong
 */
public class QuartzSource implements SourceFunction<Machine> {
    @Override
    public void run(SourceContext<Machine> sourceContext) throws Exception {
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("sourceContext", sourceContext);
        JobDetail job = JobBuilder.newJob(PrintJob.class)
                .usingJobData(JobDataMapSupport.newJobDataMap(jobData))
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(1).repeatForever())
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    @Override
    public void cancel() {

    }

    public static class PrintJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            SourceContext<Machine> sourceContext = (SourceContext<Machine>) jobExecutionContext.getMergedJobDataMap().get("sourceContext");
            Machine machine = new Machine();
            machine.setId(1L);
            machine.setMachineName("machine_name");
            machine.setCategoryId(2);
            machine.setBrandId(3);
            machine.setModelId(4);
            machine.setFactoryCompany("factory_company");
            machine.setMachineAge(5L);
            sourceContext.collect(machine);

        }
    }
}
