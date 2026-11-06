# Lịch sử phiên bản Capsule VN

## v1.5.1 (2026) — Hết đổ chuông sau khi đã nghe máy, thanh tiến trình giao hàng
- **Cuộc gọi không còn hiện trùng / đổ chuông mãi**:
  - Chạm thẻ cuộc gọi đến để mở app: thẻ trên Viên thuốc tự ẩn, app tự lo nghe / từ chối.
  - Đã nghe máy trong app (Zalo, Messenger, Telegram…): Viên thuốc nhận ra ngay qua chế độ âm thanh đàm thoại của Android và chuyển sang "đang gọi", kể cả khi app quên gỡ thông báo cuộc gọi đến.
  - Nhận đúng cuộc gọi đang diễn ra của các app không hiện đồng hồ đếm giờ (chỉ có nút Kết thúc); thông báo "cuộc gọi đến" cũ còn sót được bỏ đi.
- **Tin nhắn không trùng với app**: mở app nhắn tin thì thẻ tin nhắn của chính app đó trên Viên thuốc tự ẩn; đọc / xoá tin trong app hoặc trên thanh thông báo thì thẻ cũng tự gỡ.
- **Thanh tiến trình kiểu Live Activity** cho giao hàng, gọi xe, dẫn đường: vạch tiến trình có biểu tượng app chạy theo, kèm giai đoạn (Đã đặt → Đang chuẩn bị → Đang giao → Sắp đến → Đã giao; Tìm tài xế → Tài xế đang đến → Đang di chuyển…). Lấy từ tiến trình app báo, chữ trong thông báo hoặc thời gian còn lại. Thử ngay: Trang chủ → Thử nghiệm → Đơn giao hàng.
- **Vòng sáng LED mặc định tắt** (giống đường viền); bật lại trong Giao diện → Vòng sáng LED.
- **"Đã gửi / Đã sao chép" tách riêng** khỏi viên nhỏ chế độ chuông: kích thước, vị trí, thời gian hiển thị chỉnh riêng (Giao diện → Tùy chỉnh theo từng nơi → Thông báo viên nhỏ).
- **Nhẹ pin hơn**: đo RAM chỉ khi đang mở rộng / chơi game (không còn thức dậy mỗi 4 giây); chế độ ngân hàng chỉ nghe cú chạm ở màn hình chính; đồng hồ hết hạn của hoạt động ngừng khi tắt màn hình.

## v1.5.0 (2026) — Đường viền theo từng nơi, thời gian hiển thị riêng, tìm kiếm tính năng
- **Tùy chỉnh theo từng nơi** (Giao diện): Viên thuốc bình thường, Khi có hoạt động, Thông báo viên nhỏ, Khung thẻ thông báo, Bảng mở rộng — mỗi nơi có kích thước & vị trí, nền, đường viền và tính năng riêng, xem trước trực tiếp trên đỉnh màn hình.
- **Đường viền chuyên sâu** (mặc định: không viền ở mọi nơi):
  - 16 bảng màu: LED RGB cầu vồng, Theo màu ứng dụng / ảnh bìa, Đơn sắc, Đa sắc 2–4 màu tự chọn, Đen – Đỏ – Vàng, Vàng hoàng gia, Neon, Lửa, Đại dương, Cực quang, Hoàng hôn, Hoa anh đào, Pastel, Băng tuyết, Dạ quang, Cờ đỏ sao vàng.
  - 14 hiệu ứng: Xoay 360°, Xoay đảo chiều, Xoay qua lại, Sao chổi, Hai sao chổi, Loang màu từ đỉnh, Đổi màu toàn viền, Gợn sóng, Nhịp thở, Nhịp tim, Nhấp nháy, Neon chập chờn, Lấp lánh, Đứng yên.
  - Chỉnh độ dày, tốc độ, quầng sáng toả ra, độ đậm; "Dùng viền này cho mọi nơi" chỉ với 1 chạm. Vòng sáng LED dùng chung bộ màu & hiệu ứng mới. Thiết lập viền cũ được giữ nguyên khi cập nhật.
