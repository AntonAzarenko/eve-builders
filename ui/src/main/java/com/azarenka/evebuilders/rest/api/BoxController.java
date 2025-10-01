package com.azarenka.evebuilders.rest.api;

import com.azarenka.evebuilders.domain.casino.Box;
import com.azarenka.evebuilders.domain.casino.Reward;
import com.azarenka.evebuilders.domain.casino.dto.UserInfo;
import com.azarenka.evebuilders.service.casino.BoxService;
import com.azarenka.evebuilders.service.casino.CasinoUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/casino/box")
@CrossOrigin(origins = "*")
public class BoxController {

    @Autowired
    private CasinoUserService userService;
    @Autowired
    private BoxService boxService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Box>> getByUserIdRewards(@RequestParam Integer characterId) {
        if (Objects.isNull(characterId)) {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(userService.getRewardsByCharacterId(characterId));
    }

    @GetMapping(path = "/all_available", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserInfo>> getByUsersRewards() {
        return ResponseEntity.ok(userService.getAvailableRewards());
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reward> updateStatusBox(@RequestParam String boxId, @RequestParam boolean status) {
        boxService.update(boxId, status);
        return ResponseEntity.ok().body(null);
    }
}
