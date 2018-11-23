package com.bitbus.fantasyclout.projection.football;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bitbus.fantasyclout.player.football.FootballPlayer;
import com.bitbus.fantasyclout.player.football.FootballPlayerDTO;
import com.bitbus.fantasyclout.player.football.FootballPlayerPosition;
import com.bitbus.fantasyclout.player.football.FootballPlayerService;
import com.bitbus.fantasyclout.projection.ProjectionSource;
import com.bitbus.fantasyclout.projection.ProjectionType;
import com.bitbus.fantasyclout.projection.SeasonYearEvaluator;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FootballPlayerProjectionService {

    @Autowired
    private FootballPlayerService playerService;

    @Autowired
    private FootballPlayerProjectionRepository projectionRepo;

    @Transactional
    public void saveWeeklyProjections(FootballProjectionDTO footballProjectionDTO) {
        log.debug("Performing initial checks - verifying data received is weekly projection data");
        if (footballProjectionDTO.getProjectionType() != ProjectionType.WEEK) {
            log.error("Unexpected projection type. Expected type WEEK but received {}. No data will be saved.",
                    footballProjectionDTO.getProjectionType());
            return;
        }
        Integer projectionWeek = footballProjectionDTO.getProjectionWeek();
        if (projectionWeek == null || projectionWeek < 1 || projectionWeek > 17) {
            log.error("Invalid projection week. Allowed weeks are 1-17, but received {}. No data will be saved.",
                    projectionWeek);
            return;
        }
        log.debug("Done performing initial checks and proceeding");

        log.debug("Clearing out the existing football projections for week {}, if applicable",
                footballProjectionDTO.getProjectionWeek());
        long recordsDeleted = deleteByProjectionWeek(footballProjectionDTO.getProjectionWeek());
        log.debug("Removed {} existing projection records", recordsDeleted);

        log.debug("Beginning player projection creation process");
        create(footballProjectionDTO, true);

        log.debug("Done saving weekly projections");
    }


    public void create(FootballProjectionDTO footballProjectionDTO, boolean autoCreateMissingPlayer) {
        List<FootballPlayerProjection> projections = new ArrayList<>();
        List<FootballPlayer> players = playerService.findAll();
        for (FootballPlayerProjectionDTO projectionDTO : footballProjectionDTO.getPlayerProjections()) {
            Optional<FootballPlayer> mappedPlayer = players.stream() //
                    .filter(player -> player.getName().equals(projectionDTO.getPlayerName())) //
                    .filter(player -> player.getTeam().getAbbreviation().equals(projectionDTO.getTeamAbbreviation())
                            || player.getPosition() == FootballPlayerPosition.DEF) //
                    .filter(player -> player.getPosition() == projectionDTO.getPosition()) //
                    .findFirst();
            FootballPlayer player;
            if (mappedPlayer.isPresent()) {
                player = mappedPlayer.get();
            } else if (autoCreateMissingPlayer) {
                log.warn("Could not map an existing player to {}:{}:{}. This player will be created.",
                        projectionDTO.getPlayerName(), projectionDTO.getTeamAbbreviation(),
                        projectionDTO.getPosition());

                FootballPlayerDTO playerDTO = new FootballPlayerDTO();
                playerDTO.setName(projectionDTO.getPlayerName());
                playerDTO.setPosition(projectionDTO.getPosition().toString());
                playerDTO.setTeamAbbr(projectionDTO.getTeamAbbreviation());
                player = playerService.create(playerDTO).get();
            } else {
                log.warn("Could not map an existing player to {}:{}:{}. This projection will be skipped.",
                        projectionDTO.getPlayerName(), projectionDTO.getTeamAbbreviation(),
                        projectionDTO.getPosition());
                continue;
            }
            log.debug("Creating projection for player {}", player);
            FootballPlayerProjection playerProjection = create(footballProjectionDTO, projectionDTO, player);
            projections.add(playerProjection);
        }
        log.info("Created {} out of {} projections", projections.size(),
                footballProjectionDTO.getPlayerProjections().size());
    }

    private FootballPlayerProjection create(FootballProjectionDTO footballProjectionDTO,
            FootballPlayerProjectionDTO projectionDTO, FootballPlayer mappedPlayer) {
        FootballPlayerProjection playerProjection = new FootballPlayerProjection();
        playerProjection.setPlayer(mappedPlayer);
        playerProjection.setProjectionType(footballProjectionDTO.getProjectionType());
        playerProjection.setProjectionWeek(footballProjectionDTO.getProjectionWeek());
        playerProjection.setProjectionTime(LocalDateTime.now());
        playerProjection.setSeasonYear(
                SeasonYearEvaluator.getFootballSeasonYear(playerProjection.getProjectionTime().toLocalDate()));
        playerProjection.setSource(new ProjectionSource(footballProjectionDTO.getSource()));
        playerProjection.setPassingYards(projectionDTO.getPassingYards());
        playerProjection.setPassingTds(projectionDTO.getPassingTds());
        playerProjection.setInterceptions(projectionDTO.getInterceptions());
        playerProjection.setRushingYards(projectionDTO.getRushingYards());
        playerProjection.setRushingTds(projectionDTO.getRushingTds());
        playerProjection.setFumblesLost(projectionDTO.getFumblesLost());
        playerProjection.setReceptions(projectionDTO.getReceptions());
        playerProjection.setReceivingYards(projectionDTO.getReceivingYards());
        playerProjection.setReceivingTds(projectionDTO.getReceivingTds());
        playerProjection.setFieldGoalsMade(projectionDTO.getFieldGoalsMade());
        playerProjection.setExtraPointsMade(projectionDTO.getExtraPointsMade());
        playerProjection.setDefensiveSacks(projectionDTO.getDefensiveSacks());
        playerProjection.setDefensiveFumblesRecovered(projectionDTO.getDefensiveFumblesRecovered());
        playerProjection.setDefensiveInterceptions(projectionDTO.getDefensiveInterceptions());
        playerProjection.setDefensiveSafeties(projectionDTO.getDefensiveSafeties());
        playerProjection.setDefensiveTds(projectionDTO.getDefensiveTds());
        playerProjection.setDefensivePointsAllowed(projectionDTO.getDefensivePointsAllowed());
        return projectionRepo.save(playerProjection);
    }

    public long deleteByProjectionWeek(int projectionWeek) {
        int seasonYear = SeasonYearEvaluator.getFootballSeasonYear(LocalDate.now());
        return projectionRepo.deleteByProjectionWeekAndSeasonYear(projectionWeek, seasonYear);
    }
}
