# 💰 JagaDuit - AI Smart Finance Tracker

**JagaDuit** is a personal finance management application built with Native Android using **Kotlin** and **Jetpack Compose**. It integrates artificial intelligence (**Google Gemini AI**) to simplify transaction recording through automatic receipt scanning.

## ✨ Key Features

* **🤖 AI Receipt Scanner:** Scan receipts using the camera or gallery. AI (Gemini 2.5 Flash) automatically extracts the **Total Amount**, **Date**, and **Category**.
* **📊 Smart Dashboard:** Monthly Cash Flow summary (Income vs Expense) with intuitive color indicators (Green/Red).
* **📈 Visual Statistics:** Interactive Donut Chart to view expense percentages by category.
* **🔐 Local Authentication:** Secure Login & Sign Up system using Local Database (Room) with a **5-minute Auto-Logout** session for security.
* **📷 Transaction Proof:** Attach receipt photos to every transaction and view them in full-screen mode.
* **📅 Month Picker:** Easily navigate transaction history across different months.
* **🎨 Modern UI:** Built with Material Design 3, featuring a Dark Theme and Neon Lime accents.

## 🛠️ Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material 3)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Database:** Room Database (SQLite)
* **AI Model:** Google Gemini SDK (`gemini-2.5-flash`)
* **Async:** Coroutines & Flow
* **Navigation:** Jetpack Navigation Compose
* **DI:** ViewModelFactory (Manual Injection)

## 🚀 How to Run

### Prerequisites
1.  Android Studio (Latest version, Jellyfish/Koala recommended).
2.  JDK 17 or newer.
3.  **Google Gemini API Key** (Get it at [Google AI Studio](https://aistudio.google.com/)).

### Installation Steps

1.  **Clone the Repository**
    ```bash
    git clone [https://github.com/cifer-renn/jagaduit-app.git](https://github.com/cifer-renn/jagaduit-app.git)
    ```

2.  **Configure API Key**
    This app requires an API Key for the Receipt Scanning feature to work.
    * Open the `local.properties` file in the project root (This file is usually not committed to Git).
    * Add the following line:
        ```properties
        GEMINI_API_KEY=AIzaSyDxxxxxxxxx_YOUR_API_KEY_HERE
        ```
    * *Note: Do not use quotes ("") for the API Key value.*

3.  **Sync Gradle**
    Open Android Studio, click **File > Sync Project with Gradle Files**.

4.  **Run**
    Run the application on an Emulator or Physical Device.

## 📂 Project Structure

```text
com.example.jagaduit
├── data/             # Room Entity, DAO, Database
├── ui/
│   ├── components/   # UI Widgets (MonthPicker, Charts)
│   ├── screens/      # Screens (Login, Menu, Input, Stats)
│   └── theme/        # Color, Type, Shape
├── utils/            # Helpers (GeminiHelper, SessionManager, Formatters)
└── viewmodel/        # Logic & State Management