package com.zeaho.flink.sink;

import com.alibaba.fastjson.JSON;
import com.zeaho.flink.model.SortedModel;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.redisson.Redisson;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author lzzz
 */
public class ScoredRedisSink<T> extends RichSinkFunction<T> {

    private static Logger log = LoggerFactory.getLogger(ScoredRedisSink.class);

    private RedissonClient redissonClient;

    private RScoredSortedSet<String> rSortedSet;

    private String host;

    private int port;

    private int db;

    private String redisKey;

    @Override
    public void open(Configuration parameters) throws Exception {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%d", host, port))
                .setDatabase(db);
        redissonClient = Redisson.create(config);
    }

    @Override
    public void close() throws Exception {
        super.close();
    }

    public ScoredRedisSink(String host, int port, int db, String redisKey) {
        this.host = host;
        this.port = port;
        this.db = db;
        this.redisKey = redisKey;
    }

    private void removeAndAdd(SortedModel value) {
        rSortedSet = redissonClient.getScoredSortedSet(String.format("%s:%d", redisKey, value.getKey()), new StringCodec());
        rSortedSet.removeRangeByScore(value.getScored(), true, value.getScored(), true);
        rSortedSet.add(value.getScored(), JSON.toJSONString(value));
    }

    @Override
    public void invoke(T values, Context context) throws Exception {
        if (values instanceof List) {
            for (Object value : ((List) values)) {
                if (value instanceof SortedModel) {
                    removeAndAdd((SortedModel) value);
                }
            }
        } else if (values instanceof SortedModel) {
            removeAndAdd((SortedModel) values);
        }
    }
}
