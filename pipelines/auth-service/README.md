# Auth Service Azure DevOps Pipeline

This pipeline builds a Docker image for `BACKEND/auth-service` and pushes it to Azure Container Registry (ACR).

## Prerequisites

- Azure Container Registry `minierpdevacr` exists.
- Azure DevOps Azure Resource Manager service connection `sc-mini-erp-dev-rg` is configured (Workload Identity Federation).
- The service connection identity has `AcrPush` RBAC on `minierpdevacr`.

## What This Pipeline Does

1. Triggers on changes to the `main` branch.
2. Uses a Microsoft-hosted `ubuntu-latest` agent.
3. Logs in to ACR with:
   - `az acr login --name minierpdevacr`
4. Builds the Docker image from:
   - Dockerfile: `BACKEND/auth-service/Dockerfile`
   - Build context: `BACKEND/auth-service`
5. Pushes the image to ACR.

## Resulting Image

`minierpdevacr.azurecr.io/auth-service:dev`
