# GetStarted — set up the OpenMRS backend (2.8.x) from source

This folder gets any teammate from "fresh machine" to "running OpenMRS with all
backend modules loaded" on **Windows, Linux, or Mac**, using only public GitHub
sources (no Azure DevOps credentials required).

It clones the same backend modules the project lead uses (minus
`chartsearchai` and `azureblob-storage`), checks each out to the team's branch
of interest, builds them into `.omod` files, and runs openmrs-core under the
Maven Jetty plugin with **H2** (default), **MySQL/MariaDB**, or **PostgreSQL**.

> **Target branch:** openmrs-core **`2.8`** (the personal `2.8-local` branch with
> the `-local` suffix stripped). If your openmrs-core checkout isn't on `2.8`:
>
> ```bash
> git -C <path-to-openmrs-core> checkout 2.8
> ```

Everything below ships in two equivalent forms — pick the one for your OS:

| OS | Scripts to use |
|---|---|
| Windows | `*.ps1` (PowerShell) |
| Mac / Linux | `*.sh` (bash) — also works on Windows via Git Bash |

---

## Prerequisites

Install these first. The scripts assume they're on your `PATH`.

1. **Git** — https://git-scm.com
2. **Maven** (3.8+) — https://maven.apache.org/download.cgi
3. **Two JDKs** from Eclipse Temurin (https://adoptium.net):
   - **JDK 21** (primary — used by openmrs-core and most modules)
   - **JDK 8** (required by 3 older modules: `appointmentscheduling`,
     `appointments`, `serialization.xstream`)

   Set environment variables pointing at each JDK home so the build scripts can
   switch between them without touching your system default:

   | | Windows (PowerShell) | Mac/Linux (bash) |
   |---|---|---|
   | Java 21 | `setx JAVA21_HOME "C:\Program Files\Eclipse Adoptium\jdk-21.0.x.x-hotspot"` | `export JAVA21_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home` (in `~/.bashrc`) |
   | Java 8  | `setx JAVA8_HOME "C:\Program Files\Eclipse Adoptium\jdk-8.0.x.x-hotspot"` | `export JAVA8_HOME=/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home` |

   You can also pass them per-run: `-Java21`/`-Java8` (PowerShell) or
   `--java21`/`--java8` (bash).

> The JDK swap is **session-scoped** — your system default Java is left
> untouched.

---

## Quick start

**Windows (PowerShell)** — from this `GetStarted` folder:

```powershell
# 1. Clone all 24 backend modules from public GitHub upstreams
.\clone-repos.ps1

# 2. Build them into .omod files (-> .\omod\)
.\build-modules.ps1

# 3. Drop the built modules where openmrs-core will load them from
Copy-Item .\omod\*.omod .\openmrs-data\modules\    # folder is created on first run

# 4. Run openmrs-core (H2 in-memory, zero-config)
.\run-openmrs.ps1
```

**Mac / Linux (bash)** — from this `GetStarted` folder:

```bash
# 1. Clone
./clone-repos.sh

# 2. Build
./build-modules.sh

# 3. Drop the modules where openmrs-core loads them
mkdir -p openmrs-data/modules && cp omod/*.omod openmrs-data/modules/

# 4. Run
./run-openmrs.sh
```

Then open **http://localhost:8080/openmrs** and log in with **admin / Admin123**.

The rest of this doc explains each step in detail, how to use MySQL/PostgreSQL
instead of H2, and what to do when a build fails.

---

## Step 1 — Clone the backend modules

```powershell
.\clone-repos.ps1                       # Windows
```
```bash
./clone-repos.sh                        # Mac / Linux
```

What it does, per module listed in [`modules.csv`](modules.csv):

1. Clones `https://github.com/<owner>/<repo>.git` into
   `<workspace>/<Category>/<repo>` (e.g. `.../Backend/openmrs-module-billing`,
   `.../Dependencies/openmrs-module-event`). The default workspace root is three
   levels above this folder — the same folder that already contains
   `Backend/openmrs-core`, so the layout matches the lead's. Override with
   `-Destination` / `--destination`.
2. If a module folder already exists, it runs `git fetch --all` instead of
   re-cloning — **safe to re-run**.
3. Checks out the module's **team branch of interest** (the `branch` column in
   `modules.csv`). These are the lead's working branches with the personal
   `-local` suffix stripped (`main-local` → `main`, `3.x-local` → `3.x`,
   `2.x-local` → `2.x`, etc.). If a target branch is missing on the upstream, it
   falls back to the repo's default branch and prints a **WARNING**.

