package com.example.movie.service;

import com.example.movie.model.Role;
import com.example.movie.model.User;
import com.example.movie.repository.IRoleRepository;
import com.example.movie.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Collections;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final IRoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, IRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Tạo đối tượng DefaultOAuth2UserService để xử lý load thông tin người dùng
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // Lấy các thuộc tính của người dùng
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email;
        String name = (String) attributes.get("name");

        // Nếu là Google, lấy email trực tiếp
        if (registrationId.equals("google")) {
            email = (String) attributes.get("email");
        } else if (registrationId.equals("facebook")) {
            // Tạo email giả định cho Facebook, ví dụ: dùng ID của Facebook
            String facebookId = (String) attributes.get("id");
            email = "facebook_" + facebookId + "@example.com";
        } else {
            throw new IllegalArgumentException("Provider không được hỗ trợ: " + registrationId);
        }
        // Kiểm tra xem người dùng đã có trong cơ sở dữ liệu chưa
        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();  // Nếu người dùng đã có, lấy thông tin người dùng
        } else {
            // Nếu chưa có, tạo người dùng mới
            user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setFullName(name);
            user.setProvider(registrationId);  // Bạn có thể thêm tên provider nếu cần
            user.setPassword("");
            Role userRole = roleRepository.findByName("USER");
            if (userRole == null) {
                // Nếu vai trò USER chưa có, tạo mới
                userRole = new Role();
                userRole.setName("USER");
                userRole.setDescription("Regular user with limited access");
                roleRepository.save(userRole);
            }

            user.setRoles(Collections.singleton(userRole));  // Gán vai trò cho người dùng
            userRepository.save(user);  // Lưu vào cơ sở dữ liệu
        }

        // Trả về đối tượng OAuth2User với quyền hạn
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),  // Gán quyền mặc định cho người dùng
                attributes,
                "name");  // Trường "name" là thuộc tính chính trong OAuth2User
    }
}
