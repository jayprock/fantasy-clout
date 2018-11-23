package com.bitbus.fantasyclout.projection;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public abstract class Projection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long projectionId;

    private int seasonYear;

    @Enumerated(EnumType.STRING)
    private ProjectionType projectionType;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "source_id")
    private ProjectionSource source;

    private LocalDateTime projectionTime;

}
