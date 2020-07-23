package com.zeaho.flink.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * @author lzzz
 */
public interface SortedModel extends Serializable {

    @JsonIgnore
    long getKey();

    @JsonIgnore
    double getScored();

}
