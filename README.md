# 🌟 WorkNest – Task Management System  

---

## 📖 Introduction  

**WorkNest SpringBoot** is a **role-based task management system** built with **Spring Boot, Thymeleaf, Hibernate, and MySQL**.  

It provides:  
✅ **Admin panel** to manage users and tasks  
✅ **User panel** to manage assigned tasks  
✅ **Role-based authentication** (Admin/User)  
✅ **CRUD operations** for tasks, users, and comments  
✅ **Task assignment system** with multi-user support  

---

## 🎯 Objectives  

- Streamline task management for teams  
- Provide simple user & admin dashboards  
- Enable collaboration through task comments  
- Demonstrate a production-ready **Spring Boot + Thymeleaf** app  

---

## 🛠️ Tech Stack  

- **Backend Framework**: Spring Boot  
- **Frontend**: Thymeleaf templates  
- **Database**: MySQL 8+  
- **ORM**: Hibernate   
- **Build Tool**: Maven  
- **Server Port**: `8080`  

---

## 📂 Project Structure  

Webapp_patched/
│── pom.xml # Maven dependencies
│── src/main/java/com/worknest/
│ ├── controller/ # Controllers (Admin, User, Task, Home)
│ ├── model/ # Entities: User, Admin, Task, Comment, Assignment
│ ├── repo/ # Repositories (JPA)
│ ├── service/ # Services for business logic
│ └── WorkNestAppApplication.java # Main entry point
│
│── src/main/resources/
│ ├── application.properties # Database & App configs
│ ├── static/ # CSS, JS, images
│ └── templates/ # Thymeleaf HTML templates
│
└── .settings, .classpath, .project # Eclipse project configs


---

## Access the Application

**Homepage** → http://localhost:8080/

**Admin Login** → http://localhost:8080/admin-login

**User Login** → http://localhost:8080/user-login

---

## Features

**👨‍💼 Admin Features**

- Register and login with admin credentials

- Manage users (create, edit, delete)

- Create, assign, and edit tasks

- Assign tasks to multiple users

- View and manage task comments

- Access an admin dashboard with quick insights

**👤 User Features**

- Register and login as user

- View assigned tasks

- Edit profile and update details

- Comment on tasks

- Dashboard for task overview

---

## 📌 API Endpoints

| Endpoint           | Method   | Role       | Description          |
| ------------------ | -------- | ---------- | -------------------- |
| `/admin-login`     | GET/POST | Admin      | Admin authentication |
| `/admin-dashboard` | GET      | Admin      | Dashboard view       |
| `/user-login`      | GET/POST | User       | User authentication  |
| `/user-dashboard`  | GET      | User       | User dashboard       |
| `/task-form`       | GET/POST | Admin      | Create task          |
| `/task-edit/{id}`  | POST     | Admin      | Edit task            |
| `/task-view/{id}`  | GET      | User/Admin | View task            |

---









