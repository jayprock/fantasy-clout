package com.bitbus.fantasyclout.projection.football;

import java.util.List;

import com.bitbus.fantasyclout.projection.ProjectionSourceType;
import com.bitbus.fantasyclout.projection.ProjectionType;

import lombok.Data;

@Data
public class FootballProjectionDTO {

    private ProjectionType projectionType;

    private ProjectionSourceType source;

    private List<FootballPlayerProjectionDTO> playerProjections;

    private Integer projectionWeek;

}
