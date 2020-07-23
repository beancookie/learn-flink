package com.zeaho.flink;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

/**
 * @author lzzz
 */
public class WordCount {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> source = env.socketTextStream("localhost", 9988, "\n");
        source.flatMap((FlatMapFunction<String, Tuple2<String, Integer>>) (s, collector) -> {
            String[] tokens = s.toLowerCase().split("\\W+");
            for (String token : tokens) {
                collector.collect(new Tuple2<>(token, 1));
            }
        }).keyBy(0).timeWindow(Time.seconds(10), Time.seconds(5)).sum(1).print();

        env.execute("flink socket word count");

    }
}
