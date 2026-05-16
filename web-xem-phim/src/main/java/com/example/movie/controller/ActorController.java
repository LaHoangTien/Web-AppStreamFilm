package com.example.movie.controller;

import com.example.movie.model.Actor;
import com.example.movie.model.Genre;
import com.example.movie.model.User;
import com.example.movie.repository.ActorRepository;
import com.example.movie.repository.IUserRepository;
import com.example.movie.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/actors")
public class ActorController {

    @Autowired
    private ActorRepository actorRepository;
    @Autowired
    private ActorService actorService;
    @Autowired
    private IUserRepository userRepository;
    @GetMapping
    public String listActors(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @RequestParam(value = "search", defaultValue = "") String search,
                             Model model) {
        // Sắp xếp theo id giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("actorId")));

        Page<Actor> actorPage;
        if (search.isEmpty()) {
            actorPage = actorRepository.findAll(pageable); // Nếu không có từ khóa tìm kiếm, lấy tất cả
        } else {
            actorPage = actorRepository.findByActorNameContainingIgnoreCase(search, pageable); // Tìm theo tên diễn viên
        }
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("actorPage", actorPage);
        model.addAttribute("actor", new Actor()); // Thêm đối tượng Actor cho form modal
        return "actors/list"; // Trả về trang danh sách diễn viên
    }


    @GetMapping("/add")
    public String showAddActorForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("actor", new Actor());
        return "actors/add"; // Trả về trang thêm diễn viên
    }

    @PostMapping("/add")
    public String addActor(@ModelAttribute Actor actor,Model model) {
        if (actorRepository.existsByActorName(actor.getActorName())) {
            // Nếu đã tồn tại, hiển thị thông báo lỗi và trả lại trang thêm thể loại
            model.addAttribute("errorMessage", "Diễn viên đã tồn tại!");
            return "actors/add";
        }
        actorRepository.save(actor);
        return "redirect:/actors"; // Chuyển hướng về danh sách diễn viên
    }

    @GetMapping("/edit/{id}")
    public String showEditActorForm(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, Model model) {
        Actor actor = actorRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid actor Id:" + id));
        String profileImage = null;
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                profileImage = user.getProfileImage(); // Lấy đường dẫn ảnh đại diện
            }
        }
        model.addAttribute("profileImage", profileImage);
        model.addAttribute("actor", actor);
        return "actors/edit"; // Trả về trang chỉnh sửa diễn viên
    }

    @PostMapping("/edit/{id}")
    public String updateActor(@PathVariable Long id, @ModelAttribute Actor actor) {
        actor.setActorId(id);
        actorRepository.save(actor);
        return "redirect:/actors"; // Chuyển hướng về danh sách diễn viên
    }

    @GetMapping("/delete/{id}")
    public String deleteActor(@PathVariable Long id) {
        actorRepository.deleteById(id);
        return "redirect:/actors"; // Chuyển hướng về danh sách diễn viên
    }
    @PostMapping("/createQuickly")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createActorQuickly(@RequestBody Actor actor) {
        System.out.println("Actor added: " + actor.getActorName()); // Thêm log kiểm tra
        Actor savedActor = actorRepository.save(actor);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("actorId", savedActor.getActorId());
        response.put("actorName", savedActor.getActorName());
        return ResponseEntity.ok(response);
    }

//    @PostMapping("/add-actors")
//    public String addActorsFromApi(@RequestParam("apiUrl") String apiUrl) {
//        try {
//            // Giả sử bạn có service để thêm diễn viên từ API
//            actorService.addActorsFromApi(apiUrl);
//            return "redirect:/actors?success"; // Redirect đến trang sau khi thêm thành công
//        } catch (Exception e) {
//            return "redirect:/actors?error"; // Redirect khi có lỗi
//        }
//    }

}
