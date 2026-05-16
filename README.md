# 🎬 WebFilm - Hệ Thống Xem Phim Trực Tuyến Đa Nền Tảng

Dự án cá nhân xây dựng hệ thống xem phim trực tuyến hoàn chỉnh (Full-Stack), cung cấp trải nghiệm giải trí đồng bộ trên cả nền tảng Web và Ứng dụng di động (Android), được quản lý bởi một hệ thống Backend tập trung.

---

## 📁 Cấu trúc dự án (Monorepo)

Dự án được tổ chức theo mô hình **Monorepo** giúp dễ dàng quản lý toàn bộ mã nguồn của hệ thống trong một kho lưu trữ duy nhất:

*   **`web-xem-phim/`**: Bao gồm mã nguồn Backend (Java Spring Boot) xử lý toàn bộ logic nghiệp vụ, cơ sở dữ liệu, API, và đồng thời chứa giao diện Web Front-End (dành cho cả người dùng xem phim trực tuyến và quản trị viên quản lý hệ thống).
*   **`app-xem-phim/`**: Mã nguồn ứng dụng di động (Android Client) giao tiếp với Backend qua RESTful API để phục vụ người dùng xem phim trực tiếp trên điện thoại.

---


## ✨ Các tính năng cốt lõi & Trải nghiệm đa nền tảng

### 1. Phân hệ Web (Web Platform)
*   **Trải nghiệm người dùng:** Giao diện Web được thiết kế tối ưu, cho phép người dùng cuối duyệt phim, xem thông tin chi tiết, đánh giá và phát video stream trực tuyến (Streaming) với tốc độ cao, độ trễ thấp trên trình duyệt máy tính/điện thoại.
*   **Trang quản trị (Admin Dashboard):** Không gian quản lý riêng biệt dành cho Admin để kiểm soát toàn bộ hệ thống (Thêm/Sửa/Xóa phim, quản lý danh mục, duyệt tài khoản người dùng).

### 2. Phân hệ Di động (Android App)
*   Cung cấp ứng dụng native mượt mà dành cho hệ điều hành Android, đồng bộ dữ liệu thời gian thực với hệ thống Web thông qua các RESTful API.
*   Hỗ trợ đầy đủ các tính năng xem phim, tìm kiếm nâng cao, và quản lý trang cá nhân như trên môi trường Web.

### 3. Các tính năng hệ thống cốt lõi
*   **Xác thực bảo mật:** Đăng ký, đăng nhập hệ thống, khôi phục mật khẩu qua Gmail OTP, và đăng nhập nhanh bằng tài khoản Google/Facebook.
*   **Tích hợp luồng phim (Third-party Streaming):** Hệ thống tối ưu hóa việc gọi API từ dịch vụ bên thứ ba để lấy luồng phát (Stream URL). Quản trị viên chỉ cần chọn phim, hệ thống sẽ tự động lưu thông tin cấu hình vào cơ sở dữ liệu MySQL và nhúng (embed) luồng phát trực tiếp lên giao diện người dùng mà không cần lưu trữ file video gốc, giúp tiết kiệm tài nguyên băng thông.
*   **Tìm kiếm thông minh:** Tích hợp Elasticsearch giúp tìm kiếm tên phim, thể loại, diễn viên ngay lập tức với cơ chế tìm kiếm mờ (Fuzzy Search).

---

## 🔒 Lưu ý về An toàn Bảo mật (Security Notice)
Toàn bộ thông tin nhạy cảm bao gồm *Database Password, Gmail App Password, Google/Facebook Client Secret* trong mã nguồn đẩy lên GitHub đã được **ẩn và thay thế bằng các chuỗi ký tự mẫu**. Trong môi trường sản phẩm thực tế (Production), các thông tin này được cấu hình an toàn thông qua biến môi trường hệ thống (Environment Variables) để đảm bảo an toàn tuyệt đối.
