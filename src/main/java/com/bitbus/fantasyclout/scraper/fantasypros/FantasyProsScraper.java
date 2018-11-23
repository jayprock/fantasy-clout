package com.bitbus.fantasyclout.scraper.fantasypros;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.bitbus.fantasyclout.Profiles;
import com.bitbus.fantasyclout.player.football.FootballPlayerDTO;
import com.bitbus.fantasyclout.player.football.FootballPlayerPosition;
import com.bitbus.fantasyclout.projection.ProjectionSourceType;
import com.bitbus.fantasyclout.projection.ProjectionType;
import com.bitbus.fantasyclout.projection.football.FootballPlayerProjectionDTO;
import com.bitbus.fantasyclout.projection.football.FootballProjectionDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile(Profiles.SELENIUM)
@Slf4j
public class FantasyProsScraper {

    private static final Map<String, String> TEAM_ABBREVIATION_MAPPER;

    static {
        TEAM_ABBREVIATION_MAPPER = new HashMap<>();
        TEAM_ABBREVIATION_MAPPER.put("JAC", "JAX");
    }

    @Autowired
    private WebDriver driver;


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

    private void goToNFLInSeasonProjections() {
        goToNFL();
        log.debug("Navigating to FantasyPros In Season Research");
        driver.findElement(By.xpath(
                "//nav/div[contains(@class,'heading')]/a[text()='In-Season Research']/../following-sibling::ul//span[text()='Projections']"))
                .click();
    }

    private void goToPositionalProjections(FootballPlayerPosition position) {
        String label = position == FootballPlayerPosition.DEF ? "DST" : position.toString();
        driver.findElement(By.xpath("//ul[contains(@class,'pills')]/li/a[text()='" + label + "']")).click();
    }

    private List<WebElement> getPlayerProjectionRows() {
        return driver.findElements(By.xpath("//table[@id='data']/tbody/tr"));
    }

