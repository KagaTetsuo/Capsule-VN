# Lịch sử phiên bản Capsule VN

## v1.2.0 (2026) — Mô phỏng Đàn hồi & Chất lỏng Trực tiếp, Tùy chỉnh Bảng trạng thái mở rộng & Tối ưu giao diện khoa học
- **Cập nhật Logo & Icon ứng dụng mới**:
  - Áp dụng logo Viên thuốc Vàng 3D dập nổi nghệ thuật kèm Ngôi sao vàng trên nền khói đỏ đen huyền bí.
  - Tối ưu vùng an toàn (Safe Zone) chuẩn mực cho Adaptive Icon, bo tròn mượt mà trên Samsung One UI, HyperOS, ColorOS, Pixel Launcher mà không bị mất góc hay lẹm viền.
- **Tùy chỉnh Vị trí & Kích thước Bảng trạng thái hoạt động khi mở rộng**:
  - Bổ sung tùy biến độc lập cho Bảng mở rộng (Expanded Status Island: Đồng hồ, Pin, Âm lịch, Gaming HUD nhiệt độ/RAM/ROM, Phím tắt Hẹn giờ & Đèn pin).
  - Tùy chỉnh vị trí trên / dưới (Dọc `expandedOffsetY`), Độ rộng bề ngang (`expandedWidth`: tự động theo màn hình hoặc 260..460 dp), Chiều cao thêm (`expandedHeightExtra`), và Độ bo góc bung mở rộng (`expandedCornerRadius`: 12..50 dp).
  - Tích hợp mô phỏng xem trước trực tiếp ngay trên đỉnh màn hình khi kéo thanh trượt.
- **Mô phỏng Chuyển động Đàn hồi & Mở rộng dạng Chất lỏng (Live Sandbox)**:
  - Bổ sung Thẻ mô phỏng trực tiếp tương tác 1 chạm `MotionElasticitySimulationCard` trong cài đặt Mở rộng & Độ đàn hồi.
  - Cho phép người dùng chạm thử nghiệm độ nảy đàn hồi (Spring Test) và cảm nhận trực quan từng mức độ nảy: Không đàn hồi (1.0), Đàn hồi nhẹ (0.8), Đàn hồi vừa (0.62), Đàn hồi mạnh (0.48), cùng hiệu ứng kính 3D Liquid Glass và tán sắc.
  - Tự động kích hoạt hiệu ứng bung nảy trên cả widget mô phỏng lẫn Viên thuốc Dynamic Island thật trên đỉnh màn hình thiết bị.
- **Khắc phục lỗi che đè nút bấm trong Khung tùy chỉnh thẻ**:
  - Loại bỏ hoàn toàn khung mô phỏng trực tiếp thừa bên dưới theo yêu cầu người dùng, tiết kiệm không gian màn hình.
  - Bố trí khoảng cách đệm trên an toàn (130 dp) để thẻ xem trước nổi trên đỉnh không còn che khuất các tab "Thẻ tin nhắn", "Cuộc gọi đến", "Cập nhật".
  - Chuyển cửa sổ xem trước sang chế độ xuyên thấu cảm ứng (touch-transparent) khi đang chỉnh sửa, đảm bảo 100% không bao giờ chặn hay cướp thao tác bấm tab/nút bấm của người dùng.
  - Thêm nút công tắc mắt bật/tắt chế độ xem trước trên đỉnh màn hình linh hoạt.
- **Tái cấu trúc giao diện khoa học, tinh gọn & dễ hiểu**:
  - Gom nhóm các mục cài đặt mạch lạc, đặt tên tiêu đề trực quan, loại bỏ các mục trùng lặp:
    1. **Nội dung hiển thị** (Thẻ đa nhiệm, Tin nhắn & Thông báo, Sự kiện & Sạc pin, Lời bài hát Karaoke).
    2. **Tiện ích thông minh** (Smart OTP 1 chạm, Đọc thông báo qua Tai nghe, Chế độ Gaming & HUD, Hệ sinh thái Samsung).
    3. **Thao tác & Điều khiển** (Cử chỉ chạm/vuốt, Hành vi hiển thị & Thu nhỏ, Tắt biểu ngữ nổi hệ thống).
    4. **Hệ thống** (Ngôn ngữ hiển thị, Khôi phục cài đặt gốc).

