package com.bitbus.fantasyclout.team.football;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

@Service
public class FootballTeamService {

    @Getter
    @Setter
    @Autowired
    private FootballTeamRepository footballTeamRepo;


    public List<FootballTeam> findAll() {
        return footballTeamRepo.findAll();
    }
}
