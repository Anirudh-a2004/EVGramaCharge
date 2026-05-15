EVGramaCharge 

    A smart EV Charging Station Management Android application developed using Kotlin, Jetpack Compose, Firebase, and Google Maps API.
    EVGramaCharge connects EV users and charging station providers through a unified mobile platform for discovering, booking, and managing EV charging stations efficiently.

📌 Project Overview

    EVGramaCharge is designed to simplify the EV charging experience for both electric vehicle users and charging station providers.

    The application enables users to:

        Find nearby EV charging stations
        View station details
        Book charging slots
        Track active/completed bookings
        Navigate using Google Maps
        Receive notifications and updates

The platform also provides vendors with:

        Station management tools
        Booking monitoring
        Analytics dashboard
        Booking trends visualization
        Real-time booking synchronization

The project was developed as part of the MindMatrix Industry Readiness Programme.


🚀 Features

    👤 User Features

        User Registration & Login
        Role-Based Access
        Nearby Charging Station Discovery
        Google Maps Integration
        Real-Time Booking System
        Active Bookings
        Charging History
        Favorites System
        Notification Center
        Battery Tips
        Profile Management
        Dark/Light Mode Support

    🏢 Vendor Features

        Vendor Dashboard
        Add/Edit Charging Stations
        Station Performance Overview
        Booking Analytics
        Booking Trends Graph
        My Stations Management
        Vendor Notifications
        Station Availability Management
        Plug Count & Facilities Management
        Vendor Profile Management

🛠️Tech Stack

        Technology	Usage
        Kotlin	Android Development
        Jetpack Compose	Modern UI Development
        Firebase Authentication	User Authentication
        Firebase Firestore	Real-Time Database
        Google Maps API	Map & Navigation
        Material 3	UI Design System
        MVVM Architecture	App Architecture

System Architecture

    The application follows the MVVM (Model-View-ViewModel) architecture pattern.

    Main Components:
        UI Layer (Jetpack Compose)
        ViewModel Layer
        Firebase Firestore Database
        Firebase Authentication
        Google Maps Services


📂 Folder Structure

EVGramaCharge/
│
├── app/
│   ├── src/main/java/com/example/evgramacharge/
│   │   ├── screens/
│   │   ├── components/
│   │   ├── ui/theme/
│   │   ├── utils/
│   │   ├── MainActivity.kt
│   │   └── FirestoreStation.kt
│   │
│   ├── res/
│   └── AndroidManifest.xml
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md

⚙️ Installation & Setup

        Prerequisites
        Android Studio Hedgehog or later
        JDK 17+
        Firebase Project
        Google Maps API Key
        Clone Repository
        git clone https://github.com/Anirudh-a2004/EVGramaCharge
        Open in Android Studio
        Open Android Studio

    Select:

        Open Existing Project
        Choose the project folder
        Firebase Setup
        Create a Firebase project
        Enable:
        Firebase Authentication
        Cloud Firestore

    Download:

        google-services.json

        Place it inside:

        app/
        Google Maps Setup
        Generate a Google Maps API Key

        Add the API key inside:

        AndroidManifest.xml

        Example:

        <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="YOUR_API_KEY"/>


▶️ Running the Project
    Sync Gradle

        Allow Android Studio to sync all dependencies.

        Run the App

        Click:

        Run ▶

        Or use:

        .\gradlew.bat assembleDebug

📱 Screenshots
Home Screen
![Home Screen.jpeg](../Users/ANIRUDH%20A/OneDrive/Desktop/New%20folder/Home%20Screen.jpeg)

Map Screen
![MapScreen.jpeg](../Users/ANIRUDH%20A/OneDrive/Desktop/New%20folder/MapScreen.jpeg)

Booking Screen
![Booking Screen.jpeg](../Users/ANIRUDH%20A/OneDrive/Desktop/New%20folder/Booking%20Screen.jpeg)

Vendor Dashboard
![Vendor Screen.jpeg](../Users/ANIRUDH%20A/OneDrive/Desktop/New%20folder/Vendor%20Screen.jpeg)


🔄 Booking Synchronization

The application includes:

        Real-time booking synchronization
        Dynamic plug availability updates
        Active/completed booking tracking
        Role-based booking visibility
        Vendor-specific booking analytics
        Dark Mode Support

The app supports:

        Global dark/light mode
        Persistent theme settings
        Material 3 dynamic UI adaptation

🔒 Security & Validation

Implemented validations include:

        Future booking time validation
        Role-based access control
        Vendor-specific station management
        User-specific booking visibility
        Firebase Authentication security

📈 Future Enhancements

        QR-Based Charging Sessions
        Live Charging Progress Tracking
        Push Notifications
        Advanced Analytics Dashboard
        EV Route Optimization
        Multi-Language Support
        AI-Based Station Recommendations

📚 Learning Outcomes

    Through this project, the following concepts were explored and implemented:

        Android App Development using Kotlin
        Jetpack Compose UI Design
        Firebase Authentication & Firestore
        Google Maps API Integration
        MVVM Architecture
        Real-Time Data Synchronization
        Role-Based Navigation
        State Management
        Material 3 Design System
        Modern Mobile UI/UX Design

👨‍💻 Developer Details

        Detail	            Information
        Developer	        Anirudh A
        USN	                1SP22IC004
        Role	            Android App Development using Gen AI Intern
        Programme	        MindMatrix Industry Readiness Programme

🤝 Contribution Guidelines

        Contributions are welcome.

        To contribute:

                Fork the repository
                Create a feature branch
                Commit changes
                Push to your branch
                Create a Pull Request

📄 License

        This project is developed for educational and internship purposes.

⭐ Final Note

        EVGramaCharge demonstrates the implementation of a modern EV Charging Station Management platform using Android development technologies, Firebase integration, real-time synchronization, and modern UI/UX principles.