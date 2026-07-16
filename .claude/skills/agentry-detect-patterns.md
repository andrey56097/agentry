---
name: agentry-detect-patterns
description: Scan project for repeated patterns and propose new AI agents — with cost estimate and permission first
---

Detect patterns in the project that suggest creating new specialized agents.

## Step 1: Estimate scan cost

Run these cheap shell commands (0 LLM tokens — just `find`/`grep`):

```bash
# Count files by type — quick project size estimate
echo "=== Java files ===" && find . -name "*.java" ! -path "*/build/*" ! -path "*/.gradle/*" | wc -l
echo "=== Shell/Methods ===" && grep -r "@ShellMethod" --include="*.java" . 2>/dev/null | wc -l
echo "=== REST Controllers ===" && grep -r "@RestController" --include="*.java" . 2>/dev/null | wc -l
echo "=== React components ===" && grep -rs "function.*Component\|useState\|useEffect" --include="*.{tsx,jsx}" . 2>/dev/null | wc -l 2>/dev/null
echo "=== Docker files ===" && find . -name "Dockerfile" -o -name "docker-compose*" | wc -l
echo "=== JPA Repositories ===" && grep -r "extends.*Repository\|@Query" --include="*.java" . 2>/dev/null | wc -l
echo "=== Liquibase changelogs ===" && find . -name "*.sql" -path "*/liquibase/*" -o -name "db.changelog*" | wc -l
echo "=== GitHub Actions ===" && find .github -name "*.yml" 2>/dev/null | wc -l
echo "=== Exception handlers ===" && grep -r "@ExceptionHandler\|@ControllerAdvice\|@RestControllerAdvice" --include="*.java" . 2>/dev/null | wc -l
echo "=== Existing agents ===" && ls .claude/agents/ 2>/dev/null | wc -l
```

From the counts, estimate LLM cost (~1000-1500 tokens to analyze and propose = < $0.01).

## Step 2: Ask permission

Show the user:
```
📊 Scan estimate: ~$0.01 (just grep counts, no file contents read)
   Files to check: ~XX
```

Wait for approval before continuing.

## Step 3: Run full pattern scan

If approved, run the full scan:

```bash
# 1. Count patterns for each agent type
echo "=== Existing agents ===" && ls .claude/agents/
echo ""

# 2. Detect CLI commands
echo "=== @ShellMethod count ===" && grep -rn "@ShellMethod" --include="*.java" . --include="*.kt" 2>/dev/null | grep -v "/build/" | head -30

# 3. Detect REST patterns
echo "=== @RestController count ===" && grep -rn "@RestController" --include="*.java" . 2>/dev/null | grep -v "/build/" | head -30
echo "=== @Service count ===" && grep -rn "@Service" --include="*.java" . 2>/dev/null | grep -v "/build/" | head -30

# 4. Detect React/TSX patterns
echo "=== React components ===" && grep -rsn "function [A-Z]" --include="*.tsx" --include="*.jsx" . 2>/dev/null | head -30

# 5. Detect JPA/DB patterns
echo "=== Repository interfaces ===" && grep -rn "extends.*Repository" --include="*.java" . 2>/dev/null | grep -v "/build/" | head -20

# 6. Detect Docker/infra
echo "=== Docker files ===" && find . -name "Dockerfile" -o -name "docker-compose*" 2>/dev/null

# 7. Detect CI/CD
echo "=== Workflow files ===" && ls .github/workflows/ 2>/dev/null

# 8. Detect Shell/CLI in general
echo "=== @ShellComponent count ===" && grep -rn "@ShellComponent" --include="*.java" . 2>/dev/null | grep -v "/build/" | head -20

# 9. Detect repeated package imports (common patterns)
echo "=== Top-level packages ===" && find . -path "*/com/agentry/*" -type d ! -path "*/build/*" | cut -d/ -f6- | sort -u | head -30

# 10. Detect config files
echo "=== Config files at root ===" && find . -maxdepth 1 -name "*.yml" -o -name "*.yaml" -o -name "*.json" -o -name "*.toml" | sort
```

## Step 4: Analyze and propose

Collect the scan output and invoke the pattern-agent-proposer agent:

> Scanned project at $PWD. Here are the grep counts and patterns found.
> Check `.claude/agents/` for existing agents.
> Suggest new agents for patterns with sufficient signal (MEDIUM/HIGH confidence).
> For each suggestion, explain: what pattern was found, match count, what the agent would do.

## Step 5: Offer to create

After the agent proposes, ask:
"Shall I create any of these? I'll show the diff first."
