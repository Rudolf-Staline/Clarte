# Clarté V2.3 Release Checklist

## Build
- [x] Clean build passes (`./gradlew clean`)
- [x] Debug APK generated (`./gradlew assembleDebug`)
- [x] App launches on real device

## Local Usage
- [x] Create entry
- [x] Save entry
- [x] Delete entry
- [x] Favorite entry
- [x] Pin entry
- [x] Search entry
- [x] Trends screen opens

## AI
- [x] Mock AI works (offline fallback)
- [x] Gemini configuration can be overridden safely

## Firebase & Sync
- [x] App works without an account (Local-first)
- [x] Login & Account creation work
- [x] Logout works securely without deleting local data
- [x] Manual cloud sync works
- [x] Manual restore works

## Encryption (Passphrase)
- [x] Enable encrypted backup with strict passphrase (min 8 chars)
- [x] Checkbox confirmation required to activate
- [x] Tester ma phrase de récupération flow works
- [x] Sync encrypted entry securely
- [x] Firestore metadata contains PBKDF2 hash & salt, no plaintext journal
- [x] Restore success with correct phrase
- [x] Reject wrong phrase directly without corrupting local data.

## Privacy & Polish
- [x] Onboarding appears ONLY on first launch
- [x] Privacy center accessible from settings
- [x] Microcopy updated across all empty states
- [x] Error messages mapped to simple French
- [x] No journal content in Logcat
- [x] No passphrase exposed in Logcat or UI
- [x] Small-screen layouts acceptable with scrollviews

## Result
**Status:** READY for real-device testing.
