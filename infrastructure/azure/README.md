# Secure Digital Banking Platform on Microsoft Azure

Infrastructure-as-code and deployment guide for the CloudBank platform.

## Architecture

```
Internet
  └── Azure DNS Zone
        └── Azure Front Door (CDN + WAF)
              ├── /api/*  → App Service (Java API)
              └── /*      → App Service (React Web)
                        └── Azure SQL Database
                        └── Azure Key Vault (secrets)
                        └── Application Insights (monitoring)

VNet (10.0.0.0/16)
  ├── snet-app        → App Service VNet integration
  ├── snet-data       → SQL private endpoint (optional)
  ├── snet-keyvault   → Key Vault private endpoint (optional)
  ├── snet-vm         → Linux + Windows VMs (Site Recovery)
  ├── AzureFirewallSubnet → Azure Firewall (VM egress control)
  ├── AzureBastionSubnet  → Azure Bastion (secure VM access)
  └── snet-gateway    → Application Gateway (regional LB + WAF)
```

## Azure services deployed

| # | Service | Purpose |
|---|---------|---------|
| 1 | Resource Group | Container for all resources |
| 2 | Virtual Network + Subnets | Network isolation |
| 3 | Virtual Machines (Linux + Windows) | DR / management workloads |
| 4 | Storage Account | Blob backups + file share |
| 5 | Azure SQL Database | Persistent banking data |
| 6 | Site Recovery Vault | VM disaster recovery |
| 7 | Key Vault | Database credentials and secrets |
| 8 | Microsoft Entra ID | API authentication (JWT) + SQL admin |
| 9 | Monitor + App Insights | Infrastructure and app monitoring |
| 10 | Front Door + CDN + WAF | Global entry, caching, web protection |
| 11 | App Service | Host API (Java 21) and Web (Node 22) |
| 12 | Application Gateway | Regional load balancer + WAF |
| 13 | DNS Zones | Custom domain routing |
| 14 | Azure Firewall | Restrict VM outbound traffic |
| 15 | WAF (Front Door + App Gateway) | Web application protection |
| 16 | Azure Bastion | Secure RDP/SSH to VMs without public IPs |

## Prerequisites

- [Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli)
- Active Azure subscription
- Microsoft Entra ID tenant with:
  - **API app registration** (expose scope `access_as_user`, app roles `CUSTOMER`, `ADMIN`)
  - **SPA app registration** (redirect URI = web app URL, API permissions to the API app)
  - **Entra admin user** for SQL Database (`user@yourdomain.com`)

## 1. Create the resource group

```bash
az group create --name rg-cloudbank-prod --location canadacentral
```

## 2. Configure parameters

Edit `main.parameters.json`:

```json
{
  "entraAdminEmail": { "value": "admin@yourdomain.com" },
  "entraTenantId": { "value": "<your-tenant-id>" },
  "entraApiClientId": { "value": "<api-app-client-id>" },
  "entraSpaClientId": { "value": "<spa-app-client-id>" }
}
```

## 3. Deploy infrastructure

```bash
cd infrastructure/azure
az deployment group create \
  --resource-group rg-cloudbank-prod \
  --template-file main.bicep \
  --parameters main.parameters.json
```

## 4. Grant Entra ID SQL Database access

Connect to the `digitalbanking` database as the Entra SQL admin and run:

```sql
-- scripts/entra-sql-admin.sql
CREATE USER [user@yourdomain.com] FROM EXTERNAL PROVIDER;
ALTER ROLE db_owner ADD MEMBER [user@yourdomain.com];
```

## 5. Build and deploy the API

```bash
cd digital-banking-platform
mvn clean package -DskipTests
az webapp deploy --resource-group rg-cloudbank-prod \
  --name <api-app-name-from-output> \
  --src-path target/digital-banking-platform-0.0.1-SNAPSHOT.jar \
  --type jar
```

Or use Docker:

```bash
docker build -t cloudbank-api .
# Push to Azure Container Registry and deploy to App Service
```

## 6. Build and deploy the Web UI

```bash
cd digital-banking-web
npm install
VITE_AUTH_MODE=entra \
VITE_API_BASE_URL=https://<api-app>.azurewebsites.net/api \
VITE_ENTRA_TENANT_ID=<tenant-id> \
VITE_ENTRA_CLIENT_ID=<spa-client-id> \
VITE_ENTRA_API_SCOPE=api://<api-client-id>/access_as_user \
npm run build

az webapp deploy --resource-group rg-cloudbank-prod \
  --name <web-app-name-from-output> \
  --src-path dist \
  --type static
```

## Application profiles

| Profile | Database | Auth | Use case |
|---------|----------|------|----------|
| `local` (default) | H2 in-memory | HTTP Basic | Developer workstation |
| `azure` | Azure SQL + Flyway | Entra ID JWT | App Service production |
| `test` | H2 in-memory | HTTP Basic | CI / unit tests |

Set on App Service: `SPRING_PROFILES_ACTIVE=azure`

## Key Vault secrets

| Secret | Purpose |
|--------|---------|
| `sql-admin-username` | SQL login username |
| `sql-admin-password` | SQL login password |
| `vm-admin-password` | VM administrator password |

App Service reads these via `@Microsoft.KeyVault(SecretUri=...)` references with managed identity.

## Monitoring

- **Application Insights** — API request traces, dependencies, exceptions
- **Log Analytics** — 30-day retention
- **Action Group** — configure email receivers in `modules/monitoring.bicep`

Health check endpoint: `GET /api/actuator/health`

## VM access

VMs have **no public IPs**. Connect via **Azure Bastion** in the Azure Portal (RDP for Windows, SSH for Linux).

## Site Recovery

The Recovery Services Vault is provisioned. Enable replication per VM in the Azure Portal after deployment.

## Local development (unchanged)

```bash
# API
cd digital-banking-platform && mvn spring-boot:run

# Web
cd digital-banking-web && npm run dev
```

Local mode continues to use H2 + HTTP Basic auth with no Azure dependencies.
