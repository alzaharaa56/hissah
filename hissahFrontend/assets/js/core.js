
/**
 * ============================================================================
 * Core Utilities & Configuration Module
 * ============================================================================
 * Central configuration, session management, DOM helpers, formatting utilities, 
 * and HTTP client wrapper (`request`) for the Hissah platform frontend.
 */


export const CONFIG = {
  API_BASE_URL:
    localStorage.getItem("hissah_api_base") || "http://localhost:8080",
  APP_NAME: "Hissah",
  LOCALE: "en-OM",
  CURRENCY: "OMR",
};
export const qs = (s, r = document) => r.querySelector(s);
export const qsa = (s, r = document) => [...r.querySelectorAll(s)];
export const session = {
  get() {
    try {
      return JSON.parse(localStorage.getItem("hissah_session")) || {};
    } catch {
      return {};
    }
  },
  set(data) {
    localStorage.setItem("hissah_session", JSON.stringify(data || {}));
  },
  clear() {
    localStorage.removeItem("hissah_session");
  },
  token() {
    return this.get().token || this.get().accessToken || "";
  },
  role() {
    return this.get().role || this.get().user?.role || "";
  },
  user() {
    return this.get().user || {};
  },
  company() {
    return this.get().company || {};
  },
};
export function money(v) {
  const n = Number(v || 0);
  return new Intl.NumberFormat(CONFIG.LOCALE, {
    style: "currency",
    currency: CONFIG.CURRENCY,
    minimumFractionDigits: 3,
  }).format(n);
}
export function date(v, withTime = false) {
  if (!v) return "—";
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return String(v);
  return new Intl.DateTimeFormat(
    CONFIG.LOCALE,
    withTime
      ? { dateStyle: "medium", timeStyle: "short" }
      : { dateStyle: "medium" },
  ).format(d);
}
export function esc(v = "") {
  return String(v).replace(
    /[&<>'"]/g,
    (c) =>
      ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" })[
        c
      ],
  );
}
export function badge(v = "") {
  const key = String(v).toLowerCase();
  return `<span class="badge ${key}">${esc(String(v).replaceAll("_", " "))}</span>`;
}
export function toast(message, type = "success") {
  let host = qs(".toast-host");
  if (!host) {
    host = document.createElement("div");
    host.className = "toast-host";
    document.body.append(host);
  }
  const t = document.createElement("div");
  t.className = `toast ${type}`;
  t.textContent = message;
  host.append(t);
  setTimeout(() => t.remove(), 4200);
}
export function loading(el) {
  if (el)
    el.innerHTML =
      '<div class="loading"><div class="loader" aria-label="Loading"></div></div>';
}
export function empty(
  title = "No records yet",
  text = "Records will appear here after they are created.",
) {
  return `<div class="empty"><img src="${asset("images/empty-state.svg")}" alt=""><h3>${esc(title)}</h3><p class="muted">${esc(text)}</p></div>`;
}
export function asset(path) {
  const depth = location.pathname.split("/").filter(Boolean).length - 1;
  return `${"../".repeat(Math.max(0, depth))}assets/${path}`;
}
export function page(path) {
  const depth = location.pathname.split("/").filter(Boolean).length - 1;
  return `${"../".repeat(Math.max(0, depth))}${path}`;
}
export async function request(
  path,
  { method = "GET", body, headers = {}, auth = true, form = false } = {},
) {
  const h = { ...headers };
  if (auth && session.token()) h.Authorization = `Bearer ${session.token()}`;
  if (body && !form) h["Content-Type"] = "application/json";
  let response;
  try {
    response = await fetch(CONFIG.API_BASE_URL + path, {
      method,
      headers: h,
      body: body ? (form ? body : JSON.stringify(body)) : undefined,
    });
  } catch (e) {
    throw new Error(
      "Cannot connect to the Hissah backend. Confirm Spring Boot is running on port 8080.",
    );
  }
  const ct = response.headers.get("content-type") || "";
  const data = ct.includes("json")
    ? await response.json().catch(() => ({}))
    : await response.text();
  if (!response.ok) {
    const msg =
      data?.message || data?.error || `Request failed (${response.status})`;
    const err = new Error(msg);
    err.status = response.status;
    err.data = data;
    throw err;
  }
  return data;
}
export function query(params = {}) {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== "" && v !== null && v !== undefined) q.set(k, v);
  });
  const s = q.toString();
  return s ? `?${s}` : "";
}
export function idParam(name = "id") {
  return new URLSearchParams(location.search).get(name);
}
export function requireAuth(roles = []) {
  if (!session.token()) {
    location.replace(page("public/login.html"));
    return false;
  }
  if (roles.length && !roles.includes(session.role())) {
    toast("This page is not available for your account role.", "error");
    setTimeout(() => location.replace(dashboardPath()), 700);
    return false;
  }
  return true;
}
export function dashboardPath(role = session.role()) {
  return role === "ADMIN"
    ? page("admin/dashboard.html")
    : role === "MAIN_CONTRACTOR"
      ? page("contractor/dashboard.html")
      : page("subcontractor/dashboard.html");
}
export function logout() {
  session.clear();
  location.replace(page("public/login.html"));
}
export function formObject(form) {
  const fd = new FormData(form),
    o = {};
  for (const [k, v] of fd.entries()) {
    if (k in o) o[k] = [].concat(o[k], v);
    else o[k] = v;
  }
  return o;
}
export function numberOrNull(v) {
  return v === "" || v === null ? null : Number(v);
}