- **Thông báo viên nhỏ** (chế độ chuông, đã gửi…) có kích thước & vị trí riêng: chiều rộng, chiều cao, vị trí dọc / ngang, bo góc.
- **Thời gian hiển thị riêng từng loại**: tin nhắn, cập nhật, sạc, tai nghe, thanh toán, điều hướng, báo thức, viên nhỏ, sự kiện khác — loại nào nhanh, loại nào chậm tuỳ ý (Giao diện → Mở rộng & độ đàn hồi → Thời gian hiển thị).
- **Tìm kiếm tính năng** ở tab Tính năng và Giao diện, gõ không dấu cũng được (vd. "vien", "otp", "trung lap").
- **Sửa lỗi**:
  - Chiều rộng khi có hoạt động: kéo nhỏ giờ thu nhỏ thật (trước bị kẹt ở 100 rồi nhảy về tối đa); về 0 = tự động theo nội dung.
  - Đang chỉnh mà thoát app: bản xem trước không còn treo trên Viên thuốc khi dùng app khác.
  - Chạm mở bảng mở rộng phản hồi ngay (bỏ chờ chạm đúp mặc định); đồng hồ bảng mở rộng có thêm giây.
  - Màn hình tắt: thông báo chỉ hiện trên AOD, không tự bật sáng màn hình (vẫn bật lại được trong Hành vi hiển thị).
  - Android 13+: xin quyền "Hiện thông báo" để nhắc nhở & thông báo của Capsule VN hiện ra.
  - Chế độ ngân hàng: app ngân hàng bị tự thoát lần đầu sẽ được mở lại; nhắc bật lại Viên thuốc khi rời app hoạt động cả khi chưa dùng Shizuku.
  - Biểu ngữ hệ thống (Samsung): đánh dấu "Đã tắt" sau khi bỏ chọn Pop-up thông báo của từng app.
  - "Có gì mới" hiện đúng nội dung phiên bản mới.
- **Chống trùng thông báo**: tự ẩn thẻ cuộc gọi đến khi đang mở sẵn màn hình cuộc gọi của máy / Zalo / Messenger…
- **Quyền riêng tư**: thêm công tắc tắt nguồn lời bài hát dự phòng NetEase (máy chủ ở Trung Quốc) để chỉ dùng LRCLIB.
- **Nhẹ hơn**: cảm biến nghiêng của Liquid Glass tắt khi bật "Giảm hiệu ứng"; viền chỉ chạy hiệu ứng khi đang bật và màn hình đang sáng.

## v1.4.3 (2026) — Sửa Viên thuốc bị lệch trái / phải so với camera
- Cửa sổ Viên thuốc trước đây để hệ thống tự đo bề ngang; trên một số máy (Samsung) kích thước này bị sai sau khi khởi động lại hoặc khi đổi cấu hình (xoay ngang, mở app toàn màn hình…), làm Viên thuốc và thẻ thông báo lệch trái / phải.
- Nay cửa sổ được ghim đúng mép trái và đúng bề ngang thật của màn hình, tự kiểm tra lại khi bật màn hình, khi xoay, khi chuyển ứng dụng — Viên thuốc luôn thẳng tâm camera.

## v1.4.2 (2026) — Icon trọn khung vàng trên Samsung, hướng dẫn tắt pop-up đúng One UI
- **Icon**: launcher Samsung One UI dùng bộ icon kiểu cũ (vuông) rồi tự bo theo hình của máy, nên khung vàng bị cắt. Bộ icon này giờ được thu gọn vào giữa (khung 60/72) — khung vàng hiện trọn vẹn, không mất góc.
- **Tin nhắn hiện 2 lần trên Samsung**: hướng dẫn theo đúng chữ trên One UI — trong trang thông báo của ứng dụng, **bỏ chọn ô “Pop-up thông báo”** (giữ “M.hình khóa” và “Huy hiệu”).
- Thông báo của Giao diện hệ thống (pin yếu, USB…) không còn bị hiện như thẻ tin nhắn (đã có thẻ sự kiện riêng) và không còn nằm trong danh sách ứng dụng nhắn tin.

