# DND Encounter Tool
 
A full-stack web application for Dungeon Masters to manage D&D 5e campaigns and run combat encounters — tracking initiative, HP, AC, conditions, and turn order in real time, without leaving the browser.
 
Built with Spring Boot and PostgreSQL, with a primarily AJAX-driven frontend.

The project is currently live at dndencountertool.com
 
---
 
## Features
 
- **Campaign management** — create and organize multiple campaigns per user, each with its own party members, monster templates, and encounters
- **Encounter tracking** — add combatants (party members or NPCs/enemies), set initiative, and track turn order through combat
- **Server-persisted turn tracking** — current turn is stored in the database (not just the browser session), so it survives refreshes and reconnects
- **HP & AC tracking** — HP shown as a `current/max` pair; AC is tracked for NPCs and enemies
- **Status conditions** — attach and remove status effects (e.g., poisoned, stunned) per combatant
- **Per-combatant notes** — freeform notes for any combatant, with inline add/edit/delete and no page reload
- **Monster templates (prefabs)** — save reusable NPC/monster stat blocks per campaign; adding one to an encounter auto-fills name, HP, and AC (all still editable) and auto-numbers duplicates (e.g., "Goblin 1", "Goblin 2")
- **Incapacitated tracking** — mark combatants as down without removing them from the encounter
- **Safe deletion** — deletion requires more confirmation the more it destroys: party members delete instantly, encounters require a confirm dialog, and campaigns require typing the exact campaign name before the delete button unlocks
- **User accounts** — registration and login via Spring Security, with each user's campaigns scoped to their account
## Tech Stack
 
**Backend**
- Java, Spring Boot 4.1.0
- Spring Security (authentication/session management)
- Spring Data JPA / Hibernate
- PostgreSQL
**Frontend**
- Thymeleaf (server-rendered page shells)
- Standard JavaScript (fetch API, modals, DOM state management)
- HTML/CSS
**Infrastructure**
- Docker & docker-compose
- AWS EC2 (Ubuntu) for the application container
- AWS RDS (PostgreSQL) for the database
- Deployed behind an Elastic IP
## Architecture Notes
 
A couple things worth calling out for anyone reading the code:
 
- **Security model:** `User` implements `UserDetails` directly rather than wrapping a separate principal class.
- **Controllers are split by responsibility:** page-serving `@Controller` classes are separate from JSON-only `@RestController` API classes (under `/api`), keeping view logic and data logic from bleeding into each other.
- 
## Domain Model
 
```
User
 └── Campaign
      ├── PartyMember
      ├── MonsterTemplate      (reusable NPC stat blocks: name, HP, max HP, AC)
      └── Encounter
           └── Combatant
                ├── Status (one-to-many)
                └── notes (freeform text)
```
 
## Getting Started (Local Development)
 
### Prerequisites
- Java 21+
- Maven
- Docker & docker-compose
- PostgreSQL (or use the provided docker-compose setup)
### Setup
 
1. Clone the repository:
```bash
   git clone https://github.com/rwm76s/DND-Encounter-Tool.git
   cd DND-Encounter-Tool
```
 
2. Create a local `.env` file with your database credentials (DB_USERNAME, DB_PASSWORD, DB_HOST, DB_PORT, and DB_NAME).

4. Start the application and database with Docker Compose:
```bash
   docker compose up --build
```
 
4. The app will be available at `http://localhost:8080` (or whatever port is configured).
> Note: `ddl-auto=update` is used, so the schema will be created/updated automatically against a fresh database on first run.
 
## Deployment
 
The application runs in production as a containerized Spring Boot app on an AWS EC2 instance, connecting to an AWS RDS PostgreSQL instance. The container is configured to restart automatically (`restart: unless-stopped`).
 
## License

This project is licensed under the MIT License - see the LICENSE file for details.
