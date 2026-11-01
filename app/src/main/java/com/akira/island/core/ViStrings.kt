package com.akira.island.core

object ViStrings {
    private val enMap: Map<String, String> = mapOf(
        "Smart OTP 1-Tap Copy & Fill" to "Sao chép & Điền nhanh OTP 1 chạm",
        "Auto-copy & 1-tap autofill verification OTP codes from SMS, banks & messengers" to "Tự động sao chép & điền nhanh mã xác thực OTP 1 chạm từ SMS, ngân hàng, Zalo…",
        "Headset Voice Announcer" to "Tự động đọc thông báo qua Tai nghe",
        "Automatically read incoming messages & caller names aloud when wearing headphones" to "Tự động đọc to tin nhắn đến và tên người gọi khi đang đeo tai nghe",
        "OTP Settings" to "Tùy chỉnh nhận diện & điền mã OTP",
        "Detect OTP verification codes" to "Nhận diện mã xác thực OTP thông minh",
        "Detect 4–8 digit verification codes in SMS, banking apps, Zalo, Telegram & email and show a 1-tap button on the capsule" to "Tự động phát hiện mã xác thực 4–8 chữ số trong SMS, ngân hàng, Zalo, Telegram, Gmail và hiện nút 1 chạm trên Viên thuốc",
        "Auto-copy OTP on arrival" to "Tự động chép OTP ngay khi tin nhắn tới",
        "Automatically copy the OTP code to clipboard as soon as the message arrives without tapping" to "Tự động sao chép mã OTP vào bộ nhớ tạm (Clipboard) ngay khi nhận thông báo mà không cần chạm",
        "1-Tap Auto-fill into input box" to "Tự động điền OTP vào ô nhập liệu (1 chạm)",
        "When tapping the OTP button on the capsule, automatically fill the code directly into the active input field on screen" to "Khi chạm nút OTP trên Viên thuốc, tự động điền thẳng mã OTP vào ô nhập văn bản đang mở trên màn hình",
        "Test OTP message card (482910)" to "Thử nhận tin nhắn chứa mã OTP (482910)",
        "Show a simulated message with the 1-tap Copy & Fill OTP: 482910 button" to "Hiển thị thẻ tin nhắn mô phỏng kèm nút Chép & Điền OTP: 482910",
        "Voice Announcer Settings" to "Tùy chỉnh đọc thông báo bằng giọng nói",
        "Enable Voice Announcer" to "Bật tự động đọc to thông báo",
        "Automatically read incoming messages and calls aloud using Android Text-To-Speech (Vietnamese supported)" to "Tự động đọc to thông báo tin nhắn và cuộc gọi đến bằng giọng đọc Tiếng Việt của hệ thống Android",
        "Only when wearing headphones" to "Chỉ đọc khi đang đeo tai nghe",
        "Only read aloud when Bluetooth earbuds (Galaxy Buds, AirPods…) or wired headphones are connected — ideal while driving" to "Chỉ đọc to khi đang kết nối tai nghe Bluetooth (Galaxy Buds, AirPods…) hoặc tai nghe dây — rất tiện và riêng tư khi đang lái xe",
        "Read full message content" to "Đọc cả nội dung chi tiết tin nhắn",
        "On — reads sender name and message body. Off — announces only the app and sender name for privacy" to "Bật — đọc tên người gửi kèm nội dung tin nhắn. Tắt — chỉ đọc tên ứng dụng và tên người gửi để bảo mật",
        "Announce incoming calls" to "Đọc tên người gọi khi có cuộc gọi đến",
        "Speak the caller's name or number aloud on incoming calls" to "Đọc to tên người gọi (trong danh bạ) hoặc số điện thoại khi có cuộc gọi đến",
        "Test Voice Announcer now" to "Nghe thử giọng đọc thông báo ngay",
        "Play a sample Vietnamese voice announcement right now (works even without headphones)" to "Phát thử ngay một câu đọc thông báo Tiếng Việt (kể cả khi chưa đeo tai nghe)",
        "Stop speaking" to "Dừng đọc ngay",
        "Stop current voice announcement" to "Ngắt giọng đọc thông báo đang phát",
        "Rainbow RGB 360°" to "Cầu vồng RGB xoay 360°",
        "App / Event adaptive" to "Theo màu ứng dụng / sự kiện",
        "Cyber Neon" to "Neon Cyber (Xanh - Tím - Đỏ)",
        "Breathing Gold" to "Nhịp thở Vàng Hoàng Gia",
        "Custom color" to "Màu tùy chọn",
        "Gaming & Ecosystem" to "Chế độ Gaming & Hệ sinh thái",
        "Gaming Mode" to "Chế độ Gaming",
        "Auto-detect games on all Android devices, compact pills & Gaming HUD (°C, RAM)" to "Tự nhận diện game trên mọi máy Android, thu gọn thông báo & Gaming HUD (°C, RAM)",
        "Samsung & Android Ecosystem" to "Hệ sinh thái Samsung & Android",
        "S Pen stylus, Galaxy Buds/Watch, Super Fast Charging, Battery Protection & DeX / Cast" to "Bút S Pen, Galaxy Buds/Watch, Sạc siêu nhanh, Bảo vệ pin & DeX / Truyền màn hình",
        "Capsule LED Ring" to "Vòng sáng LED Viên thuốc",
        "Glowing LED ring around the capsule for unread alerts, charging and events" to "Vòng sáng LED quanh Viên thuốc khi có tin chưa đọc, sạc pin và sự kiện",
        "Universal Android Gaming" to "Tương thích mọi dòng máy Android",
        "Automatically activates anti-block popups and HUD when launching any game on any Android device" to "Tự động chống che màn hình và bật HUD khi mở bất kỳ tựa game nào (Samsung, Xiaomi, OPPO, Vivo, Pixel, ROG…)",
        "Compact notifications in games" to "Thu gọn thông báo khi đang chơi game",
        "Converts large message banners into a compact non-intrusive pill so game controls & minimap are never blocked" to "Chuyển thẻ tin nhắn lớn thành viên thuốc mini gọn gàng để không bao giờ che bản đồ nhỏ (minimap) hay nút bấm",
        "Gaming HUD (°C & RAM)" to "Bảng theo dõi Gaming HUD (°C & RAM)",
        "Shows real-time battery temperature (°C) and RAM usage inside the expanded capsule" to "Hiển thị nhiệt độ máy/pin (°C) và mức RAM đang dùng theo thời gian thực khi mở rộng Viên thuốc",
        "Keep visible in landscape games" to "Giữ hiển thị trong game xoay ngang",
        "Keep the compact capsule & gaming HUD active in landscape games even when Hide in landscape is on" to "Giữ viên thuốc gọn & Gaming HUD trong game màn hình ngang ngay cả khi bật Ẩn khi xoay ngang",
        "Force Gaming Mode On" to "Bật cưỡng bức Chế độ Gaming ngay",
        "Manually enable Gaming Mode for any app" to "Bật thủ công Chế độ Gaming mọi lúc cho bất kỳ ứng dụng nào",
        "Test" to "Kiểm tra thử",
        "Test compact gaming notification" to "Thử thông báo thu gọn trong Game",
        "Preview how a message looks while gaming" to "Xem thử tin nhắn Zalo/Messenger hiển thị gọn như thế nào khi đang chiến game",
        "Open Gaming HUD on capsule" to "Mở bảng Gaming HUD trên Viên thuốc",
        "Expand capsule to view live °C temperature and RAM" to "Mở rộng Viên thuốc ngay để xem nhiệt độ °C và bộ nhớ RAM thực tế",
        "Device Integration" to "Tích hợp thiết bị & phần cứng",
        "Samsung S Pen Stylus" to "Bút cảm ứng Samsung S Pen",
        "Capsule animation when removing or inserting the S Pen stylus (Galaxy S Ultra, Note, Fold, Tab)" to "Hiệu ứng Viên thuốc khi rút bút hoặc cất bút S Pen (dòng Galaxy S Ultra, Note, Z Fold, Tab)",
        "Galaxy Buds / Watch & Bluetooth Wearables" to "Galaxy Buds / Watch & Thiết bị đeo Bluetooth",
        "Shows model name and battery ring for TWS earbuds and smartwatches across all Android brands" to "Hiển thị tên thiết bị và vòng % pin cho tai nghe TWS & đồng hồ thông minh trên mọi máy Android",
        "Super Fast Charging & Battery Temp °C" to "Sạc siêu nhanh & Nhiệt độ Pin (°C)",
        "Detects Fast / Super Fast Charging 2.0 and displays live battery temperature in °C" to "Phân biệt Sạc nhanh / Sạc siêu nhanh 2.0 và hiển thị nhiệt độ pin (°C) trực tiếp",
        "Battery Protection Alert" to "Cảnh báo Bảo vệ Pin chống chai",
        "Samsung DeX / Smart View / Cast" to "Samsung DeX / Smart View / Truyền màn hình",
        "Capsule alert when connecting or disconnecting an external display, TV Cast, or Samsung DeX" to "Báo trên Viên thuốc khi kết nối hoặc ngắt màn hình rời HDMI, TV không dây hoặc Samsung DeX",
        "Live Feature Simulation" to "Mô phỏng kiểm tra trực tiếp",
        "Test S Pen (Removed)" to "Thử rút bút S Pen",
        "Simulate pulling out the S Pen stylus" to "Mô phỏng hiệu ứng khi rút bút S Pen khỏi máy",
        "Test Super Fast Charging" to "Thử Sạc siêu nhanh (Super Fast Charging)",
        "Simulate Super Fast Charging 2.0 alert" to "Mô phỏng thông báo Sạc siêu nhanh 2.0 trên Viên thuốc",
        "Test Galaxy Buds / Watch" to "Thử kết nối Galaxy Buds3 Pro / Watch",
        "Simulate connecting Galaxy Buds3 Pro with 92% battery ring" to "Mô phỏng kết nối tai nghe Galaxy Buds3 Pro kèm vòng pin 92%",
        "Test Samsung DeX / Smart View" to "Thử kết nối Samsung DeX / Màn hình rời",
        "Simulate connecting Samsung DeX / external monitor" to "Mô phỏng kết nối không dây Samsung DeX / Smart View",
        "Enable Capsule LED Ring" to "Bật Vòng sáng LED quanh Viên thuốc",
        "Animated glowing LED border around the camera cutout & capsule" to "Viền ánh sáng LED chuyển động mượt mà bao quanh camera và Viên thuốc",
        "LED Lighting Style" to "Kiểu hiệu ứng ánh sáng LED",
        "LED Ring Thickness" to "Độ dày viền sáng LED",
        "Custom LED Color" to "Màu viền LED tùy chọn",
        "LED Triggers" to "Khi nào Vòng sáng LED bật?",
        "Unread notifications" to "Khi có thông báo / tin nhắn chưa đọc",
        "Glows in the app's brand color (Zalo, Messenger, Telegram…) until you open the app or tap the capsule" to "Sáng viền theo màu ứng dụng (Zalo xanh dương, Messenger, Telegram…) cho đến khi bạn mở app hoặc chạm vào Viên thuốc",
        "During events & notifications" to "Khi có sự kiện & thông báo mới",
        "Illuminate capsule edges whenever an event or notification banner appears" to "Làm rực sáng viền Viên thuốc mỗi khi có thẻ thông báo hoặc sự kiện xuất hiện",
        "While charging battery" to "Khi đang cắm sạc pin",
        "Rotating glow around the camera while the phone is plugged in" to "Vòng sáng xoay quanh camera trong suốt thời gian cắm sạc",
        "Always-On decorative LED" to "Luôn sáng trang trí (Always-On LED)",
        "Keep the LED ring glowing continuously around the capsule while screen is on" to "Giữ vòng sáng LED luôn lung linh quanh Viên thuốc khi màn hình bật",
        "Test Unread LED Ring (Zalo Blue)" to "Bật thử Vòng sáng LED báo tin chưa đọc",
        "Light up the unread LED ring around the capsule right now (tap capsule to clear)" to "Thắp sáng vòng LED quanh Viên thuốc ngay bây giờ (chạm vào Viên thuốc để tắt)",
        "Clear Unread LED indicator" to "Tắt đèn LED thử nghiệm",
        "Turn off the active unread LED indicator" to "Tắt hiệu ứng đèn báo chưa đọc đang sáng",
        "Super Fast Charging" to "Sạc siêu nhanh",
        "Fast Charging" to "Sạc nhanh",
        "Battery Protection" to "Bảo vệ pin",
        "S Pen Stylus" to "Bút S Pen",
        "Removed" to "Đã rút bút",
        "Inserted" to "Đã cất bút",
        "External Display" to "Màn hình rời",
        "Display / DeX" to "Màn hình / DeX",
        "Smartwatch" to "Đồng hồ thông minh",
        "System HUD" to "Hệ thống",
        " · %.1f MB" to " · %.1f MB",
        "\$12.50" to "125.000 đ",
        "%.1f s" to "%.1f giây",
        "12 hours before an alarm — the wake-up time and a sleep dial, like on iPhone" to "12 giờ trước báo thức — thời gian thức dậy và vòng quay giấc ngủ, như trên iPhone",
        "300 m" to "300 m",
        "A check mark and the amount — from the wallet screen, a notification or a bank SMS" to "Dấu tích và số tiền — từ màn hình ví, thông báo hoặc tin nhắn SMS ngân hàng",
        "A lock opens when you unlock the phone" to "Ổ khóa mở ra khi bạn mở khóa điện thoại",
        "A moon and the state when it changes" to "Mặt trăng và trạng thái khi thay đổi",
        "A one-minute countdown" to "Đếm ngược một phút",
        "A soft halo in the artwork's color around the island while music plays" to "Vầng sáng dịu theo màu ảnh bìa xung quanh Viên thuốc khi phát nhạc",
        "A soft shadow under the capsule — visible on light screens" to "Bóng mờ dưới viên thuốc — dễ nhìn thấy trên nền màn hình sáng",
        "A thin line around the capsule" to "Đường viền mỏng xung quanh viên thuốc",
        "A tile to turn the island off for a while" to "Phím tắt để tạm thời tắt Viên thuốc trong giây lát",
        "About" to "Giới thiệu",
        "About the island" to "Giới thiệu về Viên thuốc",
        "Accessibility" to "Hỗ trợ tiếp cận",
        "Add the Island tile" to "Thêm phím tắt Viên thuốc",
        "Airplane Mode" to "Chế độ máy bay",
        "Airplane mode off" to "Đã tắt chế độ máy bay",
        "Airplane mode on" to "Đã bật chế độ máy bay",
        "Alarm" to "Báo thức",
        "Alarm set" to "Đã đặt báo thức",
        "All granted" to "Đã cấp tất cả quyền",
        "All off" to "Tắt tất cả",
        "All set, your settings are kept" to "Hoàn tất, cài đặt của bạn được giữ nguyên",
        "Also show notifications from the app that's on screen" to "Hiển thị cả thông báo từ ứng dụng đang mở trên màn hình",
        "An event, message, call, payment — right in the island" to "Sự kiện, tin nhắn, cuộc gọi, thanh toán — ngay trên Viên thuốc",
        "An iPhone-style capsule over your Android camera: music with artwork, an equalizer and synced lyrics, a timer with a progress ring, a stopwatch, download progress, a flashlight with brightness control, notifications with replies, payments, navigation, charging and system events. A second activity goes into a bubble next to the island." to "Viên thuốc kiểu iPhone bao quanh camera Android của bạn: âm nhạc với ảnh bìa, sóng nhạc động và lời đồng bộ; hẹn giờ với vòng đếm ngược; đồng hồ bấm giờ; tiến trình tải xuống; đèn pin tùy chỉnh độ sáng; thông báo kèm nút trả lời nhanh; thanh toán chạm; bản đồ điều hướng; trạng thái sạc và sự kiện hệ thống. Khi có hai hoạt động, hoạt động thứ hai sẽ thành bong bóng tròn cạnh Viên thuốc.",
        "Android System" to "Hệ thống Android",
        "Android says “Restricted setting” or “App was denied access”? That's how it protects apps not from Google Play:\n1. Try to turn the service on once — that window shows up, close it.\n2. Open App info → “⋮” in the top right → “Allow restricted settings”.\n3. Go back to Accessibility and turn the island on." to "Android báo “Ứng dụng đã bị từ chối cấp quyền” hoặc “Cài đặt bị hạn chế”? Đây là cơ chế bảo vệ ứng dụng cài ngoài Play Store:\n1. Bấm thử bật dịch vụ 1 lần — bảng cảnh báo hiện lên, bấm Đóng.\n2. Nhấn giữ biểu tượng app ở màn hình chính → Chọn (i) Thông tin ứng dụng → Bấm dấu 3 chấm “⋮” góc trên bên phải → Chọn “Cho phép cài đặt bị hạn chế”.\n3. Quay lại Hỗ trợ tiếp cận và gạt Bật Capsule VN.",
        "Android shows its own pop-up banners on top of the island, and the system doesn't let a third-party app block them. Tap an app and turn off “Pop on screen” (on some phones — “Show on top” or “Banners”). The notification stays in the shade, and only the island shows the event on screen." to "Android tự hiện biểu ngữ thông báo đè lên Viên thuốc, và hệ thống không cho phép ứng dụng bên thứ ba tự chặn chúng. Hãy chạm vào ứng dụng và tắt “Bật lên trên màn hình” (hoặc “Hiển thị biểu ngữ”). Thông báo vẫn được giữ trong thanh trạng thái, và chỉ có Viên thuốc hiển thị sự kiện trên màn hình.",
        "Animation elasticity" to "Độ đàn hồi của hiệu ứng",
        "Animations" to "Hiệu ứng chuyển động",
        "Anna" to "Anna",
        "App info in Android" to "Thông tin ứng dụng trong Android",
        "App messages with replies right in the island" to "Tin nhắn kèm nút trả lời nhanh ngay trên Viên thuốc",
        "App notifications" to "Thông báo ứng dụng",
        "Appearance" to "Giao diện",
        "Appearance tab → Position and size. While the screen is open, the island is red: move it until the red sits exactly over the camera, pick the width and height and tap “Save”." to "Tại tab Giao diện → Vị trí & kích thước. Trong khi màn hình này đang mở, viên thuốc sẽ có màu đỏ: hãy di chuyển thanh trượt sao cho vùng đỏ nằm vừa khít quanh camera, chọn chiều rộng/cao phù hợp rồi bấm “Lưu”.",
        "Arrive 18:42 · 12 min · 4.2 km" to "Đến nơi 18:42 · 12 phút · 4,2 km",
        "Artwork glow" to "Phát sáng theo ảnh bìa",
        "As without it" to "Như khi không có",
        "Auto" to "Tự động",
        "Autostart" to "Tự khởi chạy",
        "Back" to "Quay lại",
        "Back to how it was after installing" to "Khôi phục về trạng thái như khi mới cài đặt",
        "Bank app won't open" to "Ứng dụng ngân hàng không mở được",
        "Banks do this to protect against fraud: while any accessibility service not from Google Play is on, they won't let you in. The island reads nothing in your bank, but there's no way around the check — only turning the island off for a while. The easiest way is a tile in quick settings: tap it — the island turns off and you can use the bank; tap it again to turn the island back on." to "Các ngân hàng làm điều này để bảo vệ người dùng chống lừa viên thuốc: khi có bất kỳ dịch vụ hỗ trợ tiếp cận nào bật ngoài Google Play, họ sẽ chặn truy cập. Viên thuốc hoàn toàn không đọc dữ liệu ngân hàng, nhưng không thể vượt qua bước kiểm tra bảo mật này — chỉ có thể tạm tắt Viên thuốc. Cách tiện nhất là dùng phím tắt trên Cài đặt nhanh: chạm một cái — Viên thuốc tắt và bạn vào app ngân hàng; giao dịch xong chạm lại để bật Viên thuốc.",
        "Battery" to "Pin",
        "Battery (Xiaomi, Huawei, Samsung…)" to "Pin (Samsung, Xiaomi, Huawei…)",
        "Battery and charging" to "Pin & đang sạc",
        "Battery saver or “Activity control” → “No restrictions”" to "Tiết kiệm pin hoặc “Kiểm soát hoạt động” → “Không hạn chế”",
        "Battery saving" to "Tiết kiệm pin",
        "Battery usage → allow background activity" to "Mức sử dụng pin → Cho phép hoạt động ngầm",
        "Battery → Unrestricted, and remove the island from Sleeping apps" to "Pin → Không hạn chế, và xóa Viên thuốc khỏi danh sách Ứng dụng ở chế độ ngủ",
        "Battery, autostart and locking in recent apps" to "Pin, tự khởi chạy và khóa trong ứng dụng gần đây",
        "Behavior" to "Hành vi",
        "Blur on change" to "Làm mờ khi thay đổi",
        "Brightness" to "Độ sáng",
        "Brightness mode" to "Chế độ độ sáng",
        "Button color" to "Màu nút bấm",
        "Call" to "Cuộc gọi",
        "Calls" to "Cuộc gọi",
        "Can help on less powerful devices" to "Có thể hữu ích trên thiết bị cấu hình thấp",
        "Can't reach GitHub. Check your internet connection and try again." to "Không thể kết nối GitHub. Vui lòng kiểm tra mạng và thử lại.",
        "Cancel" to "Hủy",
        "Capsule color" to "Màu viên thuốc",
        "Center" to "Chính giữa",
        "Charging" to "Đang sạc",
        "Charging 75% at full width" to "Đang sạc 75% với độ rộng tối đa",
        "Charging indicator" to "Chỉ báo đang sạc",
        "Charging started" to "Bắt đầu sạc pin",
        "Charging stopped" to "Đã ngắt sạc pin",
        "Charging, Wi-Fi, headphones, payments, alarm" to "Sạc pin, Wi-Fi, tai nghe, thanh toán, báo thức",
        "Check for updates" to "Kiểm tra bản cập nhật",
        "Check now" to "Kiểm tra ngay",
        "Checking for updates…" to "Đang kiểm tra bản cập nhật…",
        "Checking…" to "Đang kiểm tra…",
        "Clock buttons" to "Các nút Đồng hồ",
        "Clock's timer and stopwatch in the background — even with its notifications off" to "Hẹn giờ và bấm giờ Đồng hồ chạy ngầm — kể cả khi tắt thông báo của nó",
        "Clock, battery and the buttons below. Off — tapping does nothing" to "Đồng hồ, pin và các nút bên dưới. Tắt — chạm vào sẽ không có phản hồi",
        "Close" to "Đóng",
        "Collapse the expanded island" to "Thu nhỏ Viên thuốc khi đang mở rộng",
        "Color" to "Màu sắc",
        "Color and glass" to "Màu sắc & hiệu ứng kính",
        "Color from artwork" to "Lấy màu từ ảnh bìa",
        "Color, outline and shadow" to "Màu sắc, đường viền và bóng đổ",
        "Connected" to "Đã kết nối",
        "Connections" to "Kết nối",
        "Content hidden" to "Nội dung đã ẩn",
        "Controls" to "Điều khiển",
        "Copy all" to "Sao chép tất cả",
        "Couldn't check for updates" to "Không thể kiểm tra bản cập nhật",
        "Couldn't download. Check your internet and try again." to "Không thể tải xuống. Vui lòng kiểm tra mạng và thử lại.",
        "Couldn't send the reply" to "Không thể gửi phản hồi",
        "Demo" to "Bản xem thử (Demo)",
        "Description, new versions and source code" to "Mô tả, các phiên bản mới và mã nguồn",
        "Device unlock" to "Mở khóa thiết bị",
        "Disconnected" to "Đã ngắt kết nối",
        "Do Not Disturb" to "Không làm phiền",
        "Doesn't look like a payment" to "Không giống một giao dịch thanh toán",
        "Done" to "Xong",
        "Double tap" to "Chạm đúp",
        "Downloading…" to "Đang tải xuống…",
        "Downloads and installs in one tap" to "Tải xuống và cài đặt chỉ với một chạm",
        "Downloads and uploads in Google Play, Telegram and other apps" to "Tải xuống và tải lên trong Google Play, Telegram và các ứng dụng khác",
        "Duration" to "Thời lượng",
        "Capsule VN" to "Capsule VN",
        "Dynamic cards" to "Thẻ linh hoạt",
        "Easter eggs" to "Trứng phục sinh (Bí mật)",
        "Effects" to "Hiệu ứng",
        "Elasticity" to "Độ đàn hồi",
        "English" to "English",
        "Equalizer frame rate" to "Tốc độ khung hình bộ cân bằng nhạc",
        "Event" to "Sự kiện",
        "Event display" to "Hiển thị sự kiện",
        "Events" to "Sự kiện",
        "Example: a new version arrives" to "Ví dụ: khi có phiên bản mới",
        "Expand" to "Mở rộng",
        "Expand the capsule, up — collapse" to "Vuốt xuống mở rộng viên thuốc, vuốt lên — thu nhỏ",
        "Expanded island" to "Viên thuốc mở rộng",
        "Expansion style" to "Kiểu mở rộng",
        "Experimental" to "Thử nghiệm",
        "Features" to "Tính năng",
        "Feedback" to "Góp ý & phản hồi",
        "Find Capsule VN and choose “Don't optimize” or “Unrestricted”" to "Tìm Capsule VN và chọn “Không tối ưu hóa” hoặc “Không hạn chế”",
        "First turn on the service in Accessibility" to "Trước tiên hãy bật dịch vụ trong Hỗ trợ tiếp cận",
        "Flashlight" to "Đèn pin",
        "Flashlight controls" to "Điều khiển đèn pin",
        "For a waterdrop camera: the island sits against the top of the screen and covers the notch" to "Đối với camera giọt nước: Viên thuốc áp sát đỉnh màn hình và che phần khuyết",
        "From" to "Từ",
        "From the open app" to "Từ ứng dụng đang mở",
        "General" to "Chung",
        "Gestures" to "Cử chỉ",
        "GitHub page" to "Trang GitHub",
        "Glass density" to "Độ trong suốt chất lỏng",
        "Glass with highlights and a glowing edge, like in iOS. Highlights move when you tilt the phone" to "Kính bóng bẩy với viền phát sáng, như trên iOS. Vệt sáng di chuyển khi nghiêng điện thoại",
        "Glow" to "Phát sáng",
        "Go straight" to "Đi thẳng",
        "Google and Yandex Maps, Navigator, 2GIS, Yandex Go, Uber and others — distance and time in the capsule, details when expanded" to "Google Maps, bản đồ điều hướng, gọi xe... — khoảng cách và thời gian trên viên thuốc, chi tiết khi mở rộng",
        "Got a minute?" to "Bạn có rảnh một phút không?",
        "Got it" to "Đã hiểu",
        "Grayed-out switch on Android 13+, battery and autostart" to "Nút bị mờ trên Android 13+, pin và tự khởi chạy",
        "Haptics" to "Rung phản hồi (Haptics)",
        "Headphone names when they connect" to "Tên tai nghe khi kết nối",
        "Headphones" to "Tai nghe",
        "Headphones connected" to "Đã kết nối tai nghe",
        "Headphones disconnected" to "Đã ngắt kết nối tai nghe",
        "Height" to "Chiều cao",
        "Help" to "Trợ giúp",
        "Hi! This is how a notification looks in the island" to "Xin chào! Đây là cách thông báo hiển thị trên Viên thuốc",
        "Hide" to "Ẩn",
        "Hide in landscape" to "Ẩn khi xoay ngang",
        "Hide the empty capsule" to "Ẩn viên thuốc khi trống",
        "Hide the player in its own app" to "Ẩn trình phát khi đang mở chính ứng dụng phát nhạc đó",
        "Hint when opening a bank" to "Gợi ý khi mở ứng dụng ngân hàng",
        "Hold near reader" to "Chạm vào đầu đọc thẻ",
        "Hold near reader as soon as you open a bank or wallet app, not only on the payment screen" to "Hiện chạm vào đầu đọc ngay khi mở ví điện tử/ngân hàng, không chỉ trên màn hình thanh toán",
        "Hold near reader, then Paid with the amount" to "Hiện chạm vào đầu đọc, sau đó hiện Đã thanh toán kèm số tiền",
        "Home" to "Trang chủ",
        "Horizontal" to "Ngang",
        "How are you?" to "Bạn khỏe không?",
        "How it works" to "Cách thức hoạt động",
        "How to turn the island on with Android 13+" to "Cách bật Viên thuốc trên Android 13+",
        "If it doesn't turn on" to "Nếu không bật được",
        "If the installer didn't open, allow the island to install apps and tap Install again." to "Nếu trình cài đặt không mở, hãy cấp quyền cài đặt ứng dụng cho Viên thuốc rồi bấm Cài đặt lại.",
        "If the island disappears, allow the app to autostart and remove battery restrictions — otherwise the firmware kills the service." to "Nếu Viên thuốc bị tự động biến mất, hãy cho phép ứng dụng tự khởi chạy và chuyển chế độ pin sang Không hạn chế — nếu không hệ điều hành Samsung One UI sẽ tắt dịch vụ chạy ngầm.",
        "If the island still disappears" to "Nếu Viên thuốc vẫn bị biến mất",
        "If the switch is grayed out (Android 13+)" to "Nếu công tắc bị mờ (Android 13+)",
        "If you turn off a messenger's pop-ups and then turn the island off or hide it, messages will only be in the shade." to "Nếu bạn tắt thông báo nổi của ứng dụng nhắn tin mà sau đó lại tắt hoặc ẩn Viên thuốc, tin nhắn sẽ chỉ nằm trong thanh thông báo.",
        "In Android's battery saver mode the island skips live effects by itself" to "Ở chế độ tiết kiệm pin của Android, Viên thuốc sẽ tự động bỏ qua các hiệu ứng động",
        "Incoming call" to "Cuộc gọi đến",
        "Incoming calls with answer buttons, and the call time" to "Cuộc gọi đến kèm nút trả lời và thời lượng cuộc gọi",
        "Incoming calls, names from contacts, answer button" to "Cuộc gọi đến, tên từ danh bạ, nút trả lời",
        "Install" to "Cài đặt",
        "Instructions" to "Hướng dẫn",
        "Island" to "Viên thuốc",
        "Island expansion" to "Mở rộng Viên thuốc",
        "Island on" to "Viên thuốc đang bật",
        "Island running" to "Viên thuốc đang hoạt động",
        "Its own banner doesn't pop up ✓" to "Không bị hiện thông báo nổi trùng lặp ✓",
        "Keep the island loaded" to "Giữ Viên thuốc luôn chạy",
        "Keep the island running" to "Giữ Viên thuốc hoạt động ổn định",
        "Keeps Android from closing the island when it's swiped away from recent apps" to "Ngăn Android đóng Viên thuốc khi vuốt đóng ứng dụng khỏi danh sách gần đây",
        "Language" to "Ngôn ngữ",
        "Later" to "Để sau",
        "Left — next track, right — previous" to "Trái — bài kế tiếp, phải — bài trước",
        "Like a notch" to "Kiểu tai thỏ (khuyết đỉnh)",
        "Liquid expansion" to "Mở rộng dạng chất lỏng",
        "Live equalizer" to "Bộ cân bằng sóng nhạc động",
        "Lock it in recent apps" to "Khóa trong ứng dụng gần đây",
        "Lock on the lock screen" to "Khóa trên màn hình khóa",
        "Long press" to "Nhấn giữ",
        "Look on GitHub every 3 hours for a new version" to "Kiểm tra phiên bản mới trên GitHub mỗi 3 giờ",
        "Looking for lyrics…" to "Đang tìm lời bài hát…",
        "Low Battery" to "Pin yếu",
        "Low battery" to "Pin yếu",
        "Lyrics" to "Lời bài hát",
        "Lyrics are looked up by title, artist and track length: first on LRCLIB (lrclib.net), then on NetEase, which has a lot of Russian music. If there are no synced lyrics anywhere, plain lyrics are spread over the length of the song. While searching, the player says “Looking for lyrics…”, and if nothing is found — “No lyrics found”. Only the title and artist are sent to the server. Works with any player that shows a notification with controls: Spotify, YouTube Music, Yandex Music, VK and others." to "Lời bài hát được tìm kiếm theo tên bài hát, nghệ sĩ và thời lượng: trước tiên trên LRCLIB (lrclib.net), sau đó là NetEase. Nếu không có lời đồng bộ, lời bài hát thông thường sẽ được phân bổ theo độ dài bài hát. Trong lúc tìm kiếm, trình phát sẽ hiện “Đang tìm lời bài hát…”, và nếu không tìm thấy sẽ báo “Không tìm thấy lời bài hát”. Chỉ có tên bài hát và nghệ sĩ được gửi lên máy chủ. Tính năng này tương thích với mọi ứng dụng phát nhạc có thanh điều khiển trên thông báo (Spotify, YouTube Music, Zing MP3, Apple Music...).",
        "Make a U-turn" to "Quay đầu xe",
        "Manual" to "Thủ công",
        "Mark as read" to "Đánh dấu đã đọc",
        "Message" to "Tin nhắn",
        "Messages from Telegram, WhatsApp, email and other apps as a card in the island, like on iPhone" to "Tin nhắn từ Telegram, Zalo, Messenger, WhatsApp, email... dưới dạng thẻ trên Viên thuốc, như trên iPhone",
        "Messages show up twice?" to "Tin nhắn bị hiện 2 lần?",
        "Messengers" to "Ứng dụng nhắn tin",
        "Modes" to "Chế độ",
        "Motion" to "Chuyển động",
        "Music and calls" to "Nhạc & cuộc gọi",
        "Music, calls, timers, flashlight, navigation" to "Nhạc, cuộc gọi, hẹn giờ, đèn pin, điều hướng",
        "NFC waves around the island while waiting for the reader, and a burst of sparks on Paid" to "Sóng NFC quanh Viên thuốc khi chờ đầu đọc thẻ, và hiệu ứng pháo hoa khi Đã thanh toán",
        "Navigation" to "Điều hướng & Bản đồ",
        "Navigation diagnostics" to "Chẩn đoán điều hướng",
        "Navigation, taxi and delivery" to "Điều hướng, gọi xe và giao hàng",
        "Needed for music, messages, progress and timers" to "Cần thiết cho nhạc, tin nhắn, tiến trình và hẹn giờ",
        "Needed for the music, download progress and Google Clock timer and stopwatch cards. The island doesn't store or send notifications anywhere." to "Cần thiết cho thẻ âm nhạc, tiến trình tải xuống và đồng hồ hẹn giờ/bấm giờ. Viên thuốc hoàn toàn không lưu trữ và không gửi thông báo của bạn đi bất cứ đâu.",
        "Needed:" to "Bắt buộc:",
        "Needed: " to "Cần bật: ",
        "Needed: without it there's no island" to "Bắt buộc: không có quyền này sẽ không có Viên thuốc",
        "New" to "Mới",
        "No" to "Không",
        "No Clock app found" to "Không tìm thấy ứng dụng Đồng hồ",
        "No amount — not a payment" to "Không có số tiền — không phải thanh toán",
        "No dialer found — call yourself and it will show up here" to "Không tìm thấy trình gọi điện — hãy gọi thử để xem hiển thị ở đây",
        "No list of changes found" to "Không tìm thấy danh sách thay đổi",
        "No lyrics found" to "Không tìm thấy lời bài hát",
        "No message has reached the island yet" to "Chưa có tin nhắn nào gửi tới Viên thuốc",
        "Nothing yet: no navigation, taxi or delivery notification has arrived." to "Chưa có gì: chưa nhận được thông báo điều hướng, gọi xe hoặc giao hàng.",
        "Nothing yet: the island hasn't seen a payment screen or a bank notification." to "Chưa có gì: Viên thuốc chưa nhận diện được màn hình thanh toán hoặc thông báo ngân hàng.",
        "Notification access" to "Quyền truy cập thông báo",
        "Notification buttons" to "Các nút trên thông báo",
        "Notifications" to "Thông báo",
        "Off" to "Tắt",
        "Off: Reduce effects is on" to "Tắt: Do tính năng Giảm hiệu ứng đang bật",
        "On" to "Bật",
        "On a schedule" to "Theo lịch trình",
        "On the Home tab open Permissions → Calls and allow access to the phone, call log and contacts. Then the island shows incoming calls with any dialer, shows the name from your contacts, and the answer and decline buttons work." to "Tại tab Trang chủ, mở Quyền truy cập → Cuộc gọi và cấp quyền điện thoại, nhật ký cuộc gọi và danh bạ. Khi đó Viên thuốc sẽ hiển thị cuộc gọi đến từ bất kỳ trình gọi điện nào, hiện tên người gọi từ danh bạ, và các nút nhận/từ chối cuộc gọi sẽ hoạt động.",
        "On the road" to "Trên đường đi",
        "On — music, messages, progress and timers" to "Bật — nhạc, tin nhắn, tiến trình và hẹn giờ",
        "On — the island draws over the status bar" to "Bật — Viên thuốc vẽ phủ lên trên thanh trạng thái",
        "Only an accessibility service can draw the capsule next to the camera. Open Accessibility → Capsule VN and turn it on." to "Chỉ có Dịch vụ hỗ trợ tiếp cận mới có thể vẽ viên thuốc bao quanh camera. Hãy vào Cài đặt → Hỗ trợ tiếp cận → Ứng dụng đã cài đặt → Capsule VN và bật lên.",
        "Only by tapping outside or swiping up" to "Chỉ khi chạm ra ngoài hoặc vuốt lên",
        "Open" to "Mở",
        "Open Accessibility" to "Mở Hỗ trợ tiếp cận",
        "Open App info" to "Mở Thông tin ứng dụng",
        "Open Settings → Accessibility → Installed apps → Capsule VN and turn the service on. Only such a service can draw over the status bar, next to the camera." to "Mở Cài đặt → Hỗ trợ tiếp cận → Ứng dụng đã cài đặt → Capsule VN và gạt bật dịch vụ. Chỉ có dịch vụ này mới có quyền vẽ lớp phủ lên thanh trạng thái, ngay cạnh camera.",
        "Open notification access" to "Mở Quyền truy cập thông báo",
        "Open quick settings fully, tap the pencil and drag the Island tile to the others." to "Mở hết thanh Cài đặt nhanh, bấm vào biểu tượng bút chì và kéo phím tắt Viên thuốc vào danh sách.",
        "Open recent apps and pull the Capsule VN card down or long-press it — a lock appears. A locked app isn't closed when you clear recent apps." to "Mở ứng dụng gần đây và kéo thẻ Capsule VN xuống hoặc nhấn giữ — biểu tượng ổ khóa sẽ xuất hiện để không bị tắt khi xóa danh sách gần đây.",
        "Open recent apps, long-press the Capsule VN card and tap the lock. HyperOS and MIUI don't close a locked app, even when you clear recent apps." to "Mở ứng dụng gần đây, nhấn giữ thẻ Capsule VN và bấm vào biểu tượng ổ khóa để không bị đóng khi dọn dẹp bộ nhớ.",
        "Open recent apps, tap the icon above the Capsule VN card and choose “Keep open”." to "Mở ứng dụng gần đây, chạm vào biểu tượng ứng dụng phía trên thẻ Capsule VN và chọn “Giữ mở” (Keep open).",
        "Open recent apps, tap “⋮” above the Capsule VN card (or pull it down) and choose “Lock”." to "Mở ứng dụng gần đây, bấm vào dấu “⋮” phía trên thẻ Capsule VN (hoặc kéo thẻ xuống) và chọn “Khóa”.",
        "Opens Accessibility — Capsule VN → turn on" to "Mở Hỗ trợ tiếp cận — Capsule VN → Bật lên",
        "Other" to "Khác",
        "Outline" to "Đường viền",
        "Over Bluetooth, with the charge if it's known" to "Qua Bluetooth, kèm % pin nếu thiết bị hỗ trợ",
        "Paid" to "Đã thanh toán",
        "Pause and reset in the island press Clock's own buttons. Off — they just open Clock" to "Nút tạm dừng và đặt lại trên Viên thuốc sẽ bấm trực tiếp nút của Đồng hồ. Tắt — chỉ mở Đồng hồ",
        "Paused" to "Đã tạm dừng",
        "Pay" to "Thanh toán",
        "Pay with your phone and come back here: below is what the island saw (the payment screen, a bank notification or SMS) and how it understood it. If “Paid” didn't show, tap “Copy all” and send the text. Entries stay only in the phone's memory and are gone after a restart." to "Hãy thực hiện một giao dịch thanh toán chạm rồi quay lại đây: bên dưới là những gì Viên thuốc nhận diện được (màn hình thanh toán, thông báo hoặc SMS ngân hàng). Nếu không hiện “Đã thanh toán”, bấm “Sao chép tất cả” để gửi báo lỗi. Dữ liệu chỉ lưu tạm trong bộ nhớ RAM và tự xóa khi khởi động lại.",
        "Payment" to "Thanh toán",
        "Payment diagnostics" to "Chẩn đoán thanh toán",
        "Payment screen" to "Màn hình thanh toán",
        "Payment waves" to "Sóng thanh toán",
        "Payments" to "Thanh toán",
        "Permissions" to "Quyền truy cập",
        "Place the capsule over the camera" to "Đặt viên thuốc khớp vị trí camera",
        "Player controls" to "Điều khiển trình phát",
        "Pops up over the island — tap and turn off “Pop on screen”" to "Bị hiện đè lên Viên thuốc — chạm vào và tắt “Bật lên trên màn hình”",
        "Position and size" to "Vị trí & kích thước",
        "Progress indicator" to "Chỉ báo tiến trình",
        "Project" to "Dự án",
        "Pull down" to "Kéo xuống",
        "Push sideways" to "Đẩy sang bên",
        "Reduce effects" to "Giảm hiệu ứng",
        "Reduce effects is on in Island expansion — while it's on, there are no effects" to "Giảm hiệu ứng đang bật trong Mở rộng Viên thuốc — khi bật, các hiệu ứng sẽ tắt",
        "Replies and payments" to "Trả lời & thanh toán",
        "Reply" to "Trả lời",
        "Reply with typing right in the island, and Mark as read" to "Gõ phím trả lời ngay trên Viên thuốc, và Đánh dấu đã đọc",
        "Reset" to "Đặt lại",
        "Reset all settings" to "Đặt lại toàn bộ cài đặt",
        "Reset settings?" to "Đặt lại cài đặt?",
        "Restricted settings, permissions, battery" to "Cài đặt bị hạn chế, quyền hạn, pin",
        "Ring" to "Chuông",
        "Ring mode" to "Chế độ chuông",
        "Ring, vibrate or silent" to "Chuông, rung hoặc im lặng",
        "Russian" to "Tiếng Nga",
        "Save" to "Lưu",
        "Schedule" to "Lịch trình",
        "Screen:" to "Màn hình:",
        "Screen: " to "Màn hình: ",
        "Second activity alongside" to "Hoạt động thứ hai bên cạnh",
        "Sent" to "Đã gửi",
        "Set up" to "Thiết lập",
        "Shadow" to "Bóng đổ",
        "Shake" to "Rung lắc",
        "Show the capsule only when it has something in it" to "Chỉ hiển thị viên thuốc khi có nội dung hoạt động",
        "Silent" to "Im lặng",
        "Single tap" to "Chạm một lần",
        "Size with activity" to "Kích thước khi có hoạt động",
        "Size, colors, animations, cards and events go back to their defaults." to "Kích thước, màu sắc, hiệu ứng, thẻ và sự kiện sẽ trở về mặc định ban đầu.",
        "So only the island shows calls, timers and battery" to "Để chỉ có Viên thuốc hiển thị cuộc gọi, hẹn giờ và pin",
        "So the island shows incoming calls" to "Để Viên thuốc hiển thị các cuộc gọi đến",
        "Start a navigation, taxi or delivery app and come back here: below is what the app sends in its notification. If the distance or the turn arrow doesn't show in the island, tap “Copy all” and send this text." to "Hãy bật ứng dụng bản đồ, gọi xe hoặc giao hàng rồi quay lại đây: bên dưới là những gì ứng dụng gửi trong thông báo. Nếu khoảng cách hoặc mũi tên rẽ không hiển thị trên Viên thuốc, hãy bấm “Sao chép tất cả” và gửi nội dung này để báo lỗi.",
        "Stays out of games and videos" to "Tự ẩn khi chơi game hoặc xem video",
        "Stop" to "Dừng",
        "Stopwatch" to "Đồng hồ bấm giờ",
        "Surprises" to "Điều bất ngờ",
        "Swipes" to "Vuốt",
        "Switch grayed out? Instructions" to "Công tắc bị mờ? Xem hướng dẫn",
        "Synced lyrics in the expanded player" to "Lời bài hát đồng bộ trong trình phát mở rộng",
        "System" to "Hệ thống",
        "System UI" to "Giao diện hệ thống",
        "System banners" to "Biểu ngữ hệ thống",
        "System default" to "Theo hệ thống",
        "Tap and turn off “Pop on screen”" to "Chạm vào và tắt “Bật lên trên màn hình”",
        "Tap — expand, pull down — expand, pull up or touch outside the island — collapse, push sideways — next or previous track, tap the bubble — expand the second activity." to "Chạm — mở rộng, kéo xuống — mở rộng, vuốt lên hoặc chạm ra ngoài Viên thuốc — thu nhỏ, đẩy sang bên — bài hát kế tiếp/trước đó, chạm vào bong bóng nhỏ bên cạnh — mở rộng hoạt động thứ hai.",
        "Tap — the example shows up in the island at the top of the screen" to "Chạm — ví dụ sẽ hiển thị trên Viên thuốc ở đầu màn hình",
        "Tapping the empty island" to "Khi chạm vào Viên thuốc trống",
        "Taps" to "Chạm",
        "Taps and swipes on the capsule" to "Chạm và vuốt trên viên thuốc",
        "Text lines" to "Dòng văn bản",
        "The capsule is drawn by an accessibility service, music data comes from the system media sessions, progress and timers from notifications. Animations run only while the capsule shows something; Reduce effects turns off the equalizer and the jelly." to "Viên thuốc được hiển thị bởi dịch vụ hỗ trợ tiếp cận, thông tin bài hát lấy từ phiên phát nhạc hệ thống, tiến trình và hẹn giờ lấy từ thông báo. Hiệu ứng động chỉ hoạt động khi có nội dung; tùy chọn “Giảm hiệu ứng” sẽ tắt sóng nhạc động và hiệu ứng co giãn đàn hồi.",
        "The capsule shakes when the ring mode changes, and a ringing timer's bell swings" to "Viên thuốc rung lên khi đổi chế độ chuông, và chuông hẹn giờ lắc lư",
        "The capsule shakes, the bell swings" to "Viên thuốc rung lên, chuông lắc lư",
        "The capsule shows over the camera" to "Viên thuốc hiển thị bao quanh camera",
        "The charging animation drawn by the firmware itself (Xiaomi, Samsung and others) is turned off in the phone's lock screen or battery settings, if the manufacturer allows it." to "Hiệu ứng sạc pin mặc định do hệ điều hành vẽ (Samsung, Xiaomi...) có thể tắt trong cài đặt màn hình khóa hoặc cài đặt pin của điện thoại, nếu nhà sản xuất cho phép.",
        "The color of the Reply and Mark as read buttons is in Features → Notifications." to "Màu của nút Trả lời và Đánh dấu đã đọc nằm ở Tính năng → Thông báo.",
        "The content grows together with the island and emerges from a blur" to "Nội dung mở rộng cùng với Viên thuốc và hiện rõ từ hiệu ứng mờ",
        "The current line in the expanded player fills with white as it's sung" to "Câu hát hiện tại trong trình phát mở rộng chuyển sang màu trắng theo nhịp bài hát",
        "The expanded island collapses with a tap or a touch outside it" to "Viên thuốc mở rộng sẽ thu nhỏ lại khi chạm vào hoặc chạm ra ngoài",
        "The incoming calls channel. The island turns the screen on for calls itself" to "Kênh cuộc gọi đến. Viên thuốc sẽ tự sáng màn hình khi có cuộc gọi",
        "The island doesn't store or send your notifications, calls or screens: permissions are only used to show events" to "Viên thuốc không lưu trữ hay gửi thông báo, cuộc gọi hay màn hình của bạn đi đâu: các quyền chỉ dùng để hiển thị sự kiện",
        "The island hides for a moment, so it's not in the shot (Android 9+)" to "Viên thuốc tự ẩn tạm thời để không bị dính vào ảnh chụp màn hình (Android 9+)",
        "The island is already off" to "Viên thuốc hiện đã tắt",
        "The island is running" to "Viên thuốc đang hoạt động",
        "The island recognizes one special song and puts on a little show to its beat. Which one? Try to guess" to "Viên thuốc nhận diện một bài hát đặc biệt và biểu diễn theo nhịp điệu. Đó là bài nào? Hãy thử đoán xem",
        "The island stays even if you swipe the app away from recent apps. Android may show a quiet notification" to "Viên thuốc vẫn chạy ngay cả khi bạn vuốt đóng app trong danh sách gần đây. Android có thể hiện một thông báo yên lặng",
        "The island's content flows with a blur, like on iPhone (Android 12+)" to "Nội dung của Viên thuốc chuyển động mượt mà kèm hiệu ứng mờ, như trên iPhone (Android 12+)",
        "The music and call bars dance. Off — they stand still, which saves power" to "Cột sóng nhạc và cuộc gọi nhún nhảy. Tắt — đứng yên để tiết kiệm pin",
        "The music card hides while the player itself is open" to "Thẻ phát nhạc tự ẩn khi ứng dụng nghe nhạc đang mở",
        "The new version card: download, then Updated" to "Thẻ phiên bản mới: tải xuống, sau đó báo Đã cập nhật",
        "The quick settings tile" to "Phím tắt trên Cài đặt nhanh",
        "The release has no APK. Open the release page." to "Bản phát hành không có file APK. Hãy mở trang phát hành.",
        "The service is starting…" to "Dịch vụ đang khởi động…",
        "The tile is already in quick settings" to "Phím tắt đã có trong Cài đặt nhanh",
        "The turn arrow and meters to it" to "Mũi tên chỉ hướng và khoảng cách còn lại",
        "The update file is damaged" to "Tệp cập nhật bị hỏng",
        "Thickness" to "Độ dày",
        "This notification keeps the system from closing the island" to "Thông báo này giúp hệ thống không tắt Viên thuốc ngầm",
        "Time's up" to "Hết giờ",
        "Timer" to "Hẹn giờ",
        "Timer and alarm alerts" to "Cảnh báo hẹn giờ và báo thức",
        "Timer and stopwatch" to "Hẹn giờ & đồng hồ bấm giờ",
        "Timer from assistant" to "Hẹn giờ từ trợ lý ảo",
        "Timers and alarm" to "Hẹn giờ & báo thức",
        "Timers and alarms" to "Hẹn giờ & báo thức",
        "Tip: don't turn off Clock's notifications completely — choose “Silent” and “Minimize” (or turn off only “Pop on screen”). Then the timer and stopwatch show up in the island instantly, and its buttons work without opening Clock. If the notifications are fully off, the island reads the timer from the Clock screen and presses the buttons itself, opening the app for a moment." to "Mẹo: đừng tắt hoàn toàn thông báo của Đồng hồ — hãy chọn “Im lặng” và “Thu nhỏ” (hoặc chỉ tắt “Bật lên trên màn hình”). Khi đó hẹn giờ và bấm giờ sẽ hiện ngay trên Viên thuốc, và các nút điều khiển sẽ hoạt động mà không cần mở Đồng hồ.",
        "To" to "Đến",
        "To quick settings — one tap" to "Đến Cài đặt nhanh — chỉ một chạm",
        "To see headphones connect" to "Để hiển thị khi tai nghe kết nối",
        "Top offset" to "Khoảng cách từ mép trên",
        "Try it" to "Thử nghiệm",
        "Turn it back on in Accessibility or with the tile" to "Bật lại trong Hỗ trợ tiếp cận hoặc qua phím tắt",
        "Turn it on for Capsule VN — then it starts by itself after a reboot or being closed" to "Bật quyền cho Capsule VN — để app tự khởi động lại sau khi khởi động máy hoặc bị tắt",
        "Turn left" to "Rẽ trái",
        "Turn on the island" to "Bật Viên thuốc",
        "Turn on the screen" to "Bật sáng màn hình",
        "Turn right" to "Rẽ phải",
        "Turn right onto Main Street" to "Rẽ phải vào đường chính",
        "Turn the Capsule VN service off and on again" to "Tắt và bật lại dịch vụ Capsule VN",
        "Turn the island off now" to "Tắt Viên thuốc ngay bây giờ",
        "Turn the island on" to "Bật Viên thuốc",
        "Turns the screen off like the power button (Android 9+)" to "Tắt màn hình tương tự phím nguồn (Android 9+)",
        "USB charging and other system messages" to "Sạc USB và các thông báo hệ thống khác",
        "USB device" to "Thiết bị USB",
        "USB device connected" to "Đã kết nối thiết bị USB",
        "USB device disconnected" to "Đã ngắt kết nối thiết bị USB",
        "Under a messenger notification you get the notification's own buttons. “Reply” opens an input field right in the island. When a payment app shows its phone payment screen, the island shows “Hold near reader”, and after paying — a check mark with the amount. You can try all of this: Home → Try it." to "Bên dưới thông báo tin nhắn sẽ có các nút tương tác nhanh. “Trả lời” sẽ mở ô nhập văn bản ngay trên Viên thuốc. Khi mở ứng dụng ví/ngân hàng, Viên thuốc sẽ hiện “Chạm vào đầu đọc”, và sau khi quẹt thẻ sẽ hiện dấu tích xanh kèm số tiền. Bạn có thể thử tất cả tại: Trang chủ → Thử nghiệm.",
        "Unplugged" to "Đã rút sạc",
        "Unrestricted battery" to "Pin không hạn chế",
        "Up to date" to "Đã là mới nhất",
        "Update" to "Cập nhật",
        "Updates" to "Bản cập nhật",
        "Version 1.7 is available" to "Đã có phiên bản 1.7",
        "Vibrate" to "Rung",
        "Vietnamese" to "Tiếng Việt",
        "Wake up" to "Thức dậy",
        "Wake-up time and how long you can sleep" to "Thời gian thức dậy và thời gian bạn có thể ngủ",
        "What a navigation, taxi or delivery app sends — if the distance doesn't show" to "Thông tin ứng dụng bản đồ, gọi xe hoặc giao hàng gửi tới — nếu không thấy khoảng cách",
        "What it does" to "Tính năng",
        "What it does and where the idea comes from" to "Ứng dụng làm được gì và ý tưởng bắt nguồn từ đâu",
        "What the island saw when you paid — if “Paid” didn't show" to "Thông tin Viên thuốc nhận diện khi thanh toán — nếu không hiện “Đã thanh toán”",
        "What the island shows" to "Những gì Viên thuốc hiển thị",
        "What's new" to "Có gì mới",
        "When a phone payment is on screen — Mir Pay, T-Pay, SberPay, Yandex Pay and others. Stays until you pay or leave the app" to "Khi màn hình thanh toán chạm xuất hiện (Google Wallet, Samsung Pay...). Giữ nguyên cho đến khi bạn thanh toán hoặc rời app",
        "When a timer, alarm or incoming call rings, the screen turns on and stays on while it rings" to "Khi có hẹn giờ, báo thức hoặc cuộc gọi đến, màn hình sẽ bật sáng trong lúc chuông kêu",
        "When adaptive brightness is turned on or off" to "Khi bật hoặc tắt độ sáng thích ứng",
        "When music is playing and the player is in the background" to "Khi đang phát nhạc và trình phát chạy ngầm",
        "When opening, closing and tapping the capsule" to "Khi mở, đóng và chạm vào viên thuốc",
        "When the battery drops to 5%" to "Khi pin giảm xuống 5%",
        "When the device is charging" to "Khi thiết bị đang sạc",
        "When the flashlight is on" to "Khi bật đèn pin",
        "When to show" to "Khi nào hiển thị",
        "When to show and collapse the island, haptics" to "Khi nào hiển thị và thu nhỏ Viên thuốc, phản hồi rung",
        "Where the idea comes from" to "Ý tưởng bắt nguồn từ đâu",
        "While a navigation app is open (2GIS, Yandex Navigator, Maps and others), the island reads the distance to the next turn, the street and the arrival time from its screen, and recognizes the turn arrow from the picture. Other apps' screens aren't read. If the island disappears after an update, turn it off and on again in Accessibility." to "Khi ứng dụng điều hướng mở (Google Maps và các bản đồ khác), Viên thuốc sẽ nhận diện khoảng cách đến ngã rẽ, tên đường và thời gian đến nơi từ màn hình, đồng thời nhận diện mũi tên chỉ hướng. Viên thuốc không đọc màn hình của các ứng dụng khác. Nếu Viên thuốc biến mất sau khi cập nhật, hãy tắt và bật lại trong Hỗ trợ tiếp cận.",
        "While music plays, the outline takes the artwork's color" to "Khi phát nhạc, viền ngoài sẽ đổi màu theo ảnh bìa bài hát",
        "While the phone is locked, the capsule shows a lock" to "Khi điện thoại đang khóa, viên thuốc hiển thị biểu tượng ổ khóa",
        "Wi-Fi connected" to "Đã kết nối Wi-Fi",
        "Wi-Fi disconnected" to "Đã ngắt kết nối Wi-Fi",
        "Width" to "Chiều rộng",
        "With Reply and Mark as read buttons" to "Kèm các nút Trả lời và Đánh dấu đã đọc",
        "With two activities, the second one is a small circle next to the capsule, like on iPhone" to "Khi có 2 hoạt động cùng lúc, hoạt động thứ 2 là hình tròn nhỏ bên cạnh viên thuốc, như trên iPhone",
        "Yes" to "Có",
        "You can answer or decline" to "Bạn có thể trả lời hoặc từ chối",
        "You're filled with DETERMINATION." to "Bạn tràn ngập SỰ QUYẾT TÂM.",
        "Your bank says it detected a third-party app or accessibility service?" to "Ngân hàng báo phát hiện ứng dụng bên thứ ba hoặc dịch vụ hỗ trợ tiếp cận?",
        "Your phone's firmware may stop the island in the background — then it disappears until you turn it on again" to "Hệ điều hành của máy có thể dừng Viên thuốc khi chạy ngầm — khi đó Viên thuốc sẽ biến mất cho đến khi bạn bật lại",
        "accessibility" to "hỗ trợ tiếp cận",
        "big text:" to "văn bản lớn:",
        "big text: " to "văn bản lớn: ",
        "blur" to "mờ",
        "buttons:" to "các nút:",
        "buttons: " to "các nút: ",
        "calls" to "cuộc gọi",
        "equalizer" to "sóng nhạc",
        "from the picture" to "từ hình ảnh",
        "glow" to "phát sáng",
        "in" to "còn",
        "layout:" to "bố cục:",
        "layout: " to "bố cục: ",
        "less than a minute" to "chưa đầy một phút",
        "no custom layout" to "không có bố cục tùy chỉnh",
        "not parsed: distance and time are in the fields" to "chưa phân tích: khoảng cách và thời gian nằm trong các trường dữ liệu",
        "not parsed: the notification isn't ongoing" to "chưa phân tích: thông báo không phải dạng liên tục",
        "notifications" to "thông báo",
        "now" to "bây giờ",
        "of sleep" to "ngủ",
        "payment waves" to "sóng thanh toán",
        "shake" to "rung",
        "sleep" to "ngủ",
        "subtext:" to "văn bản phụ:",
        "subtext: " to "văn bản phụ: ",
        "text:" to "văn bản:",
        "text: " to "nội dung: ",
        "title:" to "tiêu đề:",
        "title: " to "tiêu đề: ",
        "today" to "hôm nay",
        "tomorrow" to "ngày mai",
        "until alarm" to "cho đến báo thức",
        "“Battery low” — the Battery channel. Some firmwares don't let you change it" to "“Pin yếu” — kênh Thông báo Pin. Một số giao diện hệ thống không cho phép sửa đổi",
        "Message & Call cards" to "Thẻ tin nhắn & cuộc gọi",
        "Separate position, width & height controls with live simulation" to "Tùy chỉnh riêng vị trí trên/dưới, bề ngang & bề dọc kèm mô phỏng trực tiếp",
        "Message card" to "Thẻ tin nhắn",
        "Message card adjustments" to "Tùy chỉnh Thẻ tin nhắn",
        "Incoming call card adjustments" to "Tùy chỉnh Thẻ cuộc gọi đến",
        "Top / bottom position" to "Vị trí trên / dưới (Dọc)",
        "Horizontal width" to "Độ rộng bề ngang",
        "Vertical height offset" to "Độ rộng bề dọc (Chiều cao)",
        "Vertical height" to "Độ rộng bề dọc (Chiều cao)",
        "Default" to "Mặc định",
        "lines" to "dòng",
        "iOS optical 3D liquid glass lens with live refraction and specular highlights when tilting" to "Thấu kính chất lỏng 3D chuẩn iOS Liquid Glass với khúc xạ viền và phản quang sống động khi nghiêng máy",
        "Lens refraction" to "Độ cong khúc xạ viền 3D",
        "Specular & dispersion" to "Độ bóng gương & tán sắc",
        "“Set a timer for 5 minutes” from an assistant or widget can go to the island" to "“Đặt hẹn giờ 5 phút” từ trợ lý ảo hoặc tiện ích có thể chuyển thẳng lên Viên thuốc",
    )