## v1.4.1 (2026) — Tab "Khác" chỉnh riêng từng loại thông báo
- **Khung thẻ thông báo → Khác** giờ chia mục: **Tất cả** (chung) và từng loại **Sạc pin, Tai nghe & Bluetooth, Thanh toán, Điều hướng, Báo thức, Chế độ chuông, Sự kiện khác**.
  - Mỗi loại chỉnh riêng vị trí dọc / ngang, chiều rộng, chiều cao thêm, bo góc; chọn loại nào thì thẻ mẫu đúng loại đó hiện trực tiếp trên đỉnh màn hình.
  - Loại chưa chỉnh dùng chung thiết lập "Tất cả"; loại đã chỉnh có chấm vàng, bấm "Dùng chung như Tất cả" để bỏ.

## v1.4.0 (2026) — Tuỳ chỉnh chuyên sâu vị trí, kích thước, viền sáng & cử chỉ
- **Viên thuốc khi có hoạt động** (Giao diện → Khi có hoạt động): thêm Vị trí dọc (lên / xuống) và Vị trí ngang (trái / phải), xem trước trực tiếp.
- **Khung thẻ thông báo → tab "Khác"** cho mọi thông báo còn lại (sạc, tai nghe, thanh toán, sự kiện…): vị trí dọc / ngang, chiều rộng, chiều cao thêm, bo góc — xem trước trực tiếp.
- **Nền & viền cho Viên thuốc khi không hoạt động** (bật được): dùng chung nền màu / ảnh / Liquid Glass và viền.
- **Viền sáng chuyên sâu** (Vòng sáng LED & viền thẻ):
  - Màu: Cầu vồng 360°, Theo màu ứng dụng / sự kiện, Neon, Vàng hoàng gia, Màu đơn tự chọn, **2–3 màu tự chọn**.
  - Chuyển động: **Xoay 360°, Sao chổi chạy quanh viền, Nhịp thở, Nhấp nháy, Đứng yên**, kèm **tốc độ**.
  - Viền thẻ thông báo có thể dùng hiệu ứng như Vòng sáng LED thay cho màu đơn.
- **Cử chỉ hợp lý hơn**:
  - Sửa "Mở ứng dụng" không hoạt động: mở đúng app của hoạt động / thông báo (tin nhắn, điều hướng, cuộc gọi, hẹn giờ → Đồng hồ…); Viên thuốc trống mở Capsule VN. Chạm đúp / nhấn giữ trên thông báo cũng hoạt động.
  - Mỗi lựa chọn có mô tả rõ; thêm "Dùng cài đặt khuyên dùng": Chạm = Mở rộng, Chạm đúp = Mở ứng dụng, Giữ = Phát / Tạm dừng.
- **Tin nhắn hiện 2 lần trên Samsung**: nút Thiết lập mở trang thông báo của ứng dụng kèm hướng dẫn One UI (bỏ chọn "Cửa sổ bật lên" / "Biểu ngữ"); thêm tuỳ chọn **"Thu gọn khi app tự hiện pop-up"** — Viên thuốc chỉ báo viên nhỏ khi app đó tự bật pop-up.
- **Icon**: khung vàng thu về 60 dp để hiện trọn trên launcher Samsung One UI (bản trước bị cắt góc).

## v1.3.2 (2026) — Sáng màn hình cho mọi thông báo & giờ ngủ
- **Sáng màn hình khi có thông báo** giờ áp dụng cho mọi thông báo (tin nhắn, sạc pin, tai nghe, thanh toán, sự kiện hệ thống…), không chỉ tin nhắn. Vẫn tự tắt lại, không sáng khi máy trong túi / úp xuống.
- **Không sáng trong giờ ngủ** (mặc định bật, 23:00–6:00, chỉnh được): trong khung giờ này thông báo vẫn hiện trên Viên thuốc nhưng không làm sáng màn hình.

