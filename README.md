<div align="center">
  <img src="frontend/public/logo.png" alt="Mandal Ledger Logo" width="120" />
  <h1>Mandal Ledger (Real Karyakarte)</h1>
  <p>A modern, transparent, and responsive ledger application built specifically for Ganpati Mandals and Societies.</p>

  ![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)
  ![Vite](https://img.shields.io/badge/Vite-5.0+-646CFF?logo=vite&logoColor=white)
  ![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4+-38B2AC?logo=tailwind-css&logoColor=white)
  ![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white)
</div>

Say goodbye to paper receipts and mismatched Excel sheets. Bring complete transparency to your Mandal's accounts, straight from your phone!

## ✨ Features

- **Live Dashboard**: Get real-time insights into total collected funds, spent amounts, and current balance in hand.
- **Digital Receipts**: Instantly generate professional PDF receipts and share them directly via WhatsApp.
- **Room & Floor Tracking**: Automatically track which rooms/floors have paid and who is pending.
- **Expense Management**: Log every rupee spent for complete transparency.
- **Community Notice Board**: Keep all karyakartas updated with announcements and discussions.
- **Role-Based Access**: Secure login system with distinct privileges for Admins and Karyakartas.
- **Responsive Design**: Beautiful, modern UI that works flawlessly on mobile devices and desktops.

### Backend
- **Java (Jakarta EE)**: Robust servlet-based REST API
- **OpenPDF**: Dynamic PDF generation for digital receipts
- **HikariCP**: High-performance JDBC connection pooling
- **Maven**: Dependency management and build automation

### Database
- **PostgreSQL**: Reliable, relational database for robust data integrity

## 🚀 Getting Started

### Prerequisites
- Node.js & npm
- Java 17+
- Maven
- PostgreSQL

### Setup Instructions

1. **Database Setup**
   - Run the provided `setup_postgres.sql` and migration scripts in `backend/src/main/resources/db/` to initialize your database.
   - Configure your environment variables for `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

2. **Backend Development**
   ```bash
   cd backend
   mvn clean package cargo:run
   ```
   *The backend server will start on port 8080.*

3. **Frontend Development**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   *The frontend app will be accessible at http://localhost:5173.*

## 🔒 Environment & Configuration

This project is built to follow the 12-Factor App methodology. All sensitive credentials and storage directories are configurable via environment variables, ensuring zero credential leakage in the source code.

## 👨‍💻 Developed By

**Pranav Raut**  
📧 [praut1086@gmail.com](mailto:praut1086@gmail.com)
