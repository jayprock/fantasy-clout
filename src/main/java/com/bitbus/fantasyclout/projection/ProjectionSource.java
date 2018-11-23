package com.bitbus.fantasyclout.projection;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class ProjectionSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int sourceId;

    @Enumerated(EnumType.STRING)
    private ProjectionSourceType name;

    public ProjectionSource() {}

    public ProjectionSource(ProjectionSourceType name) {
        this.name = name;
    }

}
