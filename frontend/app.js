// Toggle this to true if you want to test quickly
// without a backend API. In that case we will
// "fake" the role based on the username and actions.
const USE_MOCK_BACKEND = false;
const BACKEND_BASE_URL = "http://localhost:8080";

const form = document.getElementById("login-form");
const statusBox = document.getElementById("login-status");
const rolePanel = document.getElementById("role-panel");
const roleTitle = document.getElementById("role-title");
const roleDescription = document.getElementById("role-description");
const roleActions = document.getElementById("role-actions");

function setStatus(message, type) {
  statusBox.textContent = message;
  statusBox.classList.remove("status--hidden", "status--success", "status--error");
  if (type === "success") {
    statusBox.classList.add("status--success");
  } else if (type === "error") {
    statusBox.classList.add("status--error");
  }
}

function hideStatus() {
  statusBox.classList.add("status--hidden");
}

async function loginWithBackend(username, password) {
  const response = await fetch(`${BACKEND_BASE_URL}/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });
  if (!response.ok) throw new Error("Login request failed");
  return response.json();
}

function loginWithMock(username, password) {
  // Simple mock rules just for UI testing
  if (!username || !password) {
    return { success: false, message: "Username and password are required." };
  }

  let role = "Customer";
  let company = "Acme Corp";
  let accessLevel = 1;

  const u = username.toLowerCase();
  if (u.includes("research")) {
    role = "MarketResearcher";
    accessLevel = 3;
  } else if (u.includes("exec") || u.includes("ceo")) {
    role = "CompanyExecutive";
    accessLevel = 5;
  }

  return {
    success: true,
    role,
    company,
    accessLevel,
    message: "Logged in (mock). Wire this to your real backend when ready.",
  };
}

function getRolePage(role) {
  switch (role) {
    case "MarketResearcher":
      return "./market-researcher.html";
    case "CompanyExecutive":
      return "./company-executive.html";
    case "Customer":
    default:
      return "./customer.html";
  }
}

function redirectIfSessionExists() {
  const sessionRaw = window.localStorage.getItem("mrSession");
  if (!sessionRaw) return;

  try {
    const session = JSON.parse(sessionRaw);
    if (!session?.role) return;
    window.location.replace(getRolePage(session.role));
  } catch (error) {
    window.localStorage.removeItem("mrSession");
  }
}

redirectIfSessionExists();

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  hideStatus();
  if (rolePanel) rolePanel.classList.add("role--hidden");

  const formData = new FormData(form);
  const username = formData.get("username");
  const password = formData.get("password");

  setStatus("Signing you in…", "success");

  try {
    let result;
    if (USE_MOCK_BACKEND) {
      result = loginWithMock(username, password);
    } else {
      result = await loginWithBackend(username, password);
    }

    if (!result.success) {
      setStatus(result.message || "Invalid username or password.", "error");
      return;
    }

    const session = {
      username,
      role: result.role,
      company: result.company,
      accessLevel: result.accessLevel,
    };

    window.localStorage.setItem("mrSession", JSON.stringify(session));
    window.localStorage.setItem("mrUseMockBackend", String(USE_MOCK_BACKEND));

    setStatus(result.message || "Logged in successfully.", "success");
    window.location.href = getRolePage(result.role);
  } catch (err) {
    console.error(err);
    setStatus("Something went wrong while logging in. Please try again.", "error");
  }
});

