package itc475.dndencountertool.controller;

import itc475.dndencountertool.mapper.EncounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EncounterController {
    @Autowired
    private EncounterRepository encounterRepository;

    @GetMapping("/")
    public String login() {
        return "login";
    }
}
