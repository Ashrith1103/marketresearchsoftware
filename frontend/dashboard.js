const BACKEND_BASE_URL = "http://localhost:8080";

const sessionRaw = window.localStorage.getItem("mrSession");
const useMockBackend = window.localStorage.getItem("mrUseMockBackend") === "true";

const roleTitle = document.getElementById("role-title");
const roleDescription = document.getElementById("role-description");
const roleActions = document.getElementById("role-actions");
const statusBox = document.getElementById("action-status");
const logoutButton = document.getElementById("logout-btn");
const resultPanel = document.getElementById("result-panel");
const resultOutput = document.getElementById("result-output");

let session = null;

function setStatus(message, type) {
  statusBox.textContent = message;
  statusBox.classList.remove("status--hidden", "status--success", "status--error");
  if (type === "success") {
    statusBox.classList.add("status--success");
  } else if (type === "error") {
    statusBox.classList.add("status--error");
  }
}

function askRequired(promptText) {
  const value = window.prompt(promptText);
  if (value === null) return null;
  const trimmed = value.trim();
  if (!trimmed) return null;
  return trimmed;
}

function getActionParams(role, actionKey) {
  if (role === "MarketResearcher" && actionKey === "create-survey") {
    const pname = askRequired("Product name:");
    if (!pname) return null;
    const cname = askRequired("Company name:");
    if (!cname) return null;
    const q1 = askRequired("Survey question 1:");
    if (!q1) return null;
    const q2 = askRequired("Survey question 2:");
    if (!q2) return null;
    const q3 = askRequired("Survey question 3:");
    if (!q3) return null;
    return { pname, cname, q1, q2, q3 };
  }

  if (role === "CompanyExecutive" && actionKey === "generate-report") {
    const productId = askRequired("Product ID:");
    if (!productId) return null;
    const reportTypeInput = askRequired("Report type (HTML or PDF):");
    if (!reportTypeInput) return null;
    const reportType = reportTypeInput.toUpperCase() === "PDF" ? "PDF" : "HTML";
    return { productId, reportType };
  }

  if (role === "Customer" && actionKey === "fill-survey") {
    const surveyID = askRequired("Survey ID:");
    if (!surveyID) return null;
    const a1 = askRequired("Answer 1:");
    if (!a1) return null;
    const a2 = askRequired("Answer 2:");
    if (!a2) return null;
    const a3 = askRequired("Answer 3:");
    if (!a3) return null;
    return { surveyID, a1, a2, a3 };
  }

  if (role === "Customer" && actionKey === "fill-review") {
    const company = askRequired("Company name:");
    if (!company) return null;
    const product = askRequired("Product name:");
    if (!product) return null;
    const review = askRequired("Your review:");
    if (!review) return null;
    const ratingInput = askRequired("Rating (1-5):");
    if (!ratingInput) return null;
    const rating = Number.parseInt(ratingInput, 10);
    if (Number.isNaN(rating) || rating < 1 || rating > 5) {
      return { _invalid: "Rating must be a number between 1 and 5." };
    }
    return { company, product, review, rating: String(rating) };
  }

  if (role === "MarketResearcher" && actionKey === "delete-survey") {
    const surveyID = askRequired("Survey ID to delete:");
    if (!surveyID) return null;
    return { surveyID };
  }

  return {};
}

function renderResultTable(rows) {
  if (!rows || rows.length === 0) {
    resultOutput.innerHTML = "<p>No records found.</p>";
    resultPanel.classList.remove("role--hidden");
    return;
  }

  const columns = Object.keys(rows[0]);
  const table = document.createElement("table");
  table.className = "result-table";

  const thead = document.createElement("thead");
  const headRow = document.createElement("tr");
  columns.forEach((column) => {
    const th = document.createElement("th");
    th.textContent = column;
    headRow.appendChild(th);
  });
  thead.appendChild(headRow);
  table.appendChild(thead);

  const tbody = document.createElement("tbody");
  rows.forEach((row) => {
    const tr = document.createElement("tr");
    columns.forEach((column) => {
      const td = document.createElement("td");
      td.textContent = row[column] ?? "";
      tr.appendChild(td);
    });
    tbody.appendChild(tr);
  });
  table.appendChild(tbody);

  resultOutput.innerHTML = "";
  resultOutput.appendChild(table);
  resultPanel.classList.remove("role--hidden");
}

function renderActionOutput(result) {
  if (!resultPanel || !resultOutput) {
    return;
  }

  if (Array.isArray(result?.reports)) {
    renderResultTable(result.reports);
    return;
  }

  if (Array.isArray(result?.catalogue)) {
    renderResultTable(result.catalogue);
    return;
  }

  if (Array.isArray(result?.surveys)) {
    renderResultTable(result.surveys);
    return;
  }

  if (Array.isArray(result?.reviews)) {
    renderResultTable(result.reviews);
    return;
  }

  resultPanel.classList.add("role--hidden");
  resultOutput.innerHTML = "";
}

