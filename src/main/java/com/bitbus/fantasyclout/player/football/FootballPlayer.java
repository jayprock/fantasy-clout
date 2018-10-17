package com.bitbus.fantasyclout.player.football;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.bitbus.fantasyclout.player.Player;
import com.bitbus.fantasyclout.team.football.FootballTeam;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class FootballPlayer extends Player {

    @Enumerated(EnumType.STRING)
    private FootballPlayerPosition position;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private FootballTeam team;

}
