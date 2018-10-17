package com.bitbus.fantasyclout.scraper.fantasypros;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.bitbus.fantasyclout.Profiles;
import com.bitbus.fantasyclout.player.football.FootballPlayerDTO;

import lombok.extern.slf4j.Slf4j;

@Component
@Profile(Profiles.SELENIUM)
@Slf4j
public class FantasyProsScraper {

    private static final Map<String, String> TEAM_ABBREVIATION_MAPPER = new HashMap<>();

    @Autowired
    private WebDriver driver;

    @PostConstruct
    private void init() {
        TEAM_ABBREVIATION_MAPPER.put("JAC", "JAX");
    }

    private String getTeamAbbreviation(String abbrv) {
        return Optional.ofNullable(TEAM_ABBREVIATION_MAPPER.get(abbrv)).orElse(abbrv);
    }

    private FootballPlayerDTO parseFootballDefense(String teamName) {
        String[] teamNameParts = teamName.split(" \\(|\\)");
        FootballPlayerDTO dto = new FootballPlayerDTO();
        dto.setName(teamNameParts[0]);
        dto.setTeamAbbr(getTeamAbbreviation(teamNameParts[1]));
        dto.setPosition("DEF");
        return dto;
    }

    private void goToFantasyPros() {
        log.debug("Navigating to FantasyPros home");
        driver.get("https://www.fantasypros.com");
    }

    private void goToNFL() {
        goToFantasyPros();
        log.debug("Navigating to FantasyPros NFL home");
        driver.findElement(By.xpath("//div[@id='nav-top']/nav/div[contains(@class,'top-sport-nfl')]/a")).click();
    }

    public List<FootballPlayerDTO> getCurrentSeasonFootballPlayers() {
        log.info("Attempting to scrape football player data from FantasyPros");
        goToNFL();

        log.debug("Navigating to the expert draft rankings");
        driver.findElement(By.linkText("Expert Rankings")).click();

        log.debug("Scraping player data...");
        List<WebElement> playerRows = driver.findElements(By.className("player-row"));
        List<FootballPlayerDTO> footballPlayers = new ArrayList<>();
        for (WebElement playerRow : playerRows) {
            List<WebElement> footballPlayerCols = playerRow.findElements(By.tagName("td"));
            FootballPlayerDTO playerDTO;
            WebElement nameColumn = footballPlayerCols.get(2);
            String playerName = nameColumn.findElement(By.className("full-name")).getText();
            String fantasyProsAbbrv = nameColumn.findElement(By.tagName("small")).getText();
            if (StringUtils.isEmpty(fantasyProsAbbrv)) {
                log.debug("Found a player [{}] without a team abbreviation, parsing as a DEF", playerName);
                playerDTO = parseFootballDefense(playerName);
            } else if ("FA".equals(fantasyProsAbbrv)) {
                continue;
            } else {
                playerDTO = new FootballPlayerDTO();
                playerDTO.setName(playerName);
                String expectedAbbrv = getTeamAbbreviation(fantasyProsAbbrv);
                playerDTO.setTeamAbbr(expectedAbbrv);
                playerDTO.setPosition(footballPlayerCols.get(3).getText().replaceAll("[0-9]", ""));
            }

            log.debug("Scraped data for the #{} ranked player: {}", footballPlayerCols.get(0).getText(), playerDTO);
            footballPlayers.add(playerDTO);
        }

        log.info("Scraped data for {} players", footballPlayers.size());
        return footballPlayers;
    }
}
