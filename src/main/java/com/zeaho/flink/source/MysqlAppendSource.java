package com.zeaho.flink.source;

import com.zeaho.flink.model.domain.Machine;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author lzzz
 */
public class MysqlAppendSource extends RichSourceFunction<Machine> implements ListCheckpointed<Long> {

    private static final Logger log = LoggerFactory.getLogger(MysqlAppendSource.class);

    private long id = 0L;
    private volatile boolean isRunning = true;

    final String sql = "SELECT m.*, t.name FROM machine as m LEFT JOIN tenant as t on m.tenant_id = t.id WHERE m.id > ? LIMIT 100";

    private Connection connection;

    private PreparedStatement ps;

    private Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://192.168.2.230:4408/zhg", "dev_readonly", "#Fsm@2019DWDjjHRAasdf");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    private ResultSet getResultSet() throws SQLException {
        try {
            this.ps = this.connection.prepareStatement(sql);
            this.ps.setLong(1, id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return this.ps.executeQuery();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        this.connection = getConnection();
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();

    }

    @Override
    public void close() throws Exception {
        super.close();
        if (Objects.nonNull(connection)) {
            connection.close();
        }
        if (Objects.nonNull(ps)) {
            ps.close();
        }
    }

    @Override
    public void run(SourceContext ctx) throws Exception {
        while (isRunning) {
            ResultSet resultSet = getResultSet();
            while (resultSet.next()) {
                Machine machine = new Machine();
                machine.setId(resultSet.getLong("id"));
                machine.setTenantId(resultSet.getLong("tenant_id"));
                machine.setTenantName(resultSet.getString("t.name"));
                machine.setMachineName(resultSet.getString("machine_name"));
                machine.setCategoryId(resultSet.getInt("category_id"));
                machine.setBrandId(resultSet.getInt("brand_id"));
                machine.setModelId(resultSet.getInt("model_id"));
                machine.setFactoryCompany(resultSet.getString("factory_company"));
                machine.setMachineAge(resultSet.getLong("machine_age"));
                machine.setOriginWorth(resultSet.getDouble("origin_worth"));
                machine.setCreatedAt(resultSet.getTimestamp("created_at").getTime());
                ctx.collect(machine);
                id = machine.getId();
            }
            log.info("current id {}", id);
            Thread.sleep(1000);
        }
    }

    @Override
    public void cancel() {
        this.isRunning = false;
    }

    @Override
    public List<Long> snapshotState(long checkPointId, long checkPointTimestamp) throws Exception {
        return Collections.singletonList(id);
    }

    @Override
    public void restoreState(List<Long> list) throws Exception {
        if (Objects.nonNull(list) && list.size() > 0) {
            id = list.get(0);
        }
    }
}