## v1.3.1 (2026) — Sáng màn hình khi có tin nhắn
- **Sáng màn hình khi có tin nhắn** (Tính năng → Hành vi hiển thị → Rung & màn hình, mặc định bật): khi màn hình đang tắt, tin nhắn tới sẽ làm màn hình sáng lên và hiện trên Viên thuốc ở màn hình khoá; hết thời gian hiển thị thì tự tắt lại.
  - Không sáng khi máy để trong túi hoặc úp xuống (dùng cảm biến tiệm cận).
  - Nếu bạn chạm vào Viên thuốc (trả lời, xem thêm) hoặc mở khoá máy thì màn hình không tự tắt.
  - Tôn trọng cài đặt ẩn nội dung thông báo trên màn hình khoá và chế độ Không làm phiền.

## v1.3.0 (2026) — Icon khung vàng trọn vẹn, vuốt chuyển hoạt động, trang chủ mới & tinh gọn giao diện
- **Tin nhắn chưa đọc xem lại được**: khi viền LED báo tin nhắn chưa đọc đang sáng, chạm vào Viên thuốc sẽ mở đúng thẻ tin nhắn đó (kèm nút Trả lời / Đã đọc) thay vì bảng Âm lịch – RAM – ROM; vẫn vuốt trái / phải sang nhạc, hẹn giờ… Chạm vào thẻ để mở cuộc trò chuyện; xem xong thu nhỏ thì viền tự tắt.
- **Sửa báo nhầm "Thanh toán NFC – Chạm vào đầu đọc thẻ" khi đăng nhập ứng dụng**: siết nhận diện màn hình thanh toán (không còn khớp nhầm tên màn hình như "SetPayment/SmartPay" hay chữ "cài đặt … vào máy"); gợi ý khi mở app chỉ áp dụng cho ví / ngân hàng.
- **Vuốt ngang chuyển hoạt động khi thu gọn** (tuỳ chọn trong Cử chỉ chạm & vuốt, mặc định tắt). Khi đang phát nhạc, vuốt vẫn ưu tiên chuyển bài; khi mở rộng, vuốt luôn chuyển hoạt động.
- **Giao diện đen – đỏ – vàng kim** đồng bộ với logo: nền đen ấm, thẻ đỏ thẫm, nút & điểm nhấn vàng kim.
- **Icon phóng to vừa khung squircle** (HyperOS, One UI, ColorOS) — khung vàng chiếm 68/72 dp.
- **Cập nhật OTA ổn định**: CI ký APK bằng khóa cố định (GitHub Secrets) để các bản sau cài đè được qua cập nhật trong app.
- Thêm lối vào "Giới thiệu về Viên thuốc" trong tab Giới thiệu.
- **Sao lưu cấu hình cá nhân**:
  - Tự động sao lưu (mặc định bật, Android 10+): cấu hình mới nhất luôn được lưu vào `Download/CapsuleVN/CapsuleVN-caidat.json` — file vẫn còn sau khi gỡ ứng dụng.
  - Cài lại: Trang chủ hiện thẻ "Khôi phục cài đặt cũ?" mở sẵn thư mục sao lưu, chọn file là phục hồi toàn bộ thiết lập. Nếu máy bật sao lưu Google, cài đặt cũng được Android tự khôi phục.
  - Vẫn có Sao lưu / Khôi phục thủ công trong Tính năng → Hệ thống (dùng khi đổi máy).
