package com.bitbus.fantasyclout.scraper.player;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Profile;

import com.bitbus.fantasyclout.FantasyCloutApplication;
import com.bitbus.fantasyclout.Profiles;
import com.bitbus.fantasyclout.player.football.FootballPlayerDTO;
import com.bitbus.fantasyclout.player.football.FootballPlayerService;
import com.bitbus.fantasyclout.scraper.fantasypros.FantasyProsScraper;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackageClasses = FantasyCloutApplication.class)
@Profile(Profiles.SELENIUM)
@Slf4j
public class FootballPlayerScraperMain {

    @Autowired
    private FantasyProsScraper scraper;

    @Autowired
    private FootballPlayerService playerService;


    public static void main(String[] args) {
        new SpringApplicationBuilder(FootballPlayerScraperMain.class) //
                .profiles(Profiles.SELENIUM) //
                .web(WebApplicationType.NONE) //
                .build() //
                .run(args) //
                .getBean(FootballPlayerScraperMain.class) //
                .load();
    }

    public void load() {
        log.info("Beginning football player scrape");
        List<FootballPlayerDTO> playerDTOs = scraper.getCurrentSeasonFootballPlayers();
        log.info("Done collecting player data. Found {} players", playerDTOs.size());

        log.info("Attemting to create new players");
        playerService.create(playerDTOs);
        log.info("Done creating new players");
    }

}
