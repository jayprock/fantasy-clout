package com.bitbus.fantasyclout.player.football;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Optional<FootballPlayer> create(FootballPlayerDTO playerDTO) {
        List<FootballPlayer> existingPlayers = findAll();
        List<FootballTeam> teams = teamService.findAll();
        return create(playerDTO, teams, existingPlayers);
    }

    @Transactional
    public void create(List<FootballPlayerDTO> playerDTOs) {
        List<FootballPlayer> footballPlayers = new ArrayList<>();
        List<FootballPlayer> existingPlayers = findAll();
        List<FootballTeam> teams = teamService.findAll();
        log.info("Attempting to create {} players", playerDTOs.size());
        for (FootballPlayerDTO playerDTO : playerDTOs) {
            Optional<FootballPlayer> player = create(playerDTO, teams, existingPlayers);
            if (player.isPresent()) {
                footballPlayers.add(player.get());
            }
        }
        log.info("Created {} players from {} player DTOs", footballPlayers.size(), playerDTOs.size());
    }

    private Optional<FootballPlayer> create(FootballPlayerDTO playerDTO, List<FootballTeam> teams,
            List<FootballPlayer> existingPlayers) {
        FootballTeam team = teams.stream() //
                .filter(tm -> tm.getAbbreviation().equals(playerDTO.getTeamAbbr())) //
                .findFirst() //
                .orElseThrow(() -> new RuntimeException(
                        "There is no team associated with abbreviation: " + playerDTO.getTeamAbbr()));
        FootballPlayerPosition position = FootballPlayerPosition.valueOf(playerDTO.getPosition());

        Optional<FootballPlayer> existingPlayer = existingPlayers.stream() //
                .filter(player -> player.getName().equals(playerDTO.getName())) //
                .filter(player -> player.getPosition() == position) //
                .filter(player -> player.getTeam().equals(team)) //
                .findAny();
        if (existingPlayer.isPresent()) {
            log.warn("Player {} already exists and will not be saved", existingPlayer.get());
            return Optional.empty();
        }

        FootballPlayer footballPlayer = new FootballPlayer();
        footballPlayer.setName((position != FootballPlayerPosition.DEF) ? playerDTO.getName() : team.getName());
        footballPlayer.setTeam(team);
        footballPlayer.setPosition(position);

        FootballPlayer player = playerRepo.save(footballPlayer);
        return Optional.of(player);
    }
}
