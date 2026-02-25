# Commit Messages

## Format

```
<prefix>: <description>
```

Use lowercase prefix followed by a colon and a short description of the change.

## Prefixes

| Prefix        | Usage                                       | Example                                                  |
|---------------|---------------------------------------------|----------------------------------------------------------|
| `feature`     | New functionality                           | `feature: active POI for sessions`                       |
| `fix`         | Bug fix                                     | `fix: prevent app crash on location disabling`           |
| `ui`          | Visual/UI changes                           | `ui: notification icon accent color update`              |
| `refactoring` | Code restructuring without behavior change  | `refactoring: common google map impl extraction`         |
| `security`    | Security improvements                       | `security: sensitive data encryption`                    |
| `docs`        | Documentation changes                       | `docs: update module dependency graph`                   |
| `cicd`        | CI/CD pipeline changes                      | `cicd: add deploy-docs workflow`                         |
| `config`      | Configuration, build, or dependency changes | `config: migrate to version catalog`                     |
| `release`     | Version bump and release prep               | `release: 1.5.1`                                        |

## Guidelines

- Keep the description concise — ideally under 72 characters
- Use imperative or descriptive tone (both are acceptable)
- No capitalization after the prefix
- No trailing period
