package com.example.movie.service;

import com.example.movie.repository.WatchHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

@Service
public class WatchHistoryService {
    private final WatchHistoryRepository watchHistoryRepository;

    public WatchHistoryService(WatchHistoryRepository watchHistoryRepository) {
        this.watchHistoryRepository = watchHistoryRepository;
    }
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanUpOldHistories() {
        // Tính ngày 30 ngày trước
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        Date thresholdDate = calendar.getTime();


        // Xóa các bản ghi cũ hơn 30 ngày
        watchHistoryRepository.deleteOldHistories(thresholdDate);
    }

}
