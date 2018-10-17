package com.bitbus.fantasyclout.team.football;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import com.bitbus.fantasyclout.player.football.FootballPlayer;
import com.bitbus.fantasyclout.team.Team;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FootballTeam extends Team {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
    private List<FootballPlayer> players;

}
