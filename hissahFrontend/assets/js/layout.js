
/**
 * ============================================================================
 * Layout and Navigation Module
 * ============================================================================
 * This module relies on the core application structure (core.js) to manage
 * different interface layouts (public, application dashboards, and auth pages).
 */


import { asset, page, session, logout, qs, qsa } from "./core.js";
const nav = {
  MAIN_CONTRACTOR: [
    ["Dashboard", "contractor/dashboard.html", "⌂"],
    ["My Projects", "contractor/projects.html", "▦"],
    ["Create Project", "contractor/project-form.html", "＋"],
    ["Awards & Milestones", "shared/my-awards.html", "✓"],
    ["Reports", "contractor/reports.html", "▥"],
    ["Notifications", "shared/notifications.html", "●"],
    ["Company Profile", "shared/company-profile.html", "◉"],
    ["User Profile", "shared/user-profile.html", "♙"],
  ],
  SUBCONTRACTOR: [
    ["Dashboard", "subcontractor/dashboard.html", "⌂"],
    ["Browse Opportunities", "public/opportunities.html", "⌕"],
    ["My Bids", "subcontractor/my-bids.html", "▤"],
    ["Awards & Milestones", "shared/my-awards.html", "✓"],
    ["Notifications", "shared/notifications.html", "●"],
    ["Company Profile", "shared/company-profile.html", "◉"],
    ["User Profile", "shared/user-profile.html", "♙"],
  ],
  ADMIN: [
    ["Dashboard", "admin/dashboard.html", "⌂"],
    ["Verification Queue", "admin/verification-queue.html", "✓"],
    ["Categories", "admin/categories.html", "▦"],
    ["User Accounts", "admin/users.html", "♙"],
    ["Notifications", "shared/notifications.html", "●"],
    ["User Profile", "shared/user-profile.html", "◉"],
  ],
};
export function publicHeader() {
  return `<header class="public-header"><nav class="container public-nav"><a class="brand" href="${page("index.html")}"><img src="${asset("logo/hissah-logo.svg")}" alt="Hissah"></a><div class="nav-links"><a href="${page("public/opportunities.html")}">Opportunities</a><a href="${page("public/about.html")}">About</a><a href="${page("public/login.html")}">Login</a><a class="btn btn-primary btn-sm" href="${page("public/register.html")}">Create account</a></div><button class="icon-btn mobile-menu" id="public-menu">☰</button></nav></header>`;
}
export function footer() {
  return `<footer class="footer"><div class="container footer-grid"><div><div class="brand"><img src="${asset("logo/hissah-logo.svg")}" alt="Hissah"></div><p>Connecting Omani main contractors and SMEs through transparent subcontracting opportunities.</p></div><div><h3>Platform</h3><a href="${page("public/opportunities.html")}">Browse opportunities</a><br><a href="${page("public/about.html")}">How it works</a></div><div><h3>Access</h3><a href="${page("public/login.html")}">Login</a><br><a href="${page("public/register.html")}">Register</a></div></div></footer>`;
}
export function appShell(title = "Hissah") {
  const role = session.role() || "SUBCONTRACTOR",
    user = session.user(),
    items = nav[role] || nav.SUBCONTRACTOR;
  const current = location.pathname.split("/").slice(-2).join("/");
  return `<div class="app-shell"><aside class="sidebar" id="sidebar"><a class="sidebar-brand" href="${page("index.html")}"><img src="${asset("logo/hissah-logo.svg")}" alt="Hissah"></a><div class="side-section">${role.replaceAll("_", " ")}</div><nav class="side-nav">${items.map(([label, pathName, icon]) => `<a class="side-link ${current === pathName ? "active" : ""}" href="${page(pathName)}"><span>${icon}</span><span>${label}</span></a>`).join("")}</nav><div class="side-section">Session</div><button class="side-link" style="width:100%;border:0;background:transparent" id="logout"><span>↪</span><span>Logout</span></button></aside><div class="app-main"><header class="topbar"><div class="topbar-left"><button class="icon-btn sidebar-toggle" id="sidebar-toggle">☰</button><strong class="topbar-title">${title}</strong></div><div class="user-chip"><a href="${page("shared/notifications.html")}" class="icon-btn" style="display:grid;place-items:center">●</a><img src="${asset("images/avatar.svg")}" alt=""><div><strong>${user.fullName || "Hissah User"}</strong><div class="help">${role.replaceAll("_", " ")}</div></div></div></header><main class="app-content"><div id="page-root"></div></main></div></div>`;
}
export function mountPublic(content) {
  document.body.innerHTML =
    publicHeader() +
    `<main>${content}</main>` +
    footer() +
    `<div class="toast-host"></div>`;
  wire();
}
export function mountApp(title) {
  document.body.innerHTML = appShell(title) + `<div class="toast-host"></div>`;
  wire();
}
export function mountAuth(content) {
  document.body.innerHTML = content + `<div class="toast-host"></div>`;
  wire();
}
function wire() {
  qs("#logout")?.addEventListener("click", logout);
  qs("#sidebar-toggle")?.addEventListener("click", () =>
    qs("#sidebar")?.classList.toggle("open"),
  );
  qsa("a").forEach((a) =>
    a.addEventListener("click", () => qs("#sidebar")?.classList.remove("open")),
  );
}
