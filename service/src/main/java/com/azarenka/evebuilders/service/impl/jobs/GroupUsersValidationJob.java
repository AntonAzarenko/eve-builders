package com.azarenka.evebuilders.service.impl.jobs;

import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.AuthIntegrationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GroupUsersValidationJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroupUsersValidationJob.class);

    @Autowired
    private IUserService userService;
    @Autowired
    private IUserTokenService userTokenService;
    @Autowired
    private AuthIntegrationService authIntegrationService;

    @Scheduled(cron = "0 0 0 * * 7")
    public void checkUsers() {
        var usersDto = userService.getUsersDto();
        LOGGER.info("Checking group users. START. UsersCount={}", usersDto.size());
        AtomicInteger removedCount = new AtomicInteger();
        usersDto.forEach(userDto -> {
            var username = userDto.getUsername();
            var authenticated = authIntegrationService.checkUser(username);
            if (!authenticated) {
                userService.getByUsername(username).ifPresent(user -> {
                    userTokenService.delete(user.getUid());
                    LOGGER.info("Token for user [{}] was removed", username);
                    removedCount.getAndIncrement();
                });
            }
        });
        LOGGER.info("Checking group users. FINISHED. UsersRemoved={}", removedCount.get());
    }
}