## v1.1.1 (2026) — Tùy biến Bo góc Toàn diện & Bộ 20 Thử nghiệm Tính năng Đột phá
- **Tùy biến độ bo góc cho tất cả các mục của Viên thuốc**:
  - Tùy chỉnh độ bo góc **Viên thuốc thu gọn (Nhàn rỗi)** (`capsuleCornerRadius`: từ bo vuông 0dp đến bo tròn tự động `h/2`).
  - Tùy chỉnh độ bo góc **Viên thuốc khi mở rộng** (`expandedCornerRadius`: 12..50 dp).
  - Tùy chỉnh riêng độ bo góc cho từng loại thẻ: **Thẻ tin nhắn** (`cardCornerRadius`), **Thẻ cuộc gọi đến** (`callCornerRadius`), **Thẻ cập nhật** (`updateCornerRadius`).
- **Mục Thử nghiệm toàn diện (Bộ 20 tính năng tương tác trực tiếp)**:
  - Bổ sung thử nghiệm 1 chạm trực tiếp trên Viên thuốc cho toàn bộ 20 tính năng cốt lõi và nâng cao:
    1. Thẻ tin nhắn Zalo kèm nút Trả lời / Đã đọc
    2. Nhận diện mã OTP & Nút Chép, Điền nhanh 1 chạm
    3. Đọc to thông báo qua Tai nghe (TTS Tiếng Việt chuẩn tự nhiên)
    4. Thẻ cuộc gọi đến (Nghe / Từ chối)
    5. Trình phát nhạc đĩa xoay & Sóng Equalizer động
    6. Trứng phục sinh Cờ Đỏ Sao Vàng "Tuổi Trẻ Thế Hệ Bác Hồ - Tùng Dương"
    7. Chế độ Gaming thu gọn & Bảng theo dõi Gaming HUD (°C, RAM, ROM)
    8. Vòng sáng LED Viên thuốc RGB 360° quanh camera
    9. Bút cảm ứng Samsung S Pen (hiệu ứng rút / cất bút)
    10. Sạc siêu nhanh 2.0 (Super Fast Charging 45W) kèm nhiệt độ °C
    11. Cảnh báo Bảo vệ Pin chống chai (ngừng 85%)
    12. Tai nghe Galaxy Buds3 Pro / Bluetooth kèm vạch pin
    13. Samsung DeX / Màn hình rời Smart View
    14. Hẹn giờ đếm ngược
    15. Báo thức sáng thức dậy
    16. Thanh toán chạm NFC (Đã chuẩn hóa tiền Việt 150.000 đ)
    17. Bản đồ & Điều hướng rẽ trực tiếp
    18. Thẻ cập nhật phần mềm Capsule VN
    19. Chế độ Chuông / Im lặng rung lắc
    20. Đèn pin bật / tắt
- **Tối ưu thanh thông số hệ thống (Gaming HUD)**:
  - Loại bỏ hoàn toàn tiền tố chữ RAM và ROM, khóa cứng hiển thị trên 1 hàng ngang duy nhất (`maxLines = 1`, `softWrap = false`), triệt tiêu hoàn toàn hiện tượng rớt dòng.

## v1.1.0 (2026) — Nâng cấp Icon Vàng 3D, Bo góc & Tùy biến Thẻ Cập nhật
- **Biểu tượng ứng dụng mới (Capsule VN Gold 3D & Ngôi sao)**: Thiết kế logo Capsule VN chữ vàng dập nổi 3D kèm ngôi sao năm cánh trên nền khói đỏ/đen huyền bí sang trọng. Tối ưu vùng an toàn (Safe Zone) chuẩn Adaptive Icon chống khuất góc trên mọi Launcher.
- **Tùy chỉnh độ bo góc khung thông báo**: Thêm thanh trượt tùy biến độ bo góc (`8 dp` đến `44 dp`) áp dụng đồng bộ cho các thẻ thông báo, tin nhắn, cuộc gọi và cập nhật.
- **Tùy biến vị trí & kích thước Thẻ cập nhật**: Bổ sung đầy đủ tùy chỉnh vị trí trên/dưới (`offsetY`), bề ngang (`width`) và bề dọc (`heightExtra`) cho Thẻ cập nhật phần mềm tương tự thẻ tin nhắn và cuộc gọi đến.
- **Xem trước trực tiếp (Live Simulation Preview) 3 chế độ**: Mô phỏng ngay tức thì cả 3 thẻ (Tin nhắn, Cuộc gọi, Cập nhật) ở cả màn hình cài đặt và trên Viên thuốc thực tế.
- **Tối ưu thông tin phiên bản & Hệ thống kiểm tra cập nhật**: Chuẩn hóa thông tin hiển thị và kiểm tra phát hành trực tiếp từ kho lưu trữ GitHub chính thức `KagaTetsuo/Capsule-VN`.

## v1.0.0 (2026) — Phiên bản khởi đầu chính thức của Capsule VN
- Giao diện giả lập Dynamic Island đầu tiên tối ưu chuyên sâu cho tiếng Việt và các dòng máy Android tại Việt Nam.
