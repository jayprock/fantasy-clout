package com.bitbus.fantasyclout.projection.football;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.bitbus.fantasyclout.player.football.FootballPlayer;
import com.bitbus.fantasyclout.projection.Projection;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
public class FootballPlayerProjection extends Projection {

    @ManyToOne
    @JoinColumn(name = "player_id")
    private FootballPlayer player;

    private Integer projectionWeek;

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
