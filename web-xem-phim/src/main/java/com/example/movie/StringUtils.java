package com.example.movie;

import java.util.*;

public class StringUtils {

    // Bảng ánh xạ ký tự có dấu thành không dấu
    private static final Map<Character, Character> VIETNAMESE_CHAR_MAP = new HashMap<>();

    static {
        // Khởi tạo bảng ánh xạ cho các ký tự tiếng Việt có dấu
        String vietnamese = "áàạảãâấầậẩẫăắằặẳẵéèẹẻẽêếềệểễóòọỏõôốồộổỗơớờợởỡúùụủũưứừựửữíìịỉĩýỳỵỷỹđ";
        String noDiacritics = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeooooooooooooooooouuuuuuuuuuuiiiiiyyyyyd";

        // Kiểm tra độ dài chuỗi trước khi ánh xạ
        if (vietnamese.length() != noDiacritics.length()) {
            throw new IllegalStateException("Độ dài của chuỗi vietnamese và noDiacritics không khớp!");
        }

        // Tạo bảng ánh xạ từ các ký tự có dấu sang không dấu
        for (int i = 0; i < vietnamese.length(); i++) {
            VIETNAMESE_CHAR_MAP.put(vietnamese.charAt(i), noDiacritics.charAt(i));
            VIETNAMESE_CHAR_MAP.put(Character.toUpperCase(vietnamese.charAt(i)), Character.toUpperCase(noDiacritics.charAt(i)));
        }
    }

    // Phương thức loại bỏ dấu tiếng Việt
    public static String removeDiacritics(String text) {
        if (text == null) return null;
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            // Nếu ký tự có dấu, thay thế bằng ký tự không dấu
            if (VIETNAMESE_CHAR_MAP.containsKey(c)) {
                result.append(VIETNAMESE_CHAR_MAP.get(c));
            } else {
                result.append(c);  // Giữ nguyên ký tự không có dấu
            }
        }
        return result.toString();
    }

    public static boolean isSimilar(String query, String keyword) {
        // Loại bỏ dấu và chuyển thành chữ thường
        String normalizedQuery = removeDiacritics(query).toLowerCase();
        String normalizedKeyword = removeDiacritics(keyword).toLowerCase();

        // Tách các từ thành danh sách
        Set<String> queryWords = new HashSet<>(Arrays.asList(normalizedQuery.split("\\s+")));
        Set<String> keywordWords = new HashSet<>(Arrays.asList(normalizedKeyword.split("\\s+")));

        // Tính giao và hợp của hai tập hợp
        Set<String> intersection = new HashSet<>(queryWords);
        intersection.retainAll(keywordWords); // Giao tập

        Set<String> union = new HashSet<>(queryWords);
        union.addAll(keywordWords); // Hợp tập

        // Tính Jaccard Similarity
        double similarity = (double) intersection.size() / union.size();

        // Ngưỡng độ tương đồng (tùy chỉnh, ví dụ: 0.5)
        double threshold = 0.5;

        return similarity >= threshold;
    }
}
