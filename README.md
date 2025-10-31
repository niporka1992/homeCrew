# 🏗️ HomeCrew — модульный проект

**HomeCrew** — многоуровневое приложение, включающее backend на **Spring Boot (Gradle)** и frontend на **React + Node.js**.  
Проект разделён на три модуля backend'а и один фронтовый клиент.

---

## ⚙️ Архитектура

```
homeCrew/
├── app/         # Главный модуль запуска (Spring Boot)
├── core/        # Ядро системы — бизнес-логика, сервисы, JPA и утилиты
├── bot/         # Telegram-бот, использующий core
├── frontend/    # React / Node.js клиент
└── build.gradle.kts  # Корневой Gradle-скрипт (сборка и управление)
```

---

## 🧩 Модули

### **1. `core/` — ядро**
Содержит:
- Доменные модели, DTO, репозитории (JPA)
- MapStruct-мапперы
- Сервисы, утилиты, общую бизнес-логику

📦 Сборка:
```bash
./gradlew :core:build
```

---

### **2. `bot/` — Telegram-бот**
Использует `core` для взаимодействия с базой и логикой.

📦 Сборка:
```bash
./gradlew :bot:build
```

---

### **3. `app/` — основной backend**
Главный исполняемый модуль (Spring Boot Application).  
Собирается в **fat-jar** и запускается как сервер.

🚀 **Полная сборка и тестирование** (все модули):
```bash
./gradlew fullBuild
```

После успешной сборки:
```bash
java -jar app/build/libs/app-1.0.0.jar --spring.profiles.active=dev
```

---

### **4. `frontend/` — React-клиент**
Frontend на Node.js (React, TypeScript или JavaScript).

📦 Установка зависимостей:
```bash
cd frontend
npm install
```

💻 Запуск в dev-режиме:
```bash
npm run dev
```


## 🧭 Запуск полного проекта

1. **Запусти базу данных PostgreSQL**
   ```bash
   docker run -d      --name homecrew-postgres      -e POSTGRES_USER=root      -e POSTGRES_PASSWORD=root      -e POSTGRES_DB=homecrew_dev      -p 5432:5432      postgres:18
   ```

2. **Укажи в `application.yml` или `.env` значения:**
   ```yaml
  app:
  telegram:
    token:                # Токен Telegram-бота (из @BotFather)
    group-chat-id:        # ID группового чата для уведомлений (обычно отрицательное число, например -1001234567890)
    admin:
      chatId:             # Telegram ID администратора (для личных уведомлений и логов)
  admin:
    default-password:     # Пароль по умолчанию для системного администратора приложения
  jwt:
    secret:               # Секретный ключ для подписи и проверки JWT-токенов (используется в механизме аутентификации)
   ```

3. **Собери backend:**
   ```bash
   ./gradlew fullBuild
   ```

4. **Запусти сервер:**
   ```bash
   java -jar app/build/libs/app-1.0.0.jar --spring.profiles.active=dev
   ```

5. **Запусти frontend:**
   ```bash
   cd frontend
   npm run dev
   ```

6. **Готово.**  
   **Backend:** [http://localhost:25186](http://localhost:25186)  
   **Frontend:** [http://localhost:5173](http://localhost:5173)
