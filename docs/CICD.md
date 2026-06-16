# Cosmica CI/CD

Continuous integration and delivery for the Cosmica Android app, built with
**GitHub Actions** (pipeline orchestration) and **Fastlane** (build & deploy).

## Overview

The pipeline runs on every push and pull request and gates every deployment
behind lint + unit + UI tests. If any test stage fails, the pipeline stops and
nothing is deployed.

```
lint ─▶ unit_tests ─▶ ui_tests ─┬─▶ deploy_test        (develop push → Firebase "testers")
                                 └─▶ deploy_production  (main push  → Firebase "production")
```

### Environments & branches

| Branch    | Flavor | App ID                 | Build type | Distributed to        |
|-----------|--------|------------------------|------------|-----------------------|
| `develop` | `dev`  | `com.cosmica.app.dev`  | Debug      | Firebase — `testers`  |
| `main`    | `prod` | `com.cosmica.app`      | Release    | Firebase — `production` |

Pull requests run lint + unit + UI tests only (no deployment).

## Fastlane lanes

Defined in [`fastlane/Fastfile`](../fastlane/Fastfile):

| Lane                | Purpose                                                |
|---------------------|--------------------------------------------------------|
| `lint`              | Android lint (dev flavor)                              |
| `test`              | Unit tests (dev flavor)                                |
| `ui_test`          | Instrumented UI tests on a connected emulator          |
| `build_test`        | Assemble dev debug APK                                  |
| `build_production`  | Assemble prod release APK (signed via env vars)        |
| `deploy_test`       | Build + upload TEST version to Firebase                 |
| `deploy_production` | Build + upload PRODUCTION version to Firebase          |

Run locally (requires Ruby 3.x + `bundle install`):

```bash
bundle install
bundle exec fastlane test
bundle exec fastlane deploy_test   # needs the env vars below
```

## Required GitHub secrets

Add these under **Settings → Secrets and variables → Actions**:

| Secret                 | Used by              | How to get it                                                        |
|------------------------|----------------------|---------------------------------------------------------------------|
| `NASA_API_KEY`         | all jobs             | https://api.nasa.gov                                                |
| `FIREBASE_CREDENTIALS` | both deploy jobs     | Service account JSON (full file contents) — see below               |
| `FIREBASE_TEST_APP_ID` | `deploy_test`        | Firebase console → dev app → App ID (`1:...:android:...`)           |
| `FIREBASE_PROD_APP_ID` | `deploy_production`  | Firebase console → prod app → App ID                                |
| `KEYSTORE_BASE64`      | `deploy_production`  | `base64 -i cosmica-release.jks` (the whole output)                  |
| `KEY_ALIAS`            | `deploy_production`  | Alias used when the keystore was created                            |
| `KEY_PASSWORD`         | `deploy_production`  | Key password                                                        |
| `STORE_PASSWORD`       | `deploy_production`  | Keystore password                                                   |

## One-time setup checklist

### 1. Firebase project (Test + Production apps)
- [ ] Create a Firebase project (or reuse one).
- [ ] Register **two Android apps** in it:
  - Test: package name `com.cosmica.app.dev`
  - Production: package name `com.cosmica.app`
- [ ] Enable **App Distribution** for the project.
- [ ] Create tester groups named exactly `testers` and `production`, add testers.
- [ ] Copy each app's **App ID** into `FIREBASE_TEST_APP_ID` / `FIREBASE_PROD_APP_ID`.

### 2. Firebase service account
- [ ] Google Cloud console → IAM → Service Accounts → create one.
- [ ] Grant it the **Firebase App Distribution Admin** role.
- [ ] Create a JSON key, download it.
- [ ] Paste the **entire JSON contents** into the `FIREBASE_CREDENTIALS` secret.

### 3. Release keystore (production signing)
- [ ] Generate once (keep it safe, never commit):
  ```bash
  keytool -genkey -v -keystore cosmica-release.jks \
    -keyalg RSA -keysize 2048 -validity 10000 -alias cosmica
  ```
- [ ] Base64-encode it and store as `KEYSTORE_BASE64`:
  ```bash
  base64 -i cosmica-release.jks | pbcopy
  ```
- [ ] Set `KEY_ALIAS`, `KEY_PASSWORD`, `STORE_PASSWORD` secrets to match.

### 4. Verify
- [ ] Push to `develop` → confirm the test build lands in Firebase `testers`.
- [ ] Merge/push to `main` → confirm the signed prod build lands in `production`.
- [ ] Break a test on a branch → confirm the pipeline fails and nothing deploys.
