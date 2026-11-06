-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.akira.island.data.**$$serializer { *; }
-keepclassmembers class com.akira.island.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.akira.island.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Shizuku: gọi newProcess qua phản chiếu cho Chế độ ngân hàng
-keep class rikka.shizuku.Shizuku { *; }

# Toàn bộ cấu hình (lưu dạng JSON, sao lưu / khôi phục): giữ nguyên tên lớp & trường cho chắc chắn
-keep class com.akira.island.data.** { *; }
-keepclassmembers enum * { *; }

# Thư viện Shizuku tham chiếu API ẩn của hệ thống
-dontwarn rikka.**
-dontwarn moe.shizuku.**
