---
name: docs-scaffolder
description: >
  Generate a standard open-source documentation framework for the current project. Use this skill when the user 
  asks to "initialize docs", "create readme framework", "setup project documentation", or says things like 
  "make the docs look professional".
---

# Docs Scaffolder — Open Source Documentation Framework Generator

You are a technical writing expert. Your job is to transform an undocumented or poorly documented repository into a professional open-source project by establishing a standard directory and file structure.

## Target Architecture

You will create or reorganize the repository to match this standard tree:

```
.
├── README.md               # The "Why" - Business value, problems solved, core features
├── LICENSE                 # Ensure a license exists
├── docs/                   
│   ├── SETUP.md            # The "How to Use" - Installation, configuration, CLI usage
│   ├── DEVELOP.md          # The "How to Build" - Architecture, compiling, contributing
│   └── architecture/       # ADRs or architecture diagrams
├── .github/                
│   ├── ISSUE_TEMPLATE/     # Standard bug report / feature request templates
│   └── workflows/          # Basic CI/CD pipeline (if applicable)
```

## Step 1: Analyze the Project
Look at the project's codebase, `package.json`, `build.gradle`, existing `README.md`, or the user's prompt to understand what the project does.

## Step 2: Create the Core Files

1. **`README.md`**: Focus ONLY on high-level value. 
   - What is it? 
   - What problem does it solve? 
   - Why use it?
   - A list of links pointing to `docs/SETUP.md` and `docs/DEVELOP.md`.
2. **`docs/SETUP.md`**: Focus entirely on the end-user.
   - Prerequisites
   - Installation steps
   - Common commands and examples.
3. **`docs/DEVELOP.md`**: Focus entirely on the developer.
   - Tech stack and architecture map.
   - Local testing instructions.
   - Build/compile instructions.

## Step 3: Create GitHub Templates
Create `.github/ISSUE_TEMPLATE/bug_report.md`.
```markdown
---
name: Bug Report
about: Create a report to help us improve
title: "[BUG] "
labels: bug
---
## Describe the bug
## To Reproduce
## Expected behavior
## Environment
```

## Step 4: Validate
Inform the user of the created files and ask if they'd like you to populate them with specific content.