**Owners:** most modules live under the `openmrs` GitHub org, except
`openmrs-module-appointments` and `openmrs-module-teleconsultation` (`Bahmni`)
and `openmrs-module-initializer` (`mekomsolutions`). All public — no auth.

The 24 modules and their branches:

| Category | Repo | Owner | Branch | Build |
|---|---|---|---|---|
| Backend | openmrs-module-billing | openmrs | main | Java 21 |
| Backend | openmrs-module-emrapi | openmrs | master | Java 21 |
| Backend | openmrs-module-fhir2 | openmrs | master | Java 21 |
| Backend | openmrs-module-idgen | openmrs | master | Java 21 |
| Backend | openmrs-module-openconceptlab | openmrs | master | Java 21 |
| Backend | openmrs-module-patientdocuments | openmrs | main | Java 21 `-DskipTests` |
| Backend | openmrs-module-queue | openmrs | main | Java 21 |
| Backend | openmrs-module-reporting | openmrs | master | Java 21 |
| Backend | openmrs-module-reportingrest | openmrs | master | Java 21 |
| Backend | openmrs-module-webservices.rest | openmrs | 3.x | Java 21 |
| Backend | openmrs-module-legacyui | openmrs | 2.x | Java 21 |
| Backend | openmrs-module-tasks | openmrs | main | Java 21 |
| Backend | openmrs-module-appointmentscheduling | openmrs | master | Java 8 |
| Backend | openmrs-module-appointments | Bahmni | master | Java 8 |
| Dependencies | openmrs-module-event | openmrs | master | Java 21 |
| Dependencies | openmrs-module-metadatamapping | openmrs | master | Java 21 |
| Dependencies | openmrs-module-serialization.xstream | openmrs | master | Java 8 |
| Dependencies | openmrs-module-o3forms | openmrs | main | Java 21 |
| Dependencies | openmrs-module-htmlwidgets | openmrs | master | Java 21 |
| Dependencies | openmrs-module-calculation | openmrs | master | Java 21 |
| Dependencies | openmrs-module-stockmanagement | openmrs | master | Java 21 |
| Dependencies | openmrs-module-querystore | openmrs | main | Java 21 |
| Dependencies | openmrs-module-teleconsultation | Bahmni | main | Java 21 |
| Dependencies | openmrs-module-initializer | mekomsolutions | main | Java 21 `-DskipTests` |

> To clone a single module (e.g. while smoke-testing): `.\clone-repos.ps1 -Only openmrs-module-idgen` / `./clone-repos.sh --only openmrs-module-idgen`.

---

## Step 2 — Build the `.omod` files

```powershell
.\build-modules.ps1                     # Windows
```
```bash
./build-modules.sh                      # Mac / Linux
```

This runs the verified build matrix from `BACKEND.md` (chartsearchai removed).
Three groups, in order:

| Group | JDK | Command | Modules |
|---|---|---|---|
| 1 | Java 21 | `mvn -B -ntp clean package` | 19 |
| 2 | Java 8  | `mvn -B -ntp clean package` | 3 |
| 3 | Java 21 | `mvn -B -ntp clean package -DskipTests` | 2 |

The 2 modules in group 3 (`patientdocuments`, `initializer`) compile and package
fine, but their test suites are environment-broken; `-DskipTests` produces a
usable `.omod`. The 3 modules in group 2 genuinely require Java 8 (the JDK 16+
module system blocks the old PowerMock/javassist they rely on).

For each successful build, the produced `.omod` from `<repo>/omod/target/` is
copied into **`GetStarted/omod/`**. Per-module Maven logs go to
`GetStarted/.build-logs/<module>.log`; the same output streams live to the
console.

**openmrs-core itself is not built here** — it produces `openmrs.war`, not a
module `.omod`, and the modules resolve `openmrs-api` from your local `.m2`.
You only need to build core if you've changed it (see *Troubleshooting*).

### If some modules fail

```powershell
.\retry-failed-builds.ps1               # Windows — auto-detects failures
.\retry-failed-builds.ps1 -Modules openmrs-module-legacyui,openmrs-module-idgen   # explicit list
```
```bash
./retry-failed-builds.sh                # Mac / Linux
./retry-failed-builds.sh --modules openmrs-module-legacyui,openmrs-module-idgen
```

It re-runs the failures under the **same** JDK/skipTests profile as the main
build. Anything that still fails gets a structured digest at
`.build-logs/<module>.failure.md` (reactor summary, every `[ERROR]` line, failed
goals, the exception / `Caused by:` chain, the last 40 log lines) plus an
aggregate `.build-logs/retry-report.md`.

---

## Step 3 — Where to put the `.omod` files

OpenMRS loads modules from the **OpenMRS Application Data Directory**, in its
`modules/` subfolder.

When you run via `run-openmrs.*`, the data directory is set to
**`GetStarted/openmrs-data`** (override with `-DataDir` / `--data-dir`). So copy
your built modules there:

```powershell
Copy-Item .\omod\*.omod .\openmrs-data\modules\     # Windows
```
```bash
cp omod/*.omod openmrs-data/modules/                # Mac / Linux
```

If you **don't** use `run-openmrs.*` (e.g. you run `mvn jetty:run` manually
without `OPENMRS_APPLICATION_DATA_DIRECTORY`), the default data directory is
`~/.OpenMRS` (`C:\Users\<you>\.OpenMRS` on Windows), so modules go in
`~/.OpenMRS/modules/`.

Modules dropped into the folder are picked up on the next startup; for an
already-running server, upload them via **Administration → Advanced
Administration → Manage Modules**.

---

## Step 4 — Run openmrs-core (Jetty)

`run-openmrs.*` wraps the Maven Jetty commands the team uses and sets up the
three things people usually forget:

- **`MAVEN_OPTS`** with the `--add-opens` flags openmrs-core needs on Java 21
  (legacy reflection), plus an optional remote-debug JDWP agent.
- **`OPENMRS_APPLICATION_DATA_DIRECTORY`** pointing at `GetStarted/openmrs-data`.
- **`OPENMRS_INSTALLATION_SCRIPT`** for MySQL/PostgreSQL auto-install.

### H2 (default — in-memory, zero-config)

```powershell
.\run-openmrs.ps1                       # Windows
```
```bash
./run-openmrs.sh                        # Mac / Linux
```

Equivalent raw command (what the runner executes):

```bash
mvn -Ph2 -Djetty.http.port=8080 jetty:run
```

Open **http://localhost:8080/openmrs** → log in with **admin / Admin123**.

### Useful flags

| Flag (PS / bash) | Effect |
|---|---|
| `-Mode MySQL` / `--mode mysql` | Run against MySQL using `config/openmrs-installation.mysql.properties` |
| `-Mode Postgres` / `--mode postgres` | Run against PostgreSQL using `config/openmrs-installation.postgresql.properties` |
| `-PlWebapp` / `--pl-webapp` | Build/run only the `webapp` module (`mvn -pl webapp … jetty:run`) — faster startup |
| `-Debug` / `--debug` | Attach JDWP remote-debug agent (`suspend=n,address=*:8000`) |
| `-Port` / `--port` | HTTP port (default 8080) |

### The MAVEN_OPTS tip (Java 21)

On Java 21 the build/run sometimes needs the JVM module-access unlocks. The
runner sets these automatically, but if you invoke Maven directly:

```powershell
$env:MAVEN_OPTS="--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED"
# optional remote debug:
$env:MAVEN_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000 --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED"
```

---

## Step 5 — Database configuration

You have two ways to point OpenMRS at a real database. Both start from a
running local DB server (MySQL/MariaDB **or** PostgreSQL) with an empty
`openmrs` database.

### Option A — auto-install template (recommended)

Edit the matching file under [`config/`](config/) with your DB credentials, then
run `run-openmrs.*` in that mode. On first start OpenMRS installs itself (runs
liquibase, creates tables + the admin user) with **no web wizard**.

**MySQL / MariaDB** — edit [`config/openmrs-installation.mysql.properties`](config/openmrs-installation.mysql.properties):
```sql
-- one-time DB + user setup
CREATE DATABASE openmrs CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'openmrs'@'%' IDENTIFIED BY 'openmrs';
GRANT ALL PRIVILEGES ON openmrs.* TO 'openmrs'@'%';
FLUSH PRIVILEGES;
```
```bash
./run-openmrs.sh --mode mysql     # or: .\run-openmrs.ps1 -Mode MySQL
```

