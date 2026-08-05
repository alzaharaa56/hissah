/**
 * ============================================================================
 * API Services Module
 * ============================================================================
 * Consolidates all backend API endpoints used across the Hissah platform,
 * grouped by resource domain (Auth, Users, Companies, Projects, Bids, etc.).
 */

import { request, query } from "./core.js";
export const api = {
  auth: {
    login: (b) =>
      request("/api/auth/login", { method: "POST", body: b, auth: false }),
    register: (b) =>
      request("/api/auth/register", { method: "POST", body: b, auth: false }),
  },
  user: {
    me: (id) => request("/api/users/me" + query({ userId: id })),
    update: (id, b) =>
      request("/api/users/me" + query({ userId: id }), {
        method: "PUT",
        body: b,
      }),
  },
  company: {
    me: (id) => request("/api/companies/me" + query({ userId: id })),
    get: (id, cid, role) =>
      request(
        `/api/companies/${id}` +
          query({ currentCompanyId: cid, currentRole: role }),
      ),
    update: (id, b) =>
      request("/api/companies/me" + query({ companyId: id }), {
        method: "PUT",
        body: b,
      }),
    pending: () => request("/api/admin/companies/pending"),
    verify: (id, adminUserId, b) =>
      request(`/api/admin/companies/${id}/verify` + query({ adminUserId }), {
        method: "POST",
        body: b,
      }),
  },
  documents: {
    list: (id) => request(`/api/companies/${id}/documents`),
    upload: (id, fd) =>
      request(`/api/companies/${id}/documents`, {
        method: "POST",
        body: fd,
        form: true,
      }),
    remove: (id) =>
      request(`/api/company-documents/${id}`, { method: "DELETE" }),
    download: (id) => request(`/api/company-documents/${id}/download`),
  },
  categories: {
    active: () => request("/api/categories"),
    all: () => request("/api/categories/all"),
    get: (id) => request(`/api/categories/${id}`),
    create: (b) => request("/api/categories", { method: "POST", body: b }),
    update: (id, b) =>
      request(`/api/categories/${id}`, { method: "PUT", body: b }),
    deactivate: (id) =>
      request(`/api/categories/${id}/deactivate`, { method: "PATCH" }),
  },
  projects: {
    mine: (companyId, page = 0, size = 20) =>
      request(
        "/api/projects/my-projects" +
          query({ contractorCompanyId: companyId, page, size }),
      ),
    get: (id, cid, role) =>
      request(
        `/api/projects/${id}` +
          query({ currentCompanyId: cid, currentRole: role }),
      ),
    create: (cid, uid, b) =>
      request(
        "/api/projects" +
          query({ contractorCompanyId: cid, currentUserId: uid }),
        { method: "POST", body: b },
      ),
    update: (id, cid, uid, b) =>
      request(
        `/api/projects/${id}` +
          query({ contractorCompanyId: cid, currentUserId: uid }),
        { method: "PUT", body: b },
      ),
    action: (id, action, cid, uid, reason) =>
      request(
        `/api/projects/${id}/${action}` +
          query({ contractorCompanyId: cid, currentUserId: uid, reason }),
        { method: "PATCH" },
      ),
  },
  packages: {
    search: (p) => request("/api/work-packages" + query(p)),
    get: (id) => request(`/api/work-packages/${id}`),
    byProject: (id) => request(`/api/projects/${id}/packages`),
    create: (projectId, b) =>
      request(`/api/projects/${projectId}/packages`, {
        method: "POST",
        body: b,
      }),
    update: (id, b) =>
      request(`/api/work-packages/${id}`, { method: "PUT", body: b }),
    action: (id, action, b) =>
      request(`/api/work-packages/${id}/${action}`, {
        method: "PATCH",
        body: b,
      }),
  },
  ai: {
    generateWorkPackage: (b) =>
      request("/api/ai/work-packages/generate", { method: "POST", body: b }),
    findSubcontractorMatches: (workPackageId, limit = 5) =>
      request(
        `/api/ai/work-packages/${workPackageId}/matches` + query({ limit }),
      ),
  },

  bids: {
    mine: (page = 0, size = 20) =>
      request(
        "/api/bids/my-bids" + query({ page, size, sort: "createdAt,desc" }),
      ),
    get: (id) => request(`/api/bids/${id}`),
    forPackage: (id) => request(`/api/work-packages/${id}/bids`),
    create: (packageId, fd, draft = false) =>
      request(`/api/work-packages/${packageId}/bids${draft ? "/draft" : ""}`, {
        method: "POST",
        body: fd,
        form: true,
      }),
    update: (id, fd) =>
      request(`/api/bids/${id}`, { method: "PUT", body: fd, form: true }),
    action: (id, action, b) =>
      request(`/api/bids/${id}/${action}`, { method: "PATCH", body: b }),
  },
  awards: {
    mine: () => request("/api/awards/my-awards"),
    get: (id) => request(`/api/awards/${id}`),
    byPackage: (id) => request(`/api/work-packages/${id}/award`),
    create: (packageId, bidId, b) =>
      request(`/api/work-packages/${packageId}/award/${bidId}`, {
        method: "POST",
        body: b,
      }),
  },
  milestones: {
    list: (awardId) => request(`/api/awards/${awardId}/milestones`),
    create: (awardId, b) =>
      request(`/api/awards/${awardId}/milestones`, { method: "POST", body: b }),
    update: (awardId, id, b) =>
      request(`/api/awards/${awardId}/milestones/${id}`, {
        method: "PUT",
        body: b,
      }),
    progress: (id, b) =>
      request(`/api/milestones/${id}/progress`, { method: "PATCH", body: b }),
    submit: (id) =>
      request(`/api/milestones/${id}/submit`, { method: "PATCH" }),
    approve: (id) =>
      request(`/api/milestones/${id}/approve`, { method: "PATCH" }),
  },
  notifications: {
    list: (page = 0, size = 20) =>
      request(
        "/api/notifications" + query({ page, size, sort: "createdAt,desc" }),
      ),
    recent: () => request("/api/notifications/recent"),
    count: () => request("/api/notifications/unread-count"),
    read: (id) => request(`/api/notifications/${id}/read`, { method: "PATCH" }),
    readAll: () => request("/api/notifications/read-all", { method: "PATCH" }),
  },
  reports: { dashboard: () => request("/api/reports/dashboard") },
};
