package com.worldatlas.bot.controller;

import com.worldatlas.bot.entity.User;
import com.worldatlas.bot.repository.UserRepository;
import com.worldatlas.bot.repository.CityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminWebController {
    
    private final UserRepository userRepository;
    private final CityRepository cityRepository;
    
    public AdminWebController(UserRepository userRepository, CityRepository cityRepository) {
        this.userRepository = userRepository;
        this.cityRepository = cityRepository;
    }
    
    @GetMapping
    public String adminPanel(Model model) {
        var users = userRepository.findAll();
        long totalUsers = users.size();
        long totalAdmins = users.stream().filter(u -> u.getRole() == User.Role.ADMIN).count();
        long totalFavorites = users.stream().mapToInt(u -> java.util.Collections.emptyList().size()).sum();
        long totalCities = cityRepository.count();
        
        model.addAttribute("users", users);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalFavorites", totalFavorites);
        model.addAttribute("totalCities", totalCities);
        
        return "admin";
    }
}