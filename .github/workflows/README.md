# GitHub Actions Workflows

This directory contains the CI/CD workflows for the Advanced Text Editor project.

## Workflows

### 1. CI Pipeline (`ci.yml`)
- **Purpose**: Continuous Integration
- **Triggers**: Push/PR to main/develop branches
- **Jobs**: Test, Code Quality, Security Scan, Build Artifacts, Docker Build

### 2. Release Pipeline (`release.yml`)
- **Purpose**: Automated releases
- **Triggers**: Git tags (v*) or manual dispatch
- **Jobs**: Create Release, Build & Upload, Docker Release, Documentation

### 3. Security Pipeline (`security.yml`)
- **Purpose**: Security scanning
- **Triggers**: Push/PR + weekly schedule
- **Jobs**: Dependency Check, CodeQL Analysis, Secret Scanning, License Check

## Quick Start

1. **Push to repository** - CI automatically runs
2. **Create release** - Tag with `v*` pattern
3. **View results** - Check Actions tab in GitHub

## Requirements

- GitHub repository with Actions enabled
- Proper branch protection rules
- Secrets configured (if needed)

## Configuration

See `CI_CD_DOCUMENTATION.md` in the project root for detailed setup instructions.