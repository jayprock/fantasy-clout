package com.bitbus.fantasyclout.projection.football;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FootballPlayerProjectionRepository extends JpaRepository<FootballPlayerProjection, Long> {

    long deleteByProjectionWeekAndSeasonYear(int projectionWeek, int seasonYear);

}
