package com.bitbus.fantasyclout.projection.football;

import java.math.BigDecimal;

import com.bitbus.fantasyclout.player.football.FootballPlayerPosition;

import lombok.Data;

@Data
public class FootballPlayerProjectionDTO {

    private String playerName;

    private String teamAbbreviation;

    private FootballPlayerPosition position;

    private BigDecimal passingYards;

    private BigDecimal passingTds;

    private BigDecimal interceptions;

    private BigDecimal rushingYards;

    private BigDecimal rushingTds;

    private BigDecimal receptions;

    private BigDecimal receivingYards;

    private BigDecimal receivingTds;

    private BigDecimal fumblesLost;

    private BigDecimal fieldGoalsMade;

    private BigDecimal extraPointsMade;

    private BigDecimal defensiveSacks;

    private BigDecimal defensiveInterceptions;

    private BigDecimal defensiveFumblesRecovered;

    private BigDecimal defensiveTds;

    private BigDecimal defensiveSafeties;

    private BigDecimal defensivePointsAllowed;

}
