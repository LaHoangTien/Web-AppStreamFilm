package com.example.movie.controller;


import com.example.movie.model.Country;
import com.example.movie.model.User;
import com.example.movie.repository.CountryRepository;
import com.example.movie.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/countries")
public class CountryController {

    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private IUserRepository userRepository;
    @GetMapping
    public String listCountries(@AuthenticationPrincipal UserDetails userDetails, Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 10; // Số quốc gia mỗi trang
        Page<Country> countryPage = countryRepository.findAll(PageRequest.of(page, pageSize));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("countryPage", countryPage);
        return "country/list"; // Trả về trang danh sách quốc gia
    }


    @GetMapping("/add")
    public String showAddCountryForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("country", new Country());
        return "country/add"; // Trang HTML cho thêm quốc gia
    }

    @PostMapping("/add")
    public String addCountry(@ModelAttribute Country country) {
        countryRepository.save(country);
        return "redirect:/countries";
    }

    @GetMapping("/edit/{id}")
    public String showEditCountryForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, Model model) {
        Country country = countryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quốc gia không tồn tại"));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("country", country);
        return "country/edit"; // Trang HTML cho sửa quốc gia
    }

    @PostMapping("/edit/{id}")
    public String editCountry(@PathVariable Long id, @ModelAttribute Country country) {
        country.setCountryId(id); // Cập nhật bằng setCountryId
        countryRepository.save(country);
        return "redirect:/countries";
    }


    @GetMapping("/delete/{id}")
    public String deleteCountry(@PathVariable Long id) {
        countryRepository.deleteById(id);
        return "redirect:/countries";
    }
}
