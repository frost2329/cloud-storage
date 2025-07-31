# ☁️ Cloud Storage - Облачное хранилище файлов

## Описание сервиса
Современное Backend REST приложение для безопасного хранения и управления файлами в облаке. 

Реализация проекта из [Java Роадмапа Сергея Жукова](https://zhukovsd.github.io/java-backend-learning-course/projects/cloud-file-storage/)


### Сервис предоставляет возможности:

- 🔐 Аутентификацию с помощью сессий
- 📁 Загрузку/скачивание файлов
- 🗂 Управление структурой папок
- 🔍 Поиск по хранилищу
- 📊 Логирование операций

## 🚀 Технологический стек

| Категория       | Технологии                                        |
|-----------------|---------------------------------------------------|
| Бэкенд         | Java 17, Spring Boot 3.5.3                        |
| Безопасность   | Spring Security, Spring Sesssions, Redis Sessions |
| Базы данных    | PostgreSQL, Liquibase                             |
| Хранилище      | MinIO (S3-совместимое)                            |
| Документация   | OpenAPI 3, Swagger UI                             |
| Тестирование   | JUnit 5, Testcontainers                           |

## 🛠 Быстрый старт

### Требования
- Установленный Docker и Docker Compose
- Java 17 JDK
- Gradle 7+

### Установка

#### 1. Клонируйте репозиторий
```bash
git clone https://github.com/frost2329/cloud-storage.git
```

#### 2. Настройте окружение. Отредактируйте .env файл
```bash
# Database
DB_NAME=
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# Minio
MINIO_URL=
MINIO_ACCESS_KEY=
MINIO_SECRET_KEY=
```
#### 3. Запустите приложение
```bash
docker-compose up -d --build
```


## 📚 Документация API

После запуска сервиса документация будет доступна:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI спецификация: `http://localhost:8080/v3/api-docs`

