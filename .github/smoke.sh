#!/usr/bin/env bash
# Chạy thử Capsule VN trên máy ảo Android (CI): cài APK, mở app, bật Viên thuốc, gửi tin nhắn thử,
# chạm mở rộng, chụp màn hình, đo RAM / khung hình và bắt lỗi crash. So sánh bản debug cũ với bản release.
set -u
PKG=com.dynamicisland.vn
OUT=smoke
mkdir -p "$OUT"
fail=0

adb shell svc power stayon true || true
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true
SIZE=$(adb shell wm size | tail -n1 | awk '{print $NF}' | tr -d '\r')
W=${SIZE%x*}
echo "Màn hình máy ảo: $SIZE"

thumb() { # in ảnh thu nhỏ dạng base64 vào nhật ký để xem lại được
  local png=$1
  if command -v convert >/dev/null 2>&1; then
    convert "$png" -resize 300x -quality 55 "${png%.png}.jpg" 2>/dev/null &&
      { echo "=====IMG ${png##*/}"; base64 -w0 "${png%.png}.jpg"; echo; echo "=====END"; }
  fi
}

run() { # $1 = apk, $2 = nhãn
  local apk=$1 label=$2
  echo "::group::Chạy thử $label"
  adb uninstall "$PKG" >/dev/null 2>&1 || true
  if ! adb install -r -g "$apk"; then echo "::error::Cài thất bại: $label"; fail=1; echo "::endgroup::"; return; fi
  adb logcat -c
  adb shell monkey -p "$PKG" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  sleep 8
  adb exec-out screencap -p > "$OUT/$label-1-app.png"

  adb shell appops set "$PKG" SYSTEM_ALERT_WINDOW allow || true
  adb shell cmd notification allow_listener "$PKG/com.akira.island.service.NotifListener" || true
  adb shell settings put secure enabled_accessibility_services "$PKG/com.akira.island.service.IslandService"
  adb shell settings put secure accessibility_enabled 1
  adb shell input keyevent KEYCODE_HOME
  sleep 10
  adb exec-out screencap -p > "$OUT/$label-2-idle.png"

  adb shell cmd notification post -S bigtext -t "Minh" smoke1 "Tối nay đi ăn lẩu không? 7h nhé" || true
  sleep 2
  adb exec-out screencap -p > "$OUT/$label-3-message.png"
  sleep 6

  # Chạm vào Viên thuốc (giữa đỉnh màn hình) để mở rộng
  adb shell input tap $((W / 2)) 60
  sleep 2
  adb exec-out screencap -p > "$OUT/$label-4-expanded.png"
  adb shell input tap $((W / 2)) 60
  sleep 3

  local pid
  pid=$(adb shell pidof "$PKG" | tr -d '\r')
  echo "PID $label: ${pid:-không chạy}"
  [ -z "$pid" ] && { echo "::error::$label: tiến trình không còn chạy"; fail=1; }
  echo "RAM $label:"
  adb shell dumpsys meminfo "$PKG" | grep -E "TOTAL PSS|TOTAL RSS|TOTAL:|Java Heap|Native Heap|Graphics" | head -8
  echo "Khung hình $label:"
  adb shell dumpsys gfxinfo "$PKG" | grep -E "Total frames rendered|Janky frames|50th percentile|90th percentile" | head -4
  adb shell dumpsys accessibility | grep -c "com.akira.island.service.IslandService" | sed "s/^/Dịch vụ trợ năng đang bật ($label): /"

  adb logcat -d > "$OUT/$label-logcat.txt"
  if grep -A3 "FATAL EXCEPTION" "$OUT/$label-logcat.txt" | grep -q "$PKG"; then
    echo "::error::$label: ứng dụng bị crash"
    grep -A25 "FATAL EXCEPTION" "$OUT/$label-logcat.txt" | head -60
    fail=1
  fi
  if grep -q "ANR in $PKG" "$OUT/$label-logcat.txt"; then
    echo "::error::$label: ứng dụng bị treo (ANR)"
    fail=1
  fi
  echo "::endgroup::"
}

run out/Capsule_VN-debug.apk debug
run out/Capsule_VN.apk release

for f in "$OUT"/release-*.png; do thumb "$f"; done
ls -l "$OUT"
exit $fail
