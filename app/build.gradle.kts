plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.akira.island"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dynamicisland.vn"
        minSdk = 26
        targetSdk = 35
        versionCode = 17
        versionName = "1.6.1"
    }

    
    
    // Khóa ký cố định cho bản phát hành (để cập nhật OTA cài đè được). CI giải mã khóa từ GitHub Secrets
    // vào đường dẫn CAPSULE_KEYSTORE; không có biến này thì dùng khóa debug mặc định như trước.
    val capsuleKeystore = System.getenv("CAPSULE_KEYSTORE")?.let { file(it) }?.takeIf { it.exists() }
    signingConfigs {
        if (capsuleKeystore != null) {
            create("capsule") {
                storeFile = capsuleKeystore
                storePassword = System.getenv("CAPSULE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CAPSULE_KEY_ALIAS")
                keyPassword = System.getenv("CAPSULE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            if (capsuleKeystore != null) signingConfig = signingConfigs.getByName("capsule")
        }
        // Bản phát hành: R8 thu gọn & tối ưu mã, bỏ tài nguyên thừa, không gắn trình gỡ lỗi
        // -> khởi động nhanh hơn, mượt hơn, ít RAM hơn bản debug trước đây
        release {
            if (capsuleKeystore != null) signingConfig = signingConfigs.getByName("capsule")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }

    // "Có gì mới" trong app đọc trực tiếp CHANGELOG.md -> luôn đúng nội dung phiên bản đang cài
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/changelog").get().asFile)
}

val copyChangelog = tasks.register<Copy>("copyChangelog") {
    from(rootProject.file("CHANGELOG.md"))
    into(layout.buildDirectory.dir("generated/changelog"))
    rename { "changelog.md" }
}
tasks.named("preBuild") { dependsOn(copyChangelog) }

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    // Shizuku: cấp quyền 1 chạm cho Chế độ ngân hàng (không cần máy tính)
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.savedstate:savedstate-ktx:1.2.1")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    // Nạp sẵn hồ sơ tối ưu (baseline profile) của Compose khi cài APK ngoài Play -> mở app & bung Viên thuốc mượt ngay lần đầu
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
}
