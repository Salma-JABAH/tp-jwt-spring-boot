# 🔐 TP JWT – API REST sécurisée avec Spring Boot

Authentification **stateless** par **JSON Web Token (JWT)** avec Spring Boot, Spring Security, Spring Data JPA et une base H2 en mémoire.

![Java](https://img.shields.io/badge/Java-17%2B-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Security-6DB33F)
![JWT](https://img.shields.io/badge/jjwt-0.13.0-black)
![H2](https://img.shields.io/badge/DB-H2%20in--memory-blue)

---

## 📋 Sommaire

1. [Fonctionnalités](#-fonctionnalités)
2. [Stack technique](#-stack-technique)
3. [Architecture du projet](#-architecture-du-projet)
4. [Flux d'authentification](#-flux-dauthentification)
5. [Configuration](#-configuration)
6. [Lancer le projet](#-lancer-le-projet)
7. [Endpoints](#-endpoints)
8. [Tests de l'API (captures)](#-tests-de-lapi-captures)
9. [Aperçu du code](#-aperçu-du-code)
10. [Points d'amélioration](#-points-damélioration)

---

## ✨ Fonctionnalités

- Inscription d'un utilisateur (`/auth/register`) avec mot de passe haché en **BCrypt**
- Connexion (`/auth/login`) qui renvoie un **JWT signé HS256**
- Filtre `JwtFilter` (`OncePerRequestFilter`) qui valide le jeton à chaque requête
- Routes `/api/**` protégées, routes `/auth/**` publiques
- Politique de session **STATELESS**, CSRF désactivé (API sans session)
- Console H2 pour consulter la base pendant les tests

## 🧰 Stack technique

| Élément | Détail |
|---|---|
| Langage / Build | Java, Maven (wrapper `mvnw` inclus) |
| Framework | Spring Boot, Spring Web, Spring Security, Spring Data JPA |
| JWT | `io.jsonwebtoken` **jjwt 0.13.0** (api, impl, jackson) |
| Base de données | H2 en mémoire (`jdbc:h2:mem:jwt`) |
| Outils | Lombok, IntelliJ IDEA, Postman |

## 🗂 Architecture du projet

Package de base : `com.example.tpjwtspringvf`

```
src/main/java/com/example/tpjwtspringvf
├── config/          SecurityConfig
├── controllers/     AuthController, UserController
├── dto/             RegisterRequest, LoginRequest, AuthResponse (records)
├── entity/          AppUser
├── repository/      UserRepository
├── services/        CustomUserDetailsService, JwtService, JwtFilter
└── TpJwtSpringVfApplication
```

<p align="center">
  <img src="assets/structure/01-project-structure.png" alt="Structure du projet" width="260">
</p>

## 🔄 Flux d'authentification

```mermaid
sequenceDiagram
    participant C as Client (Postman)
    participant A as AuthController
    participant S as Spring Security
    participant J as JwtService
    participant F as JwtFilter
    participant U as UserController

    C->>A: POST /auth/register (email, password)
    A-->>C: 200 "Utilisateur cree"
    C->>A: POST /auth/login (email, password)
    A->>S: authenticate(email, password)
    S-->>A: OK
    A->>J: generateToken(userDetails)
    J-->>A: JWT (HS256, 1h)
    A-->>C: { "token": "eyJ..." }
    C->>F: GET /api/profile + Authorization: Bearer JWT
    F->>J: extractUsername / isTokenValid
    F->>U: SecurityContext renseigné
    U-->>C: 200 "Utilisateur connecte : ..."
```

## ⚙️ Configuration

`src/main/resources/application.properties` :

```properties
spring.application.name=tp-jwt-spring-vf

spring.datasource.url=jdbc:h2:mem:jwt
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# Clé HS256 encodée en Base64 (voir ci-dessous pour en générer une)
jwt.secret=<VOTRE_CLE_BASE64>
# Durée de vie du jeton en millisecondes (3 600 000 ms = 1 h)
jwt.expiration=3600000
```

> ⚠️ **Ne publiez jamais votre vrai secret.** Générez-en un nouveau :
> ```bash
> openssl rand -base64 32
> ```
> Pour un vrai déploiement, injectez-le par variable d'environnement (`JWT_SECRET`) plutôt que dans le fichier.

## 🚀 Lancer le projet

```bash
git clone https://github.com/<votre-utilisateur>/tp-jwt-spring-boot.git
cd tp-jwt-spring-boot

# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

L'application démarre sur **http://localhost:8080**.
Console H2 : **http://localhost:8080/h2-console** → JDBC URL : `jdbc:h2:mem:jwt`, user : `sa`, mot de passe vide.

## 📡 Endpoints

| Méthode | URL | Accès | Description |
|---|---|---|---|
| `POST` | `/auth/register` | Public | Crée un utilisateur (rôle `USER`) |
| `POST` | `/auth/login` | Public | Authentifie et renvoie un JWT |
| `GET` | `/api/hello` | 🔒 JWT | Message de bienvenue |
| `GET` | `/api/profile` | 🔒 JWT | Email de l'utilisateur connecté |

### Exemples `curl`

```bash
# 1. Inscription
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"123456"}'

# 2. Connexion → récupérer le token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"123456"}'

# 3. Appel d'une route protégée
curl http://localhost:8080/api/profile \
  -H "Authorization: Bearer <TOKEN>"
```

## 🧪 Tests de l'API (captures)

### 1. Console H2

| Page de connexion | Base `jdbc:h2:mem:jwt` (table `USERS`) |
|---|---|
| ![H2 login](assets/tests/01-h2-login.png) | ![H2 console](assets/tests/02-h2-console.png) |

> La console propose par défaut `jdbc:h2:~/test` : il faut saisir `jdbc:h2:mem:jwt` pour voir les tables du projet.

### 2. Inscription – `POST /auth/register` → `200 OK`

![Register](assets/tests/03-register-200.png)

### 3. Vérification en base – mot de passe haché en BCrypt

![Table USERS](assets/tests/04-h2-users-table.png)

Le mot de passe est stocké sous la forme `$2a$10$...` (BCrypt, coût 10), jamais en clair.

### 4. Connexion – `POST /auth/login` → `200 OK` + JWT

![Login](assets/tests/05-login-token.png)

Le jeton décodé contient : en-tête `{"alg":"HS256"}`, charge utile `{"sub":"<email>","iat":...,"exp":...}` avec `exp - iat = 3600 s`.

### 5. Routes protégées avec jeton – `200 OK`

| `GET /api/hello` | `GET /api/profile` |
|---|---|
| ![hello 200](assets/tests/06-hello-200.png) | ![profile 200](assets/tests/07-profile-200.png) |

### 6. Routes protégées sans jeton – `403 Forbidden`

| `GET /api/profile` (Postman) | `GET /api/hello` (Postman) |
|---|---|
| ![profile 403](assets/tests/08-profile-403.png) | ![hello 403](assets/tests/10-hello-403.png) |

Même résultat depuis le navigateur (aucun en-tête `Authorization`) :

![Whitelabel 403](assets/tests/09-profile-403-browser.png)

### Récapitulatif

| # | Requête | Jeton | Statut | Résultat |
|---|---|---|---|---|
| 1 | `POST /auth/register` | – | 200 | Utilisateur créé |
| 2 | `SELECT * FROM USERS` | – | – | Ligne présente, mot de passe BCrypt, rôle `USER` |
| 3 | `POST /auth/login` | – | 200 | JWT renvoyé |
| 4 | `GET /api/hello` | Valide | 200 | Message de bienvenue |
| 5 | `GET /api/profile` | Valide | 200 | Email de l'utilisateur |
| 6 | `GET /api/profile` | Absent | 403 | Accès refusé |
| 7 | `GET /api/hello` | Absent | 403 | Accès refusé |

## 💻 Aperçu du code

<details>
<summary><b>Dépendances et configuration</b></summary>

![pom](assets/code/01-pom-dependencies.png)
![properties](assets/code/02-application-properties.png)
</details>

<details>
<summary><b>Entité, repository et DTO</b></summary>

![AppUser](assets/code/03-appuser.png)
![UserRepository](assets/code/04-userrepository.png)
![RegisterRequest](assets/code/05-registerrequest.png)
![LoginRequest](assets/code/06-loginrequest.png)
![AuthResponse](assets/code/07-authresponse.png)
</details>

<details>
<summary><b>Services (UserDetailsService, JwtService)</b></summary>

![CustomUserDetailsService](assets/code/08-customuserdetailsservice.png)
![JwtService 1](assets/code/09-jwtservice-1.png)
![JwtService 2](assets/code/10-jwtservice-2.png)
</details>

<details>
<summary><b>Filtre JWT et configuration de sécurité</b></summary>

![JwtFilter 1](assets/code/11-jwtfilter-1.png)
![JwtFilter 2](assets/code/12-jwtfilter-2.png)
![SecurityConfig](assets/code/13-securityconfig.png)
</details>

<details>
<summary><b>Contrôleurs</b></summary>

![AuthController 1](assets/code/14-authcontroller-1.png)
![AuthController 2](assets/code/15-authcontroller-2.png)
![UserController](assets/code/16-usercontroller.png)
</details>

> Le secret JWT a été masqué sur les captures `02` et `09`.

## 🛠 Points d'amélioration

- **403 au lieu de 401** : aucun `AuthenticationEntryPoint` n'est configuré ; en déclarer un pour renvoyer 401 sur un jeton absent ou invalide.
- **Secret en dur** dans `JwtService` : utiliser `@Value("${jwt.secret}")` et `@Value("${jwt.expiration}")`.
- **`pom.xml`** : la dépendance `jjwt-jackson` est déclarée trois fois, n'en garder qu'une.
- **`JwtFilter`** : tester `startsWith("Bearer ")` (avec espace) et ne pas laisser le `catch` vide (journaliser ou répondre 401).
- **Logs** : remplacer les `System.out.println` de `CustomUserDetailsService` par un logger SLF4J.
- **Rôle** : harmoniser `"user"` (valeur par défaut de l'entité) et `"USER"` (valeur enregistrée).
- **Validation** : ajouter `@Email`, `@NotBlank`, une longueur minimale de mot de passe et `@Valid` sur les DTO.
- **Production** : base persistante, console H2 désactivée, jeton d'accès court + refresh token.

## 📄 Contexte

Travail pratique réalisé dans le cadre du cours sur la sécurisation d'API avec JWT (Spring Boot).
