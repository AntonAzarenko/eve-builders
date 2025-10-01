package com.azarenka.evebuilders.rest.api;

import com.azarenka.evebuilders.domain.casino.dto.UserInfo;
import com.azarenka.evebuilders.service.casino.CasinoUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/api/v1/casino")
@CrossOrigin(origins = "*")
public class CasinoUserController {

    @Value("${casino.api-token}")
    private String apiToken;

    @Autowired
    private CasinoUserService userService;

    @GetMapping(path = "/character")
    public ResponseEntity<UserInfo> getByCharacterId(@RequestParam Integer characterId) {
        UserInfo userInfo = userService.getByCharacterId(characterId);
        if (Objects.nonNull(userInfo)) {
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(path = "/characters")
    public ResponseEntity<List<UserInfo>> getByCharacters() {
        List<UserInfo> userInfos = userService.getAvailableRewards();
        if (Objects.nonNull(userInfos)) {
            return ResponseEntity.ok(userInfos);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(path = "/character", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfo> upsertCharacterInfo(@RequestBody UserInfo payload) {
        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required");
        }
        if (payload.getCharacterId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "characterId is required");
        }
        if (payload.getCountPoints() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "points is required");
        }
        UserInfo saved = userService.insertUserInfo(payload);
        return ResponseEntity.status(201).body(saved);
    }
}