    private PlayerColumnData parsePlayerColumnData(WebElement playerColumn) {
        String playerName = playerColumn.findElement(By.className("player-name")).getText();
        String team = playerColumn.getText().substring(playerName.length()).trim();
        String teamAbbrv = getTeamAbbreviation(team);
        return new PlayerColumnData(playerName, teamAbbrv);
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

    public FootballProjectionDTO getFootballPlayerWeeklyProjections() {
        log.info("Attempting to get In-Season weekly football projections on FantasyPros");
        goToNFLInSeasonProjections();

        FootballProjectionDTO projectionDTO = new FootballProjectionDTO();
        projectionDTO.setProjectionType(ProjectionType.WEEK);
        projectionDTO.setSource(ProjectionSourceType.FANTASY_PROS);

        // Parse projection week from page title
        String[] projectionHeaderParts =
                driver.findElement(By.xpath("//div[contains(@class,'primary-heading-subheading')]/h1")).getText().trim()
                        .split(" ");
        int projectionWeek = Integer.valueOf(projectionHeaderParts[projectionHeaderParts.length - 1]);
        projectionDTO.setProjectionWeek(projectionWeek);

        // Parse projection details from table
        List<FootballPlayerProjectionDTO> projections = new ArrayList<>();
        projectionDTO.setPlayerProjections(projections);
        {
            log.debug("Parsing QB projections");
            goToPositionalProjections(FootballPlayerPosition.QB);
            List<WebElement> playerRows = getPlayerProjectionRows();
            for (WebElement playerRow : playerRows) {
                FootballPlayerProjectionDTO projection = new FootballPlayerProjectionDTO();
                projection.setPosition(FootballPlayerPosition.QB);
                List<WebElement> projectionColumns = playerRow.findElements(By.tagName("td"));
                PlayerColumnData playerColumnData = parsePlayerColumnData(projectionColumns.get(0));
                log.trace("Setting projections for player: " + playerColumnData.getPlayerName());
                projection.setPlayerName(playerColumnData.getPlayerName());
                projection.setTeamAbbreviation(playerColumnData.getTeamAbbreviation());
                projection.setPassingYards(new BigDecimal(projectionColumns.get(3).getText()));
                projection.setPassingTds(new BigDecimal(projectionColumns.get(4).getText()));
                projection.setInterceptions(new BigDecimal(projectionColumns.get(5).getText()));
                projection.setRushingYards(new BigDecimal(projectionColumns.get(7).getText()));
                projection.setRushingTds(new BigDecimal(projectionColumns.get(8).getText()));
                projection.setFumblesLost(new BigDecimal(projectionColumns.get(9).getText()));
                projections.add(projection);
            }
        }
        {
            log.debug("Parsing RB projections");
            goToPositionalProjections(FootballPlayerPosition.RB);
            List<WebElement> playerRows = getPlayerProjectionRows();
            for (WebElement playerRow : playerRows) {
                FootballPlayerProjectionDTO projection = new FootballPlayerProjectionDTO();
                projection.setPosition(FootballPlayerPosition.RB);
                List<WebElement> projectionColumns = playerRow.findElements(By.tagName("td"));
                PlayerColumnData playerColumnData = parsePlayerColumnData(projectionColumns.get(0));
                log.trace("Setting projections for player: " + playerColumnData.getPlayerName());
                projection.setPlayerName(playerColumnData.getPlayerName());
                projection.setTeamAbbreviation(playerColumnData.getTeamAbbreviation());
                projection.setRushingYards(new BigDecimal(projectionColumns.get(2).getText()));
                projection.setRushingTds(new BigDecimal(projectionColumns.get(3).getText()));
                projection.setReceptions(new BigDecimal(projectionColumns.get(4).getText()));
                projection.setReceivingYards(new BigDecimal(projectionColumns.get(5).getText()));
                projection.setReceivingTds(new BigDecimal(projectionColumns.get(6).getText()));
                projection.setFumblesLost(new BigDecimal(projectionColumns.get(7).getText()));
                projections.add(projection);
            }
        }
        {
            log.debug("Parsing WR projections");
            goToPositionalProjections(FootballPlayerPosition.WR);
            List<WebElement> playerRows = getPlayerProjectionRows();
            for (WebElement playerRow : playerRows) {
                FootballPlayerProjectionDTO projection = new FootballPlayerProjectionDTO();
                projection.setPosition(FootballPlayerPosition.WR);
                List<WebElement> projectionColumns = playerRow.findElements(By.tagName("td"));
                PlayerColumnData playerColumnData = parsePlayerColumnData(projectionColumns.get(0));
                log.trace("Setting projections for player: " + playerColumnData.getPlayerName());
                projection.setPlayerName(playerColumnData.getPlayerName());
                projection.setTeamAbbreviation(playerColumnData.getTeamAbbreviation());
                projection.setReceptions(new BigDecimal(projectionColumns.get(1).getText()));
                projection.setReceivingYards(new BigDecimal(projectionColumns.get(2).getText()));
                projection.setReceivingTds(new BigDecimal(projectionColumns.get(3).getText()));
                projection.setRushingYards(new BigDecimal(projectionColumns.get(5).getText()));
                projection.setRushingTds(new BigDecimal(projectionColumns.get(6).getText()));
                projection.setFumblesLost(new BigDecimal(projectionColumns.get(7).getText()));
                projections.add(projection);
            }
        }
        {
            log.debug("Parsing TE projections");
            goToPositionalProjections(FootballPlayerPosition.TE);
            List<WebElement> playerRows = getPlayerProjectionRows();
            for (WebElement playerRow : playerRows) {
                FootballPlayerProjectionDTO projection = new FootballPlayerProjectionDTO();
                projection.setPosition(FootballPlayerPosition.TE);
                List<WebElement> projectionColumns = playerRow.findElements(By.tagName("td"));
                PlayerColumnData playerColumnData = parsePlayerColumnData(projectionColumns.get(0));
                log.trace("Setting projections for player: " + playerColumnData.getPlayerName());
                projection.setPlayerName(playerColumnData.getPlayerName());
                projection.setTeamAbbreviation(playerColumnData.getTeamAbbreviation());
                projection.setReceptions(new BigDecimal(projectionColumns.get(1).getText()));
                projection.setReceivingYards(new BigDecimal(projectionColumns.get(2).getText()));
                projection.setReceivingTds(new BigDecimal(projectionColumns.get(3).getText()));
                projection.setFumblesLost(new BigDecimal(projectionColumns.get(4).getText()));
                projections.add(projection);
            }
        }
        {
            log.debug("Parsing K projections");
            goToPositionalProjections(FootballPlayerPosition.K);
            List<WebElement> playerRows = getPlayerProjectionRows();
            for (WebElement playerRow : playerRows) {
                FootballPlayerProjectionDTO projection = new FootballPlayerProjectionDTO();
                projection.setPosition(FootballPlayerPosition.K);
                List<WebElement> projectionColumns = playerRow.findElements(By.tagName("td"));
                PlayerColumnData playerColumnData = parsePlayerColumnData(projectionColumns.get(0));
                log.trace("Setting projections for player: " + playerColumnData.getPlayerName());
                projection.setPlayerName(playerColumnData.getPlayerName());
                projection.setTeamAbbreviation(playerColumnData.getTeamAbbreviation());
                projection.setFieldGoalsMade(new BigDecimal(projectionColumns.get(1).getText()));
                projection.setExtraPointsMade(new BigDecimal(projectionColumns.get(3).getText()));
                projections.add(projection);
            }
        }
        {
            log.debug("Parsing DEF projections");
            goToPositionalProjections(FootballPlayerPosition.DEF);
            List<WebElement> playerRows = getPlayerProjectionRows();
            for (WebElement playerRow : playerRows) {
                FootballPlayerProjectionDTO projection = new FootballPlayerProjectionDTO();
                projection.setPosition(FootballPlayerPosition.DEF);
                List<WebElement> projectionColumns = playerRow.findElements(By.tagName("td"));
                String teamName = projectionColumns.get(0).getText();
                log.trace("Setting projections for defense: " + teamName);
                projection.setPlayerName(teamName);
                projection.setDefensiveSacks(new BigDecimal(projectionColumns.get(1).getText()));
                projection.setDefensiveInterceptions(new BigDecimal(projectionColumns.get(2).getText()));
                projection.setDefensiveFumblesRecovered(new BigDecimal(projectionColumns.get(3).getText()));
                projection.setDefensiveTds(new BigDecimal(projectionColumns.get(5).getText()));
                projection.setDefensiveSafeties(new BigDecimal(projectionColumns.get(6).getText()));
                projection.setDefensivePointsAllowed(new BigDecimal(projectionColumns.get(7).getText()));
                projections.add(projection);
            }
        }
        log.info("Done parsing in-season projection data");

        return projectionDTO;
    }


    @Getter
    @AllArgsConstructor
    private static class PlayerColumnData {
        private final String playerName;
        private final String teamAbbreviation;
    }
}
