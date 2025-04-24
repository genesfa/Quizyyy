
package com.quiz.com.quiz;

import com.quiz.com.quiz.entitys.Team;
import com.quiz.com.quiz.repositorys.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:9999")
public class TeamController {
    private final TeamRepository teamRepository;

    @PostMapping
    public Team createTeam(@RequestBody Team team) {
        return teamRepository.save(team);
    }
}