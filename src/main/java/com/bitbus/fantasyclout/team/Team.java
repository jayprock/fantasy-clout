package com.bitbus.fantasyclout.team;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Data;

@MappedSuperclass
@Data
public class Team {

    @Id
    private int teamId;

    private String name;

    private String abbreviation;

}
