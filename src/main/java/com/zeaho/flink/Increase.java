package com.zeaho.flink;

import com.zeaho.flink.model.domain.Machine;
import com.zeaho.flink.model.domain.ScoredTenantMachine;
import com.zeaho.flink.model.dto.TenantMachine;
import com.zeaho.flink.sink.ScoredRedisSink;
import com.zeaho.flink.source.MysqlAppendSource;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author lzzz
 */
public class Increase {
    private static final Logger log = LoggerFactory.getLogger(Increase.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        env
                .addSource(new MysqlAppendSource())
                .assignTimestampsAndWatermarks(new AscendingTimestampExtractor<Machine>() {
                    @Override
                    public long extractAscendingTimestamp(Machine machine) {
                        return machine.getCreatedAt();
                    }
                })
                .keyBy(new KeySelector<Machine, Long>() {
                    @Override
                    public Long getKey(Machine machine) throws Exception {
                        return machine.getTenantId();
                    }
                })
                .timeWindow(Time.days(7))
                .aggregate(new AggregateFunction<Machine, TenantMachine, TenantMachine>() {
                    @Override
                    public TenantMachine createAccumulator() {
                        return new TenantMachine(null, 0L);
                    }

                    @Override
                    public TenantMachine add(Machine machine, TenantMachine tenantMachine) {
                        return new TenantMachine(machine.getTenantName(), tenantMachine.getMachineCount() + 1);
                    }

                    @Override
                    public TenantMachine getResult(TenantMachine tenantMachine) {
                        return tenantMachine;
                    }

                    @Override
                    public TenantMachine merge(TenantMachine a, TenantMachine b) {
                        return new TenantMachine(a.getTenantName(), a.getMachineCount() + b.getMachineCount());
                    }
                }, new WindowFunction<TenantMachine, ScoredTenantMachine, Long, TimeWindow>() {
                    @Override
                    public void apply(Long tenantId, TimeWindow timeWindow, Iterable<TenantMachine> iterable, Collector<ScoredTenantMachine> collector) throws Exception {
                        TenantMachine tenantMachine = iterable.iterator().next();
                        collector.collect(new ScoredTenantMachine(tenantMachine.getTenantName(), tenantMachine.getMachineCount(), timeWindow.getEnd(), timeWindow.getStart()));
                    }
                })
                .keyBy(new KeySelector<ScoredTenantMachine, Long>() {
                    @Override
                    public Long getKey(ScoredTenantMachine scoredTenantMachine) throws Exception {
                        return scoredTenantMachine.getEndTime();
                    }
                })
                .process(new KeyedProcessFunction<Long, ScoredTenantMachine, List<ScoredTenantMachine>>() {
                    private ListState<ScoredTenantMachine> machinesState;

                    @Override
                    public void open(Configuration parameters) throws Exception {
                        super.open(parameters);
                        ListStateDescriptor<ScoredTenantMachine> descriptor = new ListStateDescriptor<ScoredTenantMachine>("machines", ScoredTenantMachine.class);
                        machinesState = getRuntimeContext().getListState(descriptor);
                    }

                    @Override
                    public void processElement(ScoredTenantMachine scoredTenantMachine, Context context, Collector<List<ScoredTenantMachine>> collector) throws Exception {
                        machinesState.add(scoredTenantMachine);
                        context.timerService().registerProcessingTimeTimer(scoredTenantMachine.getEndTime() + 1);
                    }

                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<List<ScoredTenantMachine>> out) throws Exception {
                        super.onTimer(timestamp, ctx, out);
                        Set<ScoredTenantMachine> sortedMachines = new TreeSet<>();
                        for (ScoredTenantMachine machine : machinesState.get()) {
                            sortedMachines.add(machine);
                        }
                        machinesState.clear();
                        out.collect(sortedMachines.stream().limit(10).collect(Collectors.toList()));
                        Thread.sleep(5000);
                    }
                }).addSink(new ScoredRedisSink<>("127.0.0.1", 6379, 1, "machineIncrease"));

        env.execute("Machine Increase");
    }
}
