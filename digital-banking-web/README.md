# CloudBank Customer Portal

React customer-facing web UI for the Digital Banking Platform API.

## Prerequisites

- **Node.js 22+** (installed at `C:\tools\nodejs` on this VM)
- **API running** at `http://localhost:8080/api`

## Quick start

```powershell
$env:Path = "C:\tools\nodejs;" + $env:Path
cd c:\banking\digital-banking-web
npm install
npm run dev
```

Open **http://localhost:5173** in your browser.

## Demo login

| Field | Value |
|-------|-------|
| Username | `customer` |
| Password | `changeme-customer` |
| Customer | Emma, Noah, or Priya (dropdown) |

The API must be running first (`mvn spring-boot:run` in `digital-banking-platform`).

## Scripts

| Command | Description |
|---------|-------------|
| `npm run dev` | Dev server with hot reload (port 5173) |
| `npm run build` | Production build to `dist/` |
| `npm run preview` | Preview production build |

## Architecture

- **Vite** proxies `/api` → `http://localhost:8080` (no CORS issues in dev)
- **HTTP Basic auth** stored in `sessionStorage` for API calls
- Pages: Dashboard, Accounts, Transfer, Payees, Transaction history, Notifications

## Remote access (Azure VM)

If browsing from your laptop, open NSG port **5173** and use `http://<vm-public-ip>:5173`.
