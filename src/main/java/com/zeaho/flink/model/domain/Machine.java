package com.zeaho.flink.model.domain;

/**
 * @author lzzz
 */
public class Machine {
    private Long id;
    private Long tenantId;
    private String tenantName;
    private String machineName;
    private Integer categoryId;
    private Integer brandId;
    private Integer modelId;
    private String factoryCompany;
    private Long machineAge;
    private Double originWorth;
    private Long createdAt;

    public Machine() {
    }

    @Override
    public String toString() {
        return "Machine{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", tenantName='" + tenantName + '\'' +
                ", machineName='" + machineName + '\'' +
                ", categoryId=" + categoryId +
                ", brandId=" + brandId +
                ", modelId=" + modelId +
                ", factoryCompany='" + factoryCompany + '\'' +
                ", machineAge=" + machineAge +
                ", originWorth=" + originWorth +
                ", createdAt=" + createdAt +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public String getFactoryCompany() {
        return factoryCompany;
    }

    public void setFactoryCompany(String factoryCompany) {
        this.factoryCompany = factoryCompany;
    }

    public Long getMachineAge() {
        return machineAge;
    }

    public void setMachineAge(Long machineAge) {
        this.machineAge = machineAge;
    }

    public Double getOriginWorth() {
        return originWorth;
    }

    public void setOriginWorth(Double originWorth) {
        this.originWorth = originWorth;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
