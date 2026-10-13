# Salim - Native Android SMS & MMS App

Salim is a 100% native Kotlin and Jetpack Compose SMS and MMS messaging application for Android, built with complete offline privacy, zero network permissions, and real Telephony provider synchronization.

## Features
- **Real SMS and MMS**: Send and receive real text, pictures, audio voice notes, and attachments via device SIMs.
- **Default SMS App**: Fully qualified default SMS role integration (`RoleManager.ROLE_SMS`) with fallback read-only mode.
- **Dual SIM Support**: Automatically detects SIM subscriptions, allowing selection per conversation or per message.
- **Zero Network / 100% On-Device**: Salim requests NO `INTERNET` permission. Search (FTS), spam heuristics, OTP extraction, and database management happen exclusively on-device.
- **Pixel-Accurate Minimalist Design**: Soft rounded pills, 44dp circular icon buttons, Urbanist geometric typography, and `#F1F1F1` / `#EBEBEB` surfaces.
- **App & Conversation Lock**: Secure individual threads or the entire app with biometric/device credential authentication. *Note: App locking hides content inside Salim; the system Android SMS provider retains system messages according to device OS storage.*
- **Local Backup & Restore**: Safe encrypted (AES-256-GCM) and plain export/import using the Storage Access Framework (SAF).

## Building & Signing
To build the project:
```bash
./gradlew assembleRelease
./gradlew bundleRelease
```
Signing is configured for both debug and release variants using standard key credentials.

## Setting as Default SMS App
1. Open Salim. On initial launch, grant SMS, Phone, and Contacts permissions when prompted.
2. Tap "Set as default" in the header banner or navigate to **Settings > Default app and permissions**.
3. Accept the system prompt designating Salim as the default messaging application.

## Google Play SMS Permission Declaration
Salim utilizes `READ_SMS`, `SEND_SMS`, `RECEIVE_SMS`, and `RECEIVE_MMS` under the **Default SMS Handler** core app functionality exception. It fully implements all broadcast receivers and services required by Google Play policy for default SMS applications.
