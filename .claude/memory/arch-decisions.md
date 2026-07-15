---
name: adr-database-strategy
description: Гибрид Hibernate/Spring Data JPA + jOOQ для работы с БД
metadata:
  type: reference
---

# ADR-6: Гибрид JPA + jOOQ для доступа к данным

**Context:** Нужно и быстрое CRUD для сущностей, и сложные агрегирующие запросы для аналитики дашборда.

**Decision:** Использовать гибридный подход:

| Часть системы | Что использовать | Почему |
|---------------|-----------------|--------|
| CRUD-сущности (Task, AgentRun, CodeVersion, User, Project) | Hibernate / Spring Data JPA | Простые save/find/update — не нужен весь потенциал SQL-контроля |
| Аналитика дашборда (суммы бюджетов, графики score по раундам, A/B сравнение моделей) | jOOQ | Сложные агрегирующие запросы, предсказуемый и оптимальный SQL |
| Autodream/Memory (Этап 6) | Hibernate | Простая работа с фактами как сущностями, только CRUD + фильтрация |

**How to apply:**
- В `agentry-persistence` уже есть Spring Data JPA + Liquibase — это foundation для entity CRUD
- Когда дойдём до дашборда (Этап 3), добавим jOOQ как отдельный модуль или как дополнительную зависимость в persistence
- jOOQ будет работать поверх той же Liquibase-схемы (codegen из существующей БД)
- Оба подхода используют одни и те же миграции — никакого рассинхрона

**Why:** jOOQ даёт type-safe SQL с полным контролем над планом запроса для сложной аналитики, но избыточен для простого CRUD. JPA отлично справляется с entity-операциями. Вместе — best of both worlds.
