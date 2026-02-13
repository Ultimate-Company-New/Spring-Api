SonarQube integration
=====================

What's added
- `SpringApi/sonar-project.properties` — project scanner configuration
- `SpringApi/pom.xml` — added `sonar-maven-plugin` to allow `mvn sonar:sonar`
- `.github/workflows/sonar.yml` — CI workflow that runs Sonar analysis on push/PR

How to run locally

1. Start a SonarQube server (default: `http://localhost:9000`).
2. From `SpringApi` folder run:

```bash
mvn -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<TOKEN> sonar:sonar
```

Enabling / turning on all rules
--------------------------------
SonarQube rule activation is controlled by the SonarQube server's Quality Profiles. Enabling "all rules" must be done on the SonarQube server (UI) or via the SonarQube Web API. Example (server-side):

1. Log into SonarQube as an administrator.
2. Go to **Quality Profiles** → choose the language (Java) → activate rules you want (there's no supported switch to automatically enable every single rule from the scanner config).

Example using the SonarQube Web API to activate a rule (replace placeholders):

```bash
curl -u admin:ADMIN_TOKEN -X POST \
  "http://your-sonar-host/api/qualityprofiles/activate_rule?profileKey=java-sonar-way&ruleKey=java:S1123"
```

If you want a single-step activation of many rules, write a small script that calls `api/qualityprofiles/activate_rule` for each rule key. Be careful — enabling every rule can produce a large number of issues and may not be practical.

If you want, I can:
- add a script to call the SonarQube Web API to batch-enable a provided list of rule keys, or
- prepare instructions and a safe rule list to enable automatically.
VS Code task and convenience script
----------------------------------
I've added a VS Code task and a shell script so you can run Sonar locally without the Sonar extension.

- To run from VS Code: open Command Palette → Run Task → choose `Sonar: Maven scan (SpringApi)`.
- The task uses the Maven wrapper at `${workspaceFolder}/SpringApi/mvnw` and expects `SONAR_HOST_URL` and `SONAR_TOKEN` to be available in your environment.

- To run from a terminal at the repo root:

```bash
SONAR_HOST_URL=http://localhost:9000 SONAR_TOKEN=<TOKEN> ./scan-sonar.sh
```

Files added:
- `.vscode/tasks.json` — VS Code tasks to run the Maven Sonar goal
- `scan-sonar.sh` — convenience script to run the scan from the repo root

Notes:
- If you prefer running a single command from the `SpringApi` directory, you can use the Maven wrapper directly:

```bash
cd SpringApi
./mvnw -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<TOKEN> -DskipTests sonar:sonar
```

Be sure the SonarQube server is reachable and your token has permission to analyze the project.

In-editor issues (VS Code)
--------------------------
If you want Sonar issues to appear directly inside your editor (the Problems pane and inline diagnostics), use the SonarLint extension in connected mode.

1. Reinstall the SonarLint extension in VS Code (search for "SonarLint").
2. Bind SonarLint to your SonarQube server:
  - Open Command Palette → SonarLint: Bind to SonarQube or SonarCloud (connected mode).
  - Provide `SONAR_HOST_URL` (e.g., `http://localhost:9000`) and your token.
  - Choose the `SpringApi` project key.
3. Alternatively, copy `.vscode/sonarlint-settings.sample.json` to `.vscode/settings.json` and replace `<INSERT_SONAR_TOKEN_HERE>` with your token (do not commit the token).

After binding, run the analysis (Task: `Sonar: Maven scan (SpringApi)` or `scan-sonar.sh`). SonarLint will synchronize issues from the server and display them in the editor.

If you intentionally do not want the extension, note that running `mvn sonar:sonar` sends results to the SonarQube server only; VS Code alone will not show those issues without SonarLint or another plugin that reads Sonar server issues.

