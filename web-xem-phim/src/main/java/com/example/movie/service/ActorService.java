package com.example.movie.service;

import com.example.movie.model.Actor;
import com.example.movie.repository.ActorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ActorService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ActorRepository actorRepository;

    // Phương thức lấy diễn viên từ API
//    public void addActorsFromApi(String apiUrl) {
//        // Gửi yêu cầu GET đến API từ URL được cung cấp
//        ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.GET, null, Map.class);
//        Map<String, Object> responseBody = response.getBody();
//
//        if (responseBody != null && responseBody.containsKey("movie")) {
//            Map<String, Object> movie = (Map<String, Object>) responseBody.get("movie");
//            List<String> actorNames = (List<String>) movie.get("actor");
//
//            // Lưu diễn viên vào cơ sở dữ liệu
//            for (String actorName : actorNames) {
//                // Kiểm tra xem tên diễn viên đã tồn tại trong cơ sở dữ liệu chưa
//                Optional<Actor> existingActor = actorRepository.findByActorName(actorName);
//                if (!existingActor.isPresent()) {
//                    // Nếu chưa có, tạo mới và lưu vào cơ sở dữ liệu
//                    Actor actor = new Actor();
//                    actor.setActorName(actorName);
//                    actorRepository.save(actor);
//                }
//            }
//        } else {
//            throw new RuntimeException("Không thể lấy thông tin diễn viên từ API.");
//        }
//    }

}