    fun translate(en: String, fallbackVi: String): String {
        enMap[en]?.let { return it }
        if (fallbackVi != en) return fallbackVi
        return translateDynamic(en)
    }

    private fun translateDynamic(en: String): String {
        when {
            en.endsWith(" min") && en.removeSuffix(" min").toIntOrNull() != null ->
                return "${en.removeSuffix(" min")} phút"

            en.endsWith(" h") && en.removeSuffix(" h").toIntOrNull() != null ->
                return "${en.removeSuffix(" h")} giờ"

            en.matches(Regex("^\\d+\\s*h\\s*\\d+\\s*min$")) -> {
                val parts = en.split(" ")
                return "${parts[0]} giờ ${parts[2]} phút"
            }

            en.startsWith("Sleep — ") ->
                return "Giấc ngủ — " + en.removePrefix("Sleep — ")

            en.startsWith("In ") && !en.contains(":") ->
                return "Còn " + en.removePrefix("In ")

            en.startsWith("Flashlight · ") ->
                return "Đèn pin · " + en.removePrefix("Flashlight · ")

            en.contains(" · in ") -> {
                val idx = en.indexOf(" · in ")
                return en.substring(0, idx) + " · còn " + en.substring(idx + 6)
            }

            en.startsWith("Timer · ") ->
                return "Hẹn giờ · " + en.removePrefix("Timer · ")

            en.startsWith("Stopwatch · ") ->
                return "Bấm giờ · " + en.removePrefix("Stopwatch · ")

            en.startsWith("Arrive ") ->
                return "Đến nơi lúc " + en.removePrefix("Arrive ")

            en.startsWith("custom layout, texts: ") ->
                return "Bố cục tùy chỉnh, số văn bản: " + en.removePrefix("custom layout, texts: ")

            en.startsWith("couldn't read the layout: ") ->
                return "Không thể đọc bố cục: " + en.removePrefix("couldn't read the layout: ")

            en.startsWith("ongoing: ") -> {
                return en.replace("ongoing:", "Đang diễn ra:").replace("category:", "danh mục:")
            }

            en.startsWith("found: distance ") -> {
                return en.replace("found: distance", "Tìm thấy: khoảng cách")
                    .replace(", street", ", đường")
                    .replace(", arrival", ", đến nơi")
                    .replace(", turn", ", rẽ")
            }

            en.startsWith("From the screen: paid ") ->
                return "Từ màn hình: đã thanh toán " + en.removePrefix("From the screen: paid ")

            en.startsWith("Paid ") ->
                return "Đã thanh toán " + en.removePrefix("Paid ")

            en.startsWith("“Paid” on screen, but amounts: ") ->
                return "“Đã thanh toán” trên màn hình, nhưng số tiền: " + en.removePrefix("“Paid” on screen, but amounts: ")

            en.startsWith("Version ") && en.endsWith(" is available") -> {
                val ver = en.removePrefix("Version ").removeSuffix(" is available")
                return "Đã có phiên bản $ver"
            }

            en.startsWith("Installed: ") ->
                return "Đang cài đặt: " + en.removePrefix("Installed: ")

            en.startsWith("Downloading… ") ->
                return "Đang tải xuống… " + en.removePrefix("Downloading… ")

            en.startsWith("You have the latest version ") ->
                return "Bạn đang dùng phiên bản mới nhất " + en.removePrefix("You have the latest version ")

            en.startsWith("Installed version: ") ->
                return "Phiên bản đang cài: " + en.removePrefix("Installed version: ")

            en.startsWith("What's new in ") ->
                return "Có gì mới trong " + en.removePrefix("What's new in ")

            en.startsWith("Capsule ") && en.contains(" — fit it to the camera") -> {
                val dims = en.removePrefix("Capsule ").removeSuffix(" — fit it to the camera")
                return "Viên thuốc $dims — căn chỉnh vừa camera"
            }

            en.startsWith("Thickness ") && en.contains("width as without it") -> {
                return en.replace("Thickness", "Độ dày").replace("width as without it", "chiều rộng giữ nguyên")
            }

            en.startsWith("Thickness ") && en.contains("width ") -> {
                return en.replace("Thickness", "Độ dày").replace("width", "chiều rộng")
            }

            en.endsWith(". Also the glowing dot of the flashlight and navigation") -> {
                val prefix = en.removeSuffix(". Also the glowing dot of the flashlight and navigation")
                return "$prefix. Thêm chấm phát sáng của đèn pin và điều hướng"
            }

            en.startsWith("The island shows only from ") -> {
                return en.replace("The island shows only from", "Viên thuốc chỉ hiển thị từ").replace(" to ", " đến ")
            }

            en.startsWith("After ") && en.endsWith(" seconds without a touch") -> {
                val sec = en.removePrefix("After ").removeSuffix(" seconds without a touch")
                return "Sau $sec giây không chạm"
            }

            en.startsWith("Changes in version ") ->
                return "Các thay đổi trong phiên bản " + en.removePrefix("Changes in version ")

            en.startsWith("Version ") ->
                return "Phiên bản " + en.removePrefix("Version ")

            en.startsWith("Updated to ") ->
                return "Đã cập nhật lên " + en.removePrefix("Updated to ")

            en.startsWith("Downloaded ") && en.contains(" of ") && en.endsWith(" bytes") -> {
                val middle = en.removePrefix("Downloaded ").removeSuffix(" bytes")
                val parts = middle.split(" of ")
                return "Đã tải ${parts[0]} trên ${parts[1]} byte"
            }

            en.startsWith("Both the island and ") && en.contains(" itself show the message") -> {
                val app = en.substring(20, en.indexOf(" itself show the message"))
                return "Cả Viên thuốc và $app đều hiển thị thông báo. Hãy tắt mục “Bật lên trên màn hình” của ứng dụng này để tránh thông báo bị hiện trùng lặp."
            }

            en.startsWith("Alert when charging reaches ") ->
                return "Báo nhắc khi sạc đạt mức " + en.removePrefix("Alert when charging reaches ")
            en.startsWith("Currently active · ") ->
                return "Đang kích hoạt · " + en.removePrefix("Currently active · ")
            en.startsWith("Message ") && en.contains(" · Call ") ->
                return en.replace("Message ", "Tin nhắn ").replace(" · Call ", " · Cuộc gọi ")

            else -> return en
        }
    }
}
