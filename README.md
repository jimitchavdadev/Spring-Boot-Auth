# Spring Boot Authentication with OTP Verification

## 🛡️ Objective

This project implements a secure user authentication system using **Spring Boot**, featuring **email-based One-Time Password (OTP)** verification for both registration and login. The system enhances security through:

- **JWT (JSON Web Token)** authentication  
- **Password encryption** using BCrypt  
- **MySQL** for persistent storage  
- Professionally formatted **HTML emails** for OTP delivery  
- A **Bash script** for automated workflow testing

### 🔍 Key Goals

- Secure and user-friendly authentication system  
- OTP-based verification for registration and login  
- Responsive HTML OTP emails  
- Automated testing for core workflows  


## 🗂️ Project Structure

```

spring-boot-auth-otp/
├── src/
│   ├── main/
│   │   ├── java/com/example/auth/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── JwtFilter.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── dto/
│   │   │   │   ├── AuthRequest.java
│   │   │   │   └── OtpRequest.java
│   │   │   ├── entity/
│   │   │   │   └── UserInfo.java
│   │   │   ├── repository/
│   │   │   │   └── UserInfoRepository.java
│   │   │   ├── service/
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── OtpService.java
│   │   │   │   ├── UserInfoService.java
│   │   │   │   └── UserInfoDetails.java
│   │   │   └── AuthApplication.java
│   │   ├── resources/
│   │   │   ├── templates/
│   │   │   │   └── otp\_email\_template.html
│   │   │   └── application.properties
│   └── test/
├── testing.sh
├── pom.xml
└── README.md
````


## 🔁 How It Works

### 🔐 Registration Flow

1. `POST /auth/register`: User provides email, password, roles.  
2. Email uniqueness is verified. Password is hashed, OTP is generated and emailed.  
3. `POST /auth/verify-otp`: User verifies OTP. Account is marked verified.

### 🔓 Login Flow

1. `POST /auth/login`: User provides email and password.  
2. If valid, OTP is emailed.  
3. `POST /auth/verify-login-otp`: User enters OTP and receives JWT.  
4. `GET /auth/user/profile`: Access protected resource using JWT.


## ⚙️ Key Components

- **Database**: `user_info` table with OTP, expiry, verification flags  
- **OTP**: 6-digit codes, valid for 10 minutes, auto-cleared  
- **Email**: Responsive HTML using Spring Mail  
- **Security**: BCrypt, JWT, Spring Security (stateless + role-based)  
- **Testing**: `testing.sh` automates registration/login flows  


## 📨 Endpoints

| Endpoint               | Method | Description                         | Body             |
|------------------------|--------|-------------------------------------|------------------|
| `/auth/register`       | POST   | Register a new user                 | `UserInfo`       |
| `/auth/verify-otp`     | POST   | Verify OTP for registration         | `OtpRequest`     |
| `/auth/login`          | POST   | Login and receive new OTP           | `AuthRequest`    |
| `/auth/verify-login-otp`| POST   | Verify OTP and receive JWT          | `OtpRequest`     |
| `/auth/user/profile`   | GET    | Access protected profile (JWT)      | –                |


## 📦 Prerequisites

- Java 17+  
- Maven 3.6+  
- MySQL 8.0+  
- Gmail account with app password  
- Bash (for `testing.sh`)


## 🚀 Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/spring-boot-auth-otp.git
   cd spring-boot-auth-otp
2. **Configure MySQL**

   ```sql
   CREATE DATABASE auth_db;
   ```

   Update `application.properties`:

   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_mysql_password
   ```

3. **Configure Email (Gmail SMTP)**

   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   ```

4. **Build and Run**

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Make Testing Script Executable**

   ```bash
   chmod +x testing.sh
   ```


## 🧪 Usage & Testing

### Manual (via `curl` or Postman)

**Register**

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","roles":"ROLE_USER"}'
```

**Verify OTP**

```bash
curl -X POST http://localhost:8080/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","otpCode":"123456"}'
```

**Login**

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

**Verify OTP & Get JWT**

```bash
curl -X POST http://localhost:8080/auth/verify-login-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","otpCode":"654321"}'
```

**Access Protected Resource**

```bash
curl -H "Authorization: Bearer <your_jwt>" \
  http://localhost:8080/auth/user/profile
```

### Automated

```bash
./testing.sh
```

Options:

* `[1]` Register new user
* `[2]` Login existing user
* `[3]` Exit

---

## 🧱 Database Schema

| Column           | Type         | Description                    |
| ---------------- | ------------ | ------------------------------ |
| `id`             | BIGINT       | Primary key, auto-increment    |
| `email`          | VARCHAR(255) | Unique user email              |
| `password`       | VARCHAR(255) | BCrypt-hashed password         |
| `roles`          | VARCHAR(255) | Comma-separated roles          |
| `is_verified`    | BOOLEAN      | Email verification status      |
| `otp_code`       | VARCHAR(6)   | OTP code (nullable)            |
| `otp_expires_at` | TIMESTAMP    | OTP expiration time (nullable) |

Check schema:

```sql
DESCRIBE auth_db.user_info;
```


## 💌 Email Template

Located at `src/main/resources/templates/otp_email_template.html`.

* Blue header with “Your OTP Code”
* Centered OTP in bold, blue font
* Validity and security instructions
* Footer with app name and contact email

*Customize the template to match your branding.*


## 🔐 Security Features

* **BCrypt** password encryption
* **JWT** for authentication and authorization
* **Spring Security** with stateless sessions
* **OTP validity**: 10 minutes, deleted after use
* **CSRF disabled** (for REST)


## 🛠️ Troubleshooting

### Email

* Check spam folder
* Validate SMTP config
* Enable debug logs:

  ```properties
  logging.level.org.springframework.mail=DEBUG
  ```

### DB

* Ensure MySQL is running
* Confirm `auth_db` and `user_info` exist
* Check user verification status:

  ```sql
  SELECT email, is_verified FROM user_info;
  ```

### Scripts

* Make sure `testing.sh` is executable
* Use curl manually to isolate issues


## 🔮 Future Improvements

* Add `/auth/resend-otp` endpoint
* Rate limiting for OTP requests
* Frontend UI (React/Angular)
* Logo and branding in emails


## 🤝 Contributing

1. Fork this repo
2. Create a branch (`feature/your-feature`)
3. Commit and push
4. Open a pull request


## 📄 License

This project is licensed under the **MIT License**. See the `LICENSE` file for details.


## 📬 Contact

For questions or support: [jimitchavdadev@gmail.com](mailto:jimitchavdadev@gmail.com)
