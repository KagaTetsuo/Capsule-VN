# Lịch sử phiên bản Capsule VN

## v1.1.1 (2026) — Tùy biến Bo góc Toàn diện & Bộ 20 Thử nghiệm Tính năng Đột phá
- **Tùy biến độ bo góc cho tất cả các mục của Viên thuốc**:
  - Tùy chỉnh độ bo góc **Viên thuốc thu gọn (Nhàn rỗi)** (`capsuleCornerRadius`: từ bo vuông 0dp đến bo tròn tự động `h/2`).
  - Tùy chỉnh độ bo góc **Viên thuốc khi mở rộng** (`expandedCornerRadius`: 12..50 dp).
  - Tùy chỉnh riêng độ bo góc cho từng loại thẻ: **Thẻ tin nhắn** (`cardCornerRadius`), **Thẻ cuộc gọi đến** (`callCornerRadius`), **Thẻ cập nhật** (`updateCornerRadius`).
- **Mục Thử nghiệm toàn diện (Bộ 20 tính năng tương tác trực tiếp)**:
  - Bổ sung thử nghiệm 1 chạm trực tiếp trên Viên thuốc cho toàn bộ 20 tính năng cốt lõi và nâng cao:
    1. 💬 Thẻ tin nhắn Zalo kèm nút Trả lời / Đã đọc
    2. 🔑 Nhận diện mã OTP & Nút Chép, Điền nhanh 1 chạm
    3. 🎧 Đọc to thông báo qua Tai nghe (TTS Tiếng Việt chuẩn tự nhiên)
    4. 📞 Thẻ cuộc gọi đến (Nghe / Từ chối)
    5. 🎵 Trình phát nhạc đĩa xoay & Sóng Equalizer động
    6. 🇻🇳 Trứng phục sinh Cờ Đỏ Sao Vàng "Tuổi Trẻ Thế Hệ Bác Hồ - Tùng Dương"
    7. 🎮 Chế độ Gaming thu gọn & Bảng theo dõi Gaming HUD (°C, RAM, ROM)
    8. 🌈 Vòng sáng LED Viên thuốc RGB 360° quanh camera
    9. 🖊️ Bút cảm ứng Samsung S Pen (hiệu ứng rút / cất bút)
    10. ⚡ Sạc siêu nhanh 2.0 (Super Fast Charging 45W) kèm nhiệt độ °C
    11. 🛡️ Cảnh báo Bảo vệ Pin chống chai (ngưỡng 85%)
    12. 🎧 Tai nghe Galaxy Buds3 Pro / Bluetooth kèm vòng pin
    13. 🖥️ Samsung DeX / Màn hình rời Smart View
    14. ⏳ Hẹn giờ đếm ngược
    15. ⏰ Báo thức sáng thức dậy
    16. 💳 Thanh toán chạm NFC (Đã chuẩn hóa tiền Việt 150.000 đ)
    17. 🗺️ Bản đồ & Điều hướng rẽ trực tiếp
    18. 🔄 Thẻ cập nhật phần mềm Capsule VN
    19. 🔇 Chế độ Chuông / Im lặng rung lắc
    20. 🔦 Đèn pin bật / tắt
- **Tối ưu thanh thông số hệ thống (Gaming HUD)**:
  - Loại bỏ hoàn toàn tiền tố chữ RAM và ROM, khóa cứng hiển thị trên 1 hàng ngang duy nhất (`maxLines = 1`, `softWrap = false`), triệt tiêu hoàn toàn hiện tượng rớt dòng.

