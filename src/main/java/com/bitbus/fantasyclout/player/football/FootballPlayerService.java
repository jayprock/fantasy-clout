package com.bitbus.fantasyclout.player.football;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitbus.fantasyclout.team.football.FootballTeam;
import com.bitbus.fantasyclout.team.football.FootballTeamService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FootballPlayerService {

    @Getter
    @Setter
    @Autowired
    private FootballTeamService teamService;

    @Getter
    @Setter
    @Autowired
    private FootballPlayerRepository playerRepo;


    public List<FootballPlayer> findAll() {
        return playerRepo.findAll();
    }

    @Transactional
    public void create(List<FootballPlayerDTO> playerDTOs) {
        List<FootballPlayer> footballPlayers = new ArrayList<>();
        List<FootballPlayer> existingPlayers = findAll();
        List<FootballTeam> teams = teamService.findAll();
        log.info("Attempting to create {} players", playerDTOs.size());
        for (FootballPlayerDTO playerDTO : playerDTOs) {
            FootballTeam team = teams.stream() //
                    .filter(tm -> tm.getAbbreviation().equals(playerDTO.getTeamAbbr())) //
                    .findFirst() //
                    .orElseThrow(() -> new RuntimeException(
                            "There is no team associated with abbreviation: " + playerDTO.getTeamAbbr()));
            FootballPlayerPosition position = FootballPlayerPosition.valueOf(playerDTO.getPosition());

            boolean playerAlreadyExists = existingPlayers.stream() //
                    .anyMatch(existingPlayer -> existingPlayer.getName().equals(playerDTO.getName())
                            && existingPlayer.getPosition() == position && existingPlayer.getTeam().equals(team));
            if (playerAlreadyExists) {
                log.warn("Already found player associated with {}:{}:{}", playerDTO.getName(), playerDTO.getPosition(),
                        playerDTO.getTeamAbbr());
                continue;
            }
            FootballPlayer footballPlayer = new FootballPlayer();
            footballPlayer.setName(playerDTO.getName());
            footballPlayer.setTeam(team);
            footballPlayer.setPosition(position);
            footballPlayers.add(footballPlayer);
        }
        playerRepo.saveAll(footballPlayers);
        log.info("Created {} players from {} player DTOs", footballPlayers.size(), playerDTOs.size());
    }
}
