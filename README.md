<div align="center">
  <img src="frontend/public/logo2.png" alt="Mandal Ledger Logo" width="120" />
  <h1>Mandal Ledger (Real Karyakarte)</h1>
  <p>A modern, transparent, and robust ledger application built specifically for Ganpati Mandals, Societies, and Community Organizations.</p>

  ![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)
  ![Vite](https://img.shields.io/badge/Vite-5.0+-646CFF?logo=vite&logoColor=white)
  ![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4+-38B2AC?logo=tailwind-css&logoColor=white)
  ![Java](https://img.shields.io/badge/Java-17+-ED8B00?logo=openjdk&logoColor=white)
  ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?logo=postgresql&logoColor=white)
</div>

---

## 📖 Overview

Managing community funds, vargani (contributions), and daily expenses during festivals or for housing societies is traditionally a chaotic process involving paper receipts, scattered WhatsApp messages, and mismatched Excel sheets. 

**Mandal Ledger** digitizes and streamlines this entire process. It empowers organizations to maintain a flawless, live digital ledger that is accessible and verifiable by every authorized member directly from their smartphones. By bringing complete transparency to financial operations, it builds trust and accountability within the community.

## ✨ Comprehensive Features

### 💰 Vargani & Contribution Tracking
- **Live Ledger**: Instantly view total collected funds, current cash in hand, and outstanding balances.
- **Advanced Room & Floor Mapping**: Track contributions by building, floor, and room number. Supports complex structures like multiple families renting a single room, plus separate "Owner" tracking for shared spaces.
- **Automated Sync**: When a contribution is logged, the respective room's status is automatically updated in the tracker.

### 📄 Digital Receipts & Sharing
- **Instant PDF Generation**: Generate professional, dynamic PDF receipts upon every transaction using OpenPDF.
- **WhatsApp Integration**: Share generated receipts instantly with contributors via WhatsApp with pre-formatted messages and clickable links.
- **Public Receipt Access**: Secure, tokenless public endpoints allow external users to view their receipts effortlessly.

### 📊 Expense Management & Analytics
- **Audit Trails**: Log every rupee spent with mandatory notes and categorizations.
- **Real-Time Deductions**: Expenses are immediately reflected against the total collected vargani to display accurate "Cash in Hand".
- **Downloadable Reports**: Export financial summaries and room statuses for offline analysis and auditing.

### 📢 Community Notice Board
- **Real-Time Communication**: Keep all Karyakartas (workers/volunteers) updated with important announcements.
- **Interactive Discussions**: Nested replies and reaction support allow for seamless internal coordination.

### 🔐 Role-Based Access Control
- **Admins**: Full access to financial overriding, bulk room creation, and system configurations.
- **Karyakartas (Volunteers)**: Restricted access tailored for logging contributions and viewing notices without exposing sensitive administrative controls.

## 🛠️ Architecture & Tech Stack

The application is structured as a decoupled SPA (Single Page Application) with a robust RESTful API backend.

### Frontend
- **React.js**: Modular UI component architecture.
- **Vite**: Ultra-fast hot-module replacement and optimized production builds.
- **Tailwind CSS**: Custom, utility-first styling for a beautiful, premium, and fully responsive user interface.
- **React Router**: Seamless client-side navigation.
- **Lucide React**: Clean, modern iconography.

### Backend
- **Java (Jakarta EE)**: High-performance, scalable servlet-based REST API without heavy framework overhead.
- **OpenPDF**: Dynamic and performant PDF generation for digital receipts.
- **HikariCP**: Lightning-fast JDBC connection pooling for optimal database performance.
- **Maven**: Standardized dependency management and build automation.

### Database
- **PostgreSQL**: Robust, relational database ensuring ACID compliance and strict data integrity for all financial records.

## 🚀 Getting Started

Follow these steps to run the project locally.

### Prerequisites
- Node.js (v18+) & npm
- Java (JDK 17+)
- Maven (v3.8+)
- PostgreSQL (v15+)

### 1. Database Setup
1. Open your PostgreSQL terminal/UI and create the database and user:
   ```sql
   CREATE DATABASE ganpati_mandal;
   CREATE USER mandal_app WITH PASSWORD 'changeme';
   GRANT ALL PRIVILEGES ON DATABASE ganpati_mandal TO mandal_app;
   ```
2. Navigate to `backend/src/main/resources/db/` and execute `setup_postgres.sql` and the migration scripts to initialize the schema.

### 2. Backend Initialization
The backend relies on environment variables for sensitive configurations, adhering to the 12-Factor App methodology.

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build and run the server using Cargo:
   ```bash
   mvn clean package cargo:run
   ```
   *The backend will be available at `http://localhost:8080/api/`.*

### 3. Frontend Initialization
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies and start the development server:
   ```bash
   npm install
   npm run dev
   ```
   *The application will be accessible at `http://localhost:5173/`.*

## 🔒 Environment Variables Reference

For production deployments, configure the following environment variables on your server. If left unset, the application will safely fall back to local development defaults.

| Variable | Description | Default (Local Dev) |
|----------|-------------|---------------------|
| `DB_URL` | PostgreSQL connection string | `jdbc:postgresql://localhost:5432/ganpati_mandal` |
| `DB_USERNAME` | Database username | `mandal_app` |
| `DB_PASSWORD` | Database password | `changeme` |
| `JWT_SECRET` | Base64 encoded 256-bit key for secure tokens | *Provided in application.properties* |
| `STORAGE_BASE_DIR` | Absolute path for saving uploaded files and generated PDFs | `user.home + /mandal_data` |

## 🤝 Contributing

We welcome contributions to make Mandal Ledger even better!
1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## 👨‍💻 Developed By

**Pranav Raut**  
📧 [praut1086@gmail.com](mailto:praut1086@gmail.com)  
*Designed with ❤️ for Community Transparency*
