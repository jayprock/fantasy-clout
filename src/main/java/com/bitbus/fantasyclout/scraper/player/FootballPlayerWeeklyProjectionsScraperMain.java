package com.bitbus.fantasyclout.scraper.player;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.bitbus.fantasyclout.FantasyCloutApplication;
import com.bitbus.fantasyclout.Profiles;
import com.bitbus.fantasyclout.projection.football.FootballPlayerProjectionService;
import com.bitbus.fantasyclout.projection.football.FootballProjectionDTO;
import com.bitbus.fantasyclout.scraper.fantasypros.FantasyProsScraper;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackageClasses = FantasyCloutApplication.class)
@Slf4j
public class FootballPlayerWeeklyProjectionsScraperMain {

    @Autowired
    private FantasyProsScraper scraper;

    @Autowired
    private FootballPlayerProjectionService projectionService;


    public static void main(String[] args) {
        new SpringApplicationBuilder(FootballPlayerWeeklyProjectionsScraperMain.class) //
                .profiles(Profiles.SELENIUM) //
                .web(WebApplicationType.NONE) //
                .build() //
                .run(args) //
                .getBean(FootballPlayerWeeklyProjectionsScraperMain.class) //
                .scrape();

    }

    private void scrape() {
        log.info("Beginning Football player weekly projection scrape");
        FootballProjectionDTO footballProjectionDTO = scraper.getFootballPlayerWeeklyProjections();
        log.info("Scraped all projection data, found {} player projections, now attempting to save",
                footballProjectionDTO.getPlayerProjections().size());
        projectionService.saveWeeklyProjections(footballProjectionDTO);
        log.info("Weekly football player projection data save is complete. Done.");
    }

}
