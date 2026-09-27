# GitManager 🚀

A modern, minimalist **monochrome** GitHub repository & account management Android application built with **Kotlin** and **Jetpack Compose**.

---

## ✨ Features

- 🎨 **Minimalist Monochrome Day (Light) Theme**: Clean aesthetic, subtle borders (1dp), sharp/less rounded corners (4-8dp), with dark mode switch support.
- 🔤 **Custom Typography**: Integrated **Mali** font family (`Light`, `Regular`, `Medium`, `SemiBold`) with small, crisp, high-density typographic scales.
- 🔐 **GitHub-Only Authentication**:
  - **Direct GitHub OAuth Flow** (Deep link handling via `gitmanager://oauth`).
  - **Personal Access Token (PAT)** authentication fallback with real-time scope validation and step-by-step token guidance.
  - Zero email / Gmail forms for direct, developer-first access.
- 📂 **Full Repository CRUD**:
  - **Create**: Modal dialog to create public or private repositories.
  - **Read**: Infinite scroll pagination, live search filter, and category pills (*All, Public, Private, Forks*).
  - **Update**: Edit repo name, description, and visibility.
  - **Delete**: Safe confirmation modal with repo name verification.
- 🏷️ **QR Code & Repository Card Generation**:
  - Instant offline QR Code generation for any repo URL via ZXing.
  - Export and share stylish monochrome printable/sharable preview cards containing repo details and embedded QR codes.
  - Native Android Sharesheet integration.
- 🔍 **User Search & Discovery**:
  - Debounced GitHub user search with real-time pagination.
  - View public profiles, follower/following counts, and public repository lists.

---

## 🛠️ Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, Navigation Compose
- **Networking**: Retrofit 2, OkHttp 3, OkHttp Logging Interceptor
- **Image Loading**: Coil Compose
- **QR Generation**: ZXing Core (3.5.3)
- **Local Persistence**: SharedPreferences / Android DataStore
- **Font Assets**: Custom `Mali` font in `res/font/`

---

## 🔑 How GitHub Login Works

### 1. Direct GitHub OAuth
1. The user clicks **"Sign in with GitHub OAuth"**.
2. The app launches `https://github.com/login/oauth/authorize` with requested scopes (`repo,user,delete_repo`).
3. After approval, GitHub redirects back to `gitmanager://oauth?code=...`.
4. `MainActivity` intercepts this via Android Intent Filters and authenticates the user.

### 2. Personal Access Token (PAT)
1. Go to **GitHub** -> **Settings** -> **Developer Settings** -> **Personal access tokens** -> **Tokens (classic)**.
2. Generate a token with `repo` and `user` permissions.
3. Paste the `ghp_...` token into the GitManager app and tap **"Authenticate Token"**.

---

## 🚀 How to Run the Project

1. Open **Android Studio** (Koala / Ladybug or newer recommended).
2. Select **Open** and choose this project folder (`gitmanager`).
3. Allow Gradle to sync and download dependencies.
4. Select an Android device or emulator (API 24+) and click **Run (Shift + F10)**.
