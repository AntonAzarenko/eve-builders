package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.db.Role;
import com.azarenka.evebuilders.domain.db.User;
import com.azarenka.evebuilders.domain.dto.UserDto;
import com.azarenka.evebuilders.repository.database.IUserRepository;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IUserTokenService userTokenService;

    @Override
    public Optional<User> getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public String getCharacterId() {
        Optional<User> userOptional = userRepository.findByUsername(SecurityUtils.getUserName());
        return userOptional.map(User::getCharacterId).orElse(null);
    }

    @Override
    public String getUserToken() {
        String userId = userRepository.findByUsername(SecurityUtils.getUserName()).get().getUid();
        return userTokenService.getUserToken(userId);
    }

    @Override
    public void updateLanguage(String language) {
        Optional<User> userOptional = userRepository.findByUsername(SecurityUtils.getUserName());
        userOptional.ifPresent(user -> {
            user.setLanguage(language);
            userRepository.save(user);
        });
    }

    @Override
    public List<UserDto> getUsersDto() {
        List<User> all = userRepository.findAll();
        return all.stream()
            .map(user -> new UserDto(user.getUsername(), user.getCharacterId(), user.getRoles()))
            .toList();
    }

    @Override
    public void updateUserRoles(UserDto userDto, Set<Role> selectedRoles) {
        Optional<User> userOptional = userRepository.findByUsername(userDto.getUsername());
        userOptional.ifPresent(user -> {
            user.setRoles(selectedRoles);
            userRepository.save(user);
        });
    }
}