**PostgreSQL** — edit [`config/openmrs-installation.postgresql.properties`](config/openmrs-installation.postgresql.properties).
The first-run role needs `SUPERUSER` so liquibase can install the `uuid-ossp`
and soundex extensions:
```sql
CREATE DATABASE openmrs;
CREATE ROLE openmrs WITH LOGIN SUPERUSER PASSWORD 'openmrs';
```
```bash
./run-openmrs.sh --mode postgres  # or: .\run-openmrs.ps1 -Mode Postgres
```

> PostgreSQL is a first-class supported database in openmrs-core (the driver and
> full liquibase `dbms="postgresql"` handling ship in the platform) and is the
> project's current default direction.

### Option B — pre-placed runtime properties

Alternatively, copy [`config/openmrs-runtime.properties.template`](config/openmrs-runtime.properties.template)
to `<data dir>/openmrs-runtime.properties` (i.e.
`GetStarted/openmrs-data/openmrs-runtime.properties`) and edit it. OpenMRS reads
this at startup and skips the web wizard because the connection is already
specified.

### Option C — web installation wizard

Skip all config and just run `.\run-openmrs.ps1` (H2) **or** run plain
`mvn -Djetty.http.port=8080 jetty:run` (no `-Ph2`, no install script). On first
open of `http://localhost:8080/openmrs` the web installer appears — step through
it and choose your DB type.

---

## Keeping your clones fresh (optional)

To fast-forward every module from its GitHub upstream (the same thing the lead's
`sync-upstreams.ps1` does):

```powershell
.\refresh-from-upstream.ps1            # Windows
```
```bash
./refresh-from-upstream.sh             # Mac / Linux
```

It adds an `upstream` remote to each repo and fast-forwards the **base** branch
(`-local` stripped) — it never touches a `-local` working branch or rewrites
history.

---

## Troubleshooting

- **"No JDK configured for Java 8 / 21"** — set `JAVA8_HOME` and `JAVA21_HOME`
  (or `JAVA_HOME`), or pass `-Java8`/`-Java21` (`--java8`/`--java21`).
- **Build fails with `Source option 6 is no longer supported`** or
  `module java.base does not "opens java.lang"` — that module needs Java 8, not
  21. Make sure `JAVA8_HOME` points at a real JDK 8.
- **openmrs-module-querystore fails on dependency resolution** (`openmrs
  2.9.0-SNAPSHOT` missing) — build openmrs-core first so the snapshot lands in
  your `.m2`:
  ```bash
  cd <workspace>/Backend/openmrs-core
  mvn -B -ntp clean install -DskipTests
  ```
- **A module's `.omod` doesn't appear after a "successful" build** — some modules
  produce test-fixture omods in other folders; the build script only copies from
  `<repo>/omod/target/`. Check that folder.
- **Module doesn't load at runtime** — confirm the `.omod` is in
  `GetStarted/openmrs-data/modules/` (when using `run-openmrs.*`) and watch the
  startup log for module-load errors. Legacy modules may need a compatible core
  version.
- **Port 8080 already in use** — run with `-Port 8090` / `--port 8090`.
- **`mvn : The term 'mvn' is not recognized`** — Maven isn't on your `PATH`.
- **PowerShell `mvn --%` parsing** — the raw commands you may see elsewhere use
  `--%` (the stop-parsing token); the `run-openmrs.*` wrapper handles quoting for
  you so you don't need it.

---

## What this folder contains

```
GetStarted/
├── README.md                       this doc
├── modules.csv                     single source of truth (repo, owner, branch, build group)
├── clone-repos.{ps1,sh}            Step 1 — clone 24 modules from GitHub
├── build-modules.{ps1,sh}          Step 2 — build matrix -> omod/
├── retry-failed-builds.{ps1,sh}    re-run failures + failure digests
├── refresh-from-upstream.{ps1,sh}  optional: fast-forward modules from upstream
├── run-openmrs.{ps1,sh}            Step 4 — run openmrs-core under Jetty
├── config/
│   ├── openmrs-installation.mysql.properties      MySQL/MariaDB auto-install template
│   ├── openmrs-installation.postgresql.properties PostgreSQL auto-install template
│   └── openmrs-runtime.properties.template        pre-placed runtime config alternative
├── omod/                           (created) built .omod files land here
└── openmrs-data/                   (created) OpenMRS app-data dir used by run-openmrs.*
```
