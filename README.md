# Vitalis - Health Management System (Full Stack)

Vitalis is a comprehensive medical management application designed for both patients and doctors. It features a premium UI, real-time appointment scheduling, and patient history management.

## 🚀 Project Overview

This repository contains both the **Backend** and **Frontend** components of the Vitalis platform.

- **Frontend**: Flutter-based mobile application with a premium medical aesthetic.
- **Backend**: Spring Boot API providing secure authentication, data management, and integration with Supabase.

---

## 📱 Frontend (Flutter)

Modern and intuitive interface for a premium user experience.

### Key Features
- **Modern Medical Design**: Soft shadows, rounded inputs, and professional color palette.
- **Role Selection**: Dynamic toggle for Patient and Doctor accounts.
- **Profile Management**: Profile photo selection with automatic compression.
- **Office Location**: Interactive map selector using OpenStreetMap and Geocoding.
- **State Management**: Built with Flutter BLoC for robust and predictable states.

### Setup
1. Navigate to `clinica-frontend/`.
2. Run `flutter pub get`.
3. Run `flutter run`.

---

## ⚙️ Backend (Spring Boot)

Robust and scalable RESTful API.

### Key Features
- **Secure Authentication**: JWT-based security for all endpoints.
- **Database Architecture**: Comprehensive SQL schema managed via Supabase.
- **Automated Validations**: Real-time server-side data validation.
- **Multipart Support**: Optimized for handling profile photo uploads.

### Setup
1. Navigate to `clinica-backend/`.
2. Configure `.env.local` with your database credentials.
3. Run `./mvnw spring-boot:run`.

---

## 🛠️ Tech Stack

- **Frontend**: Flutter, BLoC, Dio, OpenStreetMap, Image Picker.
- **Backend**: Java, Spring Boot, Spring Security (JWT), Hibernate, Flyway.
- **Database**: PostgreSQL (Supabase).
- **Other**: Docker, Maven.

---

## 📄 License
This project is for portfolio purposes.
