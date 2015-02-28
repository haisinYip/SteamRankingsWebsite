package com.steamrankings.website.controllers;

import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.steamrankings.service.api.games.Games;
import com.steamrankings.service.api.leaderboards.RankEntryByTotalPlayTime;

public class GamesController {
	
	@RequestMapping("/games")
	public String getGamesLeaderboard(Model model) {
		
		
		
		
		//model.addAttribute("rankEntries", rankEntries);
		return "games";
	}
	
	//@RequestMapping("/games")
}