## v1.1.0 (2026) — Nâng cấp Icon Vàng 3D, Bo góc & Tùy biến Thẻ Cập nhật
- **Biểu tượng ứng dụng mới (Capsule VN Gold 3D & Ngôi sao)**: Thiết kế logo Capsule VN chữ vàng dập nổi 3D kèm ngôi sao năm cánh trên nền khói đỏ/đen huyền bí sang trọng. Tối ưu vùng an toàn (Safe Zone) chuẩn Adaptive Icon chống khuất góc trên mọi Launcher (Samsung One UI, Pixel...).
- **Tùy chỉnh độ bo góc khung thông báo**: Thêm thanh trượt tùy biến độ bo góc (`8 dp` đến `44 dp`) áp dụng đồng bộ cho các thẻ thông báo, tin nhắn, cuộc gọi và cập nhật.
- **Tùy biến vị trí & kích thước Thẻ cập nhật**: Bổ sung đầy đủ tùy chỉnh vị trí trên/dưới (`offsetY`), bề ngang (`width`) và bề dọc (`heightExtra`) cho Thẻ cập nhật phần mềm tương tự thẻ tin nhắn và cuộc gọi đến.
- **Xem trước trực tiếp (Live Simulation Preview) 3 chế độ**: Mô phỏng ngay tức thì cả 3 thẻ (Tin nhắn, Cuộc gọi, Cập nhật) ở cả màn hình cài đặt và trên Viên thuốc thực tế.
- **Tối ưu thông tin phiên bản & Hệ thống kiểm tra cập nhật**: Chuẩn hóa thông tin hiển thị (bỏ nhãn thử nghiệm) và kiểm tra phát hành trực tiếp từ kho lưu trữ GitHub chính thức `KagaTetsuo/Capsule-VN`.

## v1.0.0 (2026) — Phiên bản khởi đầu chính thức của Capsule VN
- **Định danh độc quyền Capsule VN**: Đổi tên toàn bộ ứng dụng sang **Capsule VN — Viên thuốc thông minh cho Android**, loại bỏ 100% tiếng Nga khỏi toàn bộ ứng dụng và mã nguồn GitHub.
- **Tự động Sao chép & Điền nhanh mã OTP 1 chạm**: Tự động nhận diện mã xác thực 4–8 số từ SMS, ngân hàng (VCB, TCB, MB, ACB, BIDV...), Zalo, Telegram, Gmail; tự chép vào bộ nhớ tạm (Clipboard) và chạm 1 lần để điền thẳng vào ô nhập liệu trên màn hình.
- **Tự động đọc to thông báo qua Tai nghe (TTS Tiếng Việt)**: Đọc to tin nhắn đến và tên người gọi bằng giọng đọc Tiếng Việt chuẩn khi đang đeo tai nghe Bluetooth hoặc tai nghe có dây; tự động hạ nhỏ nhạc nền khi đọc (Audio Ducking).
- **Vòng sáng LED Viên thuốc (Capsule Edge LED)**: Viền ánh sáng LED chuyển động bao quanh camera với các chế độ Cầu vồng RGB 360°, Neon Cyber, Vàng Hoàng Gia hoặc tự đổi theo màu app khi có tin chưa đọc, cắm sạc pin và sự kiện.
- **Chế độ Gaming toàn diện cho mọi máy Android**: Tự nhận diện game, thu gọn thông báo chống che màn hình và bảng **Gaming HUD (°C & RAM)** theo dõi nhiệt độ máy/pin và dung lượng RAM thời gian thực.
- **Tích hợp sâu Hệ sinh thái Samsung One UI & Android**: Hiệu ứng rút/cất bút **S Pen**, phân biệt **Sạc nhanh / Sạc siêu nhanh 2.0** kèm nhiệt độ `°C`, cảnh báo **Bảo vệ pin**, vòng % pin **Galaxy Buds / Watch**, và thông báo **Samsung DeX / Smart View**.
- **Tách riêng tùy chỉnh Thẻ Tin nhắn & Cuộc gọi đến**: Cho phép điều chỉnh độc lập vị trí trên/dưới, bề ngang và bề dọc kèm khung mô phỏng trực tiếp (Live Preview).
- **Thấu kính chất lỏng 3D chuẩn iOS Liquid Glass**: Hiệu ứng khúc xạ ánh sáng viền 3D AGSL và phản quang con quay hồi chuyển theo góc nghiêng máy.
- **Tối ưu camera đục lỗ**: Giảm giới hạn chiều rộng Viên thuốc xuống tối thiểu `15 dp` để ôm khít hoàn hảo camera trước.
