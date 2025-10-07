package com.azarenka.evebuilders.rest.api;

import com.azarenka.evebuilders.domain.casino.Reward;
import com.azarenka.evebuilders.service.casino.UpdateFleetInfoService;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/casino/job")
@CrossOrigin(origins = "*")
public class JobRestController {

    @Autowired
    private UpdateFleetInfoService fleetInfoService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reward> updateStatusBox() {
        fleetInfoService.updateDailyUserFleetInfo();
        return ResponseEntity.ok().body(null);
    }
}
