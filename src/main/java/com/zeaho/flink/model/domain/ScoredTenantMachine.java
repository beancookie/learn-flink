package com.zeaho.flink.model.domain;

import com.zeaho.flink.model.SortedModel;
import com.zeaho.flink.model.dto.TenantMachine;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author lzzz
 */
public class ScoredTenantMachine extends TenantMachine implements SortedModel, Comparable<ScoredTenantMachine> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    private Long startTime;
    private Long endTime;

    public ScoredTenantMachine(String tenantName, Long machineCount, Long startTime, Long endTime) {
        super(tenantName, machineCount);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "ScoredTenantMachine{" +
                "tenantName=" + super.getTenantName() +
                ", machineCount=" + super.getMachineCount() +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoredTenantMachine that = (ScoredTenantMachine) o;
        return Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(super.getTenantName(), that.getTenantName()) &&
                Objects.equals(super.getMachineCount(), that.getMachineCount());
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, super.getTenantName(), super.getMachineCount());
    }

    @Override
    public long getKey() {
        return this.getEndTime();
    }

    @Override
    public double getScored() {
        return this.getMachineCount();
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public int compareTo(ScoredTenantMachine other) {
        return (int) (this.getScored() - other.getScored());
    }
}
