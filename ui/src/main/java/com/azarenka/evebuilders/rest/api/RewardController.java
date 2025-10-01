package com.azarenka.evebuilders.rest.api;

import com.azarenka.evebuilders.domain.casino.Reward;
import com.azarenka.evebuilders.service.casino.RewardService;
import com.azarenka.evebuilders.validators.api.RewardValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/casino/reward")
@CrossOrigin(origins = "*")
public class RewardController {

    @Autowired
    private RewardService rewardService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Reward>> getRewards() {
        return ResponseEntity.ok(rewardService.getRewards());
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reward> saveReward(@RequestBody Reward reward) {
        RewardValidator.validateForSave(reward);
        return ResponseEntity.ok(rewardService.save(reward));
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reward> updateReward(@RequestBody Reward reward) {
        RewardValidator.validateForSave(reward);
        return ResponseEntity.ok(rewardService.save(reward));
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Reward> deleteReward(@RequestParam String rewardId) {
        if(rewardId == null) {
            return ResponseEntity.badRequest().build();
        }
        int rows = rewardService.remove(rewardId);
        return (rows == 0) ? ResponseEntity.notFound().build()
            : ResponseEntity.noContent().build();
    }
}
