package com.zeaho.flink.model.dto;

/**
 * @author lzzz
 */
public class TenantMachine {
    private String tenantName;
    private Long machineCount;

    public TenantMachine() {
    }

    public TenantMachine(String tenantName, Long machineCount) {
        this.tenantName = tenantName;
        this.machineCount = machineCount;
    }

    @Override
    public String toString() {
        return "TenantMachine{" +
                "tenantName='" + tenantName + '\'' +
                ", machineCount=" + machineCount +
                '}';
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public Long getMachineCount() {
        return machineCount;
    }

    public void setMachineCount(Long machineCount) {
        this.machineCount = machineCount;
    }
}