- **Nhắc bật lại Viên thuốc khi rời app ngân hàng** rung nhẹ 2 nhịp, không chuông.
- **Nền & viền thẻ thông báo tuỳ chỉnh** (Giao diện → Khung thẻ thông báo): kiểu nền Như Viên thuốc / Màu tuỳ chọn / Ảnh nền (tự cắt vừa khung, làm tối để dễ đọc) / Liquid Glass (iOS), độ đậm nền, màu & độ dày viền — áp dụng cho thẻ tin nhắn, cuộc gọi đến, cập nhật và tin chưa đọc, xem trước trực tiếp.
- **Chế độ ngân hàng & VNeID** (Trang chủ / Giới thiệu → Ngân hàng & VNeID): tự tắt Viên thuốc khi bạn chạm biểu tượng hoặc mở app ngân hàng, ví / thanh toán, VNeID, VssID (tự nhận diện theo tên gói, tên app và dịch vụ thanh toán NFC; chỉnh được danh sách).
  - Dùng ngay, không cần cài đặt gì: dùng xong chạm thông báo để bật lại.
  - Cấp "Truy cập dữ liệu sử dụng" (trong Cài đặt): rời app sẽ hiện nhắc 1 chạm mở thẳng trang bật Viên thuốc.
  - Nâng cao (tuỳ chọn): cấp 1 lần qua Shizuku hoặc ADB để tự bật lại hoàn toàn; sau đó có thể gỡ Shizuku và tắt Tuỳ chọn nhà phát triển.
- **Nền & viền áp dụng cho các bảng thông báo** (tin nhắn, cuộc gọi, cập nhật, điều hướng, báo thức…) **và cả bảng mở rộng**; thông báo dạng viên nhỏ (sạc, tai nghe, chế độ chuông, thanh toán…) mặc định giữ nền đen như cũ, bật được nền ảnh / Liquid Glass nếu muốn.
- **Thẻ tin nhắn cân đối hơn**: dòng tên app / thời gian to, rõ và thụt vào khỏi góc bo; bớt khoảng trống phía trên; ảnh đại diện tròn 44 dp kèm biểu tượng app; nút Trả lời / Đã đọc dạng viên tròn chia đều bề ngang.
- **Trình phát nhạc mở rộng hiện đại**: ảnh bìa 64 dp bo tròn, tên bài / ca sĩ rõ ràng, thanh tiến trình toàn chiều ngang với thời gian bên dưới, nút Phát / Tạm dừng tròn trắng nổi bật ở giữa.
- **Nhiều tin nhắn chưa đọc**: mỗi cuộc trò chuyện là một trang riêng (tối đa 6), vuốt qua lại; cuộc trò chuyện nào đã xem / đã mở / đã trả lời thì tự bỏ khỏi danh sách, viền LED đổi theo màu tin mới nhất còn lại.
- **Cuộc gọi Zalo / Messenger**: sau khi bấm Nghe trên Viên thuốc, tự mở màn hình cuộc gọi của ứng dụng để ứng dụng đó tắt chuông và rung.
- **Thử nghiệm tính năng**: bỏ mục Trứng phục sinh; sửa Trình phát nhạc thử nghiệm không hiện (trước đây bị ẩn vì trùng với chính ứng dụng đang mở).
- **Icon ứng dụng giữ khung vàng, tách nền ngoài khung**:
  - Giữ nguyên khung vàng và toàn bộ nền khói đỏ/đen bên trong khung; 4 góc đen bên ngoài được tách bỏ (trong suốt).
  - Icon trong mục Giới thiệu, icon tròn kiểu cũ và ảnh README dùng chung bản khung vàng tách nền.
- **Vuốt qua lại giữa các hoạt động khi mở rộng**:
  - Chạm vào Viên thuốc mở đúng hoạt động đang hiển thị; vuốt trái / phải để chuyển sang hoạt động khác đang chạy (nhạc, hẹn giờ, đèn pin, điều hướng, tải xuống…), có chấm chỉ báo trang.
  - Bảng trạng thái mặc định (Giờ, Pin, Âm lịch…) chỉ hiện khi không có hoạt động nào.
