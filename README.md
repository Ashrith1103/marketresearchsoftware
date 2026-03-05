# Market Research Software

A role-based market research platform built with Java + MySQL backend and a simple HTML/CSS/JS frontend.

## What this project does

This project supports three user roles:

- **Market Researcher**
	- Create, manage, and delete surveys
	- View catalogue, report summaries, and generated reports
- **Company Executive**
	- View company catalogue
	- View review data and report summaries
	- Generate reports for specific products
- **Customer**
	- View catalogue and available surveys
	- Fill surveys

The latest UI changes show role output directly in the browser (tables) instead of relying only on console logs.

## Tech stack

- **Backend:** Java (HTTP server + JDBC)
- **Frontend:** Vanilla HTML, CSS, JavaScript
- **Database:** MySQL

## Project structure

- `MarketResearchSW/` → Java backend source files
- `frontend/` → Web UI (login + role dashboards)
- `DB/` and `MarketResearchSW/marketresearchsw.sql` → SQL schema/data
- `Required JAR Files/` → dependencies (MySQL connector, etc.)

## Prerequisites

- Java JDK installed
- MySQL running locally
- Python installed (for serving frontend)

## Run locally

### 1) Compile backend

```powershell
cd "C:\Users\ASUS\OneDrive\Desktop\academics\Market-Research-Software"
javac .\MarketResearchSW\*.java
```

### 2) Start backend API

```powershell
java -cp ".;Required JAR Files\mysql-connector-j-8.0.33.jar" MarketResearchSW.ApiServer
```

Expected log:

`HTTP API server listening on http://localhost:8080`

### 3) Start frontend

```powershell
cd "C:\Users\ASUS\OneDrive\Desktop\academics\Market-Research-Software\frontend"
python -m http.server 5500
```

Open:

`http://localhost:5500`

## API quick test

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/login" -Method POST -ContentType "application/json" -Body '{"username":"demo","password":"qwerty"}'
```

## Report output location

Generated report files are written by default to:

`C:\Users\ASUS\Downloads\MarketResearchReports`

You can override this with environment variable `ReportOutputLocation`.

## Notes

- If backend fails to start with `Address already in use`, free port `8080` and restart.
- If role pages show empty tables, verify the logged-in user’s `company` mapping in `userlogin` matches records in `company` / `products`.






