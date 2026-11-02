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
