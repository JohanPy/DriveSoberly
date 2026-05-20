#!/usr/bin/env bash
set -euo pipefail

LOG_FILE="connected-tests.log"
CRASH_PATTERN='INSTRUMENTATION_ABORTED: System has crashed|DeadSystemException'

wait_for_android() {
  adb wait-for-device
  until [[ "$(adb shell getprop sys.boot_completed | tr -d '\r')" == "1" ]]; do
    echo "Waiting for sys.boot_completed..."
    sleep 5
  done
  until [[ "$(adb shell getprop dev.bootcomplete | tr -d '\r')" == "1" ]]; do
    echo "Waiting for dev.bootcomplete..."
    sleep 5
  done
  until adb shell pm path android >/dev/null 2>&1; do
    echo "Waiting for Package Manager service..."
    sleep 5
  done
}

prepare_emulator() {
  adb shell input keyevent KEYCODE_WAKEUP || true
  adb shell locksettings set-disabled true || true
  adb shell wm dismiss-keyguard || true
  adb shell svc power stayon true || true
  adb shell settings put system screen_off_timeout 2147483647 || true
  adb shell settings put global window_animation_scale 0.0 || true
  adb shell settings put global transition_animation_scale 0.0 || true
  adb shell settings put global animator_duration_scale 0.0 || true
  adb shell settings put global verifier_verify_adb_installs 0 || true
  adb shell settings put global package_verifier_enable 0 || true
  adb shell settings put secure show_ime_with_hard_keyboard 0 || true
  adb shell settings put global drivesoberly_nosplash 1 || true
  adb shell settings put global stay_on_while_plugged_in 3 || true
  adb shell settings put secure accessibility_enabled 0 || true
}

run_connected_tests_once() {
  if ./gradlew connectedDebugAndroidTest --no-daemon > "$LOG_FILE" 2>&1; then
    cat "$LOG_FILE"
    return 0
  fi

  cat "$LOG_FILE"
  return 1
}

wait_for_android
prepare_emulator
sleep 5

if run_connected_tests_once; then
  exit 0
fi

if grep -Eq "$CRASH_PATTERN" "$LOG_FILE"; then
  echo "Detected emulator system crash during instrumentation. Rebooting emulator and retrying once..."
  adb reboot || true
  wait_for_android
  prepare_emulator
  sleep 5
  run_connected_tests_once
else
  exit 1
fi