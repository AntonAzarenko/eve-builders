package com.azarenka.evebuilders.service.impl;

import com.azarenka.evebuilders.domain.mysql.User;
import com.azarenka.evebuilders.repository.mysql.IUserRepository;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.api.IUserTokenService;
import com.azarenka.evebuilders.service.impl.auth.SecurityUtils;
import com.azarenka.evebuilders.service.impl.auth.UserDetailServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private UserDetailServiceImpl userDetailsService;
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
    public void authenticateUser(User user, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
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
}