- **Thông báo mẫu không chen ngang thông báo thật**: khi chỉnh "Thời gian hiển thị sự kiện", thông báo mẫu chỉ thay thế thông báo mẫu trước đó; tin nhắn thật từ ứng dụng khác luôn được ưu tiên và hiển thị đủ thời gian.
- **Trang chủ mới**: thẻ thương hiệu với logo Viên thuốc Capsule VN trên nền đỏ rượu – đen ánh vàng, kèm trạng thái hoạt động và phiên bản; ô xem trước Viên thuốc ở mục Giao diện đổi sang tông màu cùng bộ nhận diện.
- **Rà soát & tinh gọn toàn bộ giao diện**:
  - Tên mục và tiêu đề trang được đồng bộ (Thẻ hoạt động, Tin nhắn & thông báo, Sự kiện hệ thống, Mã OTP 1 chạm, Đọc thông báo qua tai nghe, Hành vi hiển thị, Biểu ngữ hệ thống, Màu sắc & viền, Vòng sáng LED, Hiệu ứng đặc biệt, Khung thẻ thông báo…), mô tả ngắn gọn dễ hiểu hơn.
  - Bỏ các mục trùng lặp: OTP, đọc thông báo và số dòng tin nhắn chỉ còn ở một nơi; lối tắt khung thẻ trùng trong Vị trí & kích thước được gỡ; "Ứng dụng ngân hàng không mở được" chuyển về Giới thiệu → Trợ giúp.
  - Dịch lại thẻ "Tin nhắn bị hiện 2 lần?" sang tiếng Việt; chữ mô tả các mục nhỏ gọn hơn (14 sp).

## v1.2.1 (2026) — Sửa icon mất góc, làm lại giao diện Vị trí & kích thước, xem trước chuyển động trực tiếp trên Viên thuốc
- **Icon ứng dụng không còn bị mất góc**:
  - Nguyên nhân: lớp foreground của Adaptive Icon chứa sẵn khung viền vàng bo góc + 4 góc đen, nên khi launcher (HyperOS, One UI, Pixel…) cắt theo hình dạng riêng thì khung vàng bị xén lệch, góc bị khuyết.
  - Làm lại foreground dạng tràn viền: nền khói đỏ/đen + nhũ vàng phủ kín 108 dp, logo "CAPSULE VN ★" nằm gọn trong vùng an toàn 66 dp; launcher tự bo theo hình dạng của máy.
  - Bổ sung icon đơn sắc riêng (`ic_launcher_monochrome`) cho chế độ Biểu tượng theo chủ đề (Android 13+), không còn bị khối vuông đặc.
- **Vị trí & kích thước → tab Bảng mở rộng**:
  - Sửa lỗi **Vị trí dọc (lên / xuống) không hoạt động**: giá trị `expandedOffsetY` trước đây chỉ được lưu mà không được áp dụng; nay Bảng mở rộng dịch chuyển đúng (kể cả giá trị âm) cả khi xem trước lẫn khi bung thật.
  - Thiết kế lại thanh trượt dùng chung: tiêu đề chiếm trọn chiều ngang, nút "↺ Mặc định" nằm dưới tiêu đề, ô giá trị cố định bên phải → hết hiện tượng rớt chữ từng ký tự.
  - Đặt lại tiêu đề ngắn gọn: "Vị trí dọc (lên / xuống)", "Chiều rộng" (hiển thị "Tự động" khi theo màn hình), "Chiều cao thêm", "Bo góc"; đồng bộ cho thẻ Tin nhắn / Cuộc gọi / Cập nhật.
- **Mở rộng & Độ đàn hồi**:
  - Bỏ khung mô phỏng chuyển động; mọi thay đổi (Kiểu mở rộng, Độ đàn hồi, Mở rộng dạng chất lỏng, Tốc độ chuyển động, Giảm hiệu ứng) giờ được bung thử **trực tiếp trên Viên thuốc thật** ở đỉnh màn hình. Thêm nút ▶ để xem lại.
  - "Thời gian hiển thị sự kiện" phát một thông báo mẫu trên Viên thuốc, hiển thị đúng số giây đã chọn (bước 0,5 giây).

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