function getActionRoute(role, actionKey) {
  if (role === "MarketResearcher") {
    if (actionKey === "create-survey") return "/market-researcher/surveys";
    if (actionKey === "manage-surveys") return "/market-researcher/surveys";
    if (actionKey === "delete-survey") return "/market-researcher/surveys/delete";
    if (actionKey === "view-reports") return "/market-researcher/reports";
    if (actionKey === "manage-catalogue") return "/market-researcher/catalogue";
  } else if (role === "CompanyExecutive") {
    if (actionKey === "view-company-catalogue") return "/company-exec/catalogue";
    if (actionKey === "view-report-summary") return "/company-exec/reports";
    if (actionKey === "generate-report") return "/company-exec/reports";
    if (actionKey === "view-reviews") return "/company-exec/reviews";
  } else if (role === "Customer") {
    if (actionKey === "fill-survey") return "/customer/surveys/fill";
    if (actionKey === "fill-review") return "/customer/reviews/fill";
    if (actionKey === "view-available-reviews") return "/customer/reviews";
    if (actionKey === "view-available-surveys") return "/customer/surveys";
    if (actionKey === "view-catalogue") return "/customer/catalogue";
  }

  return null;
}

function mockRoleAction(role, actionKey) {
  const niceKey = actionKey.replace(/-/g, " ");
  return {
    success: true,
    message: `Simulated "${niceKey}" for role ${role}.`,
  };
}

async function callBackendAction(role, actionKey) {
  const path = getActionRoute(role, actionKey);
  if (!path) {
    return { success: false, message: "Unknown action." };
  }

  const url = new URL(`${BACKEND_BASE_URL}${path}`);
  url.searchParams.set("username", session.username);

  const extraParams = getActionParams(role, actionKey);
  if (extraParams === null) {
    return { success: false, message: "Action cancelled." };
  }
  if (extraParams?._invalid) {
    return { success: false, message: extraParams._invalid };
  }

  Object.entries(extraParams).forEach(([key, value]) => {
    url.searchParams.set(key, value);
  });

  const response = await fetch(url.toString(), { method: "GET" });
  if (!response.ok) {
    return { success: false, message: "Backend returned an error for this action." };
  }

  return response.json();
}

function getRoleConfig(role, company, accessLevel) {
  if (role === "MarketResearcher") {
    return {
      title: "Market Researcher dashboard",
      description: `You are logged in as a Market Researcher${
        company ? ` at ${company}` : ""
      }. Access level: ${accessLevel ?? "-"}.`,
      actions: [
        { label: "Create survey", key: "create-survey" },
        { label: "Manage surveys", key: "manage-surveys" },
        { label: "Delete survey", key: "delete-survey" },
        { label: "View and export reports", key: "view-reports" },
        { label: "Manage product catalogue", key: "manage-catalogue" },
      ],
    };
  }

  if (role === "CompanyExecutive") {
    return {
      title: "Company Executive dashboard",
      description: `You are logged in as a Company Executive${
        company ? ` at ${company}` : ""
      }. Access level: ${accessLevel ?? "-"}.`,
      actions: [
        { label: "View company catalogue", key: "view-company-catalogue" },
        { label: "View report summary", key: "view-report-summary" },
        { label: "Generate report", key: "generate-report" },
        { label: "View product reviews", key: "view-reviews" },
      ],
    };
  }

  return {
    title: "Customer portal",
    description: "You are logged in as a Customer. You can participate in surveys and view your history.",
    actions: [
      { label: "Fill a survey", key: "fill-survey" },
      { label: "Fill a review", key: "fill-review" },
      { label: "View available devices to review", key: "view-available-reviews" },
      { label: "View available surveys", key: "view-available-surveys" },
      { label: "View catalogue", key: "view-catalogue" },
    ],
  };
}

function renderDashboard() {
  const expectedRole = document.body.dataset.role;

  if (!sessionRaw) {
    window.location.replace("./index.html");
    return;
  }

  session = JSON.parse(sessionRaw);

  if (!session?.username || !session?.role) {
    window.location.replace("./index.html");
    return;
  }

  if (expectedRole && expectedRole !== session.role) {
    if (session.role === "MarketResearcher") {
      window.location.replace("./market-researcher.html");
      return;
    }
    if (session.role === "CompanyExecutive") {
      window.location.replace("./company-executive.html");
      return;
    }
    window.location.replace("./customer.html");
    return;
  }

  const config = getRoleConfig(session.role, session.company, session.accessLevel);
  roleTitle.textContent = config.title;
  roleDescription.textContent = config.description;

  roleActions.innerHTML = "";
  config.actions.forEach((action) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "pill pill--button";
    button.textContent = action.label;
    button.addEventListener("click", async () => {
      setStatus("Talking to backend…", "success");
      try {
        const result = useMockBackend
          ? mockRoleAction(session.role, action.key)
          : await callBackendAction(session.role, action.key);

        if (!result.success) {
          setStatus(result.message || "Action failed.", "error");
          return;
        }

        setStatus(result.message || "Action completed.", "success");
        renderActionOutput(result);
      } catch (error) {
        setStatus("Something went wrong while performing this action.", "error");
      }
    });
    roleActions.appendChild(button);
  });
}

logoutButton.addEventListener("click", () => {
  window.localStorage.removeItem("mrSession");
  window.location.replace("./index.html");
});

renderDashboard();
