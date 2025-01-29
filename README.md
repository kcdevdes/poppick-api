# ⚡ PopPick API

PopPick API is the backend service powering the PopPick platform, a tournament-based match game designed to enhance real-time engagement between streamers and viewers through interactive voting and ranking systems.

## 🚀 Features

- **User Authentication**: OAuth 2.0 login via Google, Apple, Kakao, and Naver.
- **Match Management**: Create, update, delete, and manage tournament-based matches.
- **Real-time Voting**: Synchronizes viewer engagement with live broadcasts.
- **Ranking System**: Calculates popularity and recent activity rankings.
- **Comment System**: Supports user-generated comments on matches.

## 🏗 Architecture

### System Overview
- **Backend**: Spring Boot (REST API)
- **Database**: PostgreSQL (Amazon RDS)
- **Authentication**: JWT-based OAuth 2.0
- **Storage**: AWS S3 for media handling

## 📄 License
This project is licensed under the MIT License.


