const BASE = (import.meta.env.VITE_API_BASE || "") + "/api";

function authHeaders() {
  const token = localStorage.getItem("mandal-token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export async function uploadFile(file) {
  const formData = new FormData();
  formData.append("file", file);

  const res = await fetch(BASE + "/upload", {
    method: "POST",
    headers: {
      ...authHeaders(),
    },
    body: formData,
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || "Upload failed");
  }
  
  return res.json();
}

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...options.headers,
    },
    ...options,
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || "Request failed");
  }
  // Handle empty responses (204 No Content, etc.)
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// ─── Auth ─────────────────────────────────────────────────────────────────────
export function login(email, password) {
  return request("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
  });
}

export function register(name, email, password, inviteCode) {
  return request("/auth/register", {
    method: "POST",
    body: JSON.stringify({ name, email, password, inviteCode }),
  });
}

export function memberJoin(name, phone, inviteCode) {
  return request("/auth/member-join", {
    method: "POST",
    body: JSON.stringify({ name, phone, inviteCode }),
  });
}

export function loginWithPhone(phone, password) {
  return request("/auth/login-phone", {
    method: "POST",
    body: JSON.stringify({ phone, password }),
  });
}

export function setPasswordApi(password) {
  return request("/auth/set-password", {
    method: "POST",
    body: JSON.stringify({ password }),
  });
}

// ─── Mandal ───────────────────────────────────────────────────────────────────
export function createMandal(name) {
  return request("/auth/create-mandal", {
    method: "POST",
    body: JSON.stringify({ name }),
  });
}

export function joinMandal(inviteCode) {
  return request("/auth/join-mandal", {
    method: "POST",
    body: JSON.stringify({ inviteCode }),
  });
}

export function getMandal() {
  return request("/mandal");
}

export function renameMandal(mandalName) {
  return request("/mandal", {
    method: "PUT",
    body: JSON.stringify({ mandalName }),
  });
}

// ─── Dashboard ────────────────────────────────────────────────────────────────
export function getDashboard() {
  return request("/dashboard/summary");
}

// ─── Contributions ────────────────────────────────────────────────────────────
export function getContributions(params = {}) {
  const qs = new URLSearchParams(params).toString();
  return request("/contributions" + (qs ? "?" + qs : ""));
}

export function addContribution(data) {
  return request("/contributions", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function updateContribution(id, data) {
  return request("/contributions/" + id, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

// ─── Expenses ─────────────────────────────────────────────────────────────────
export function getExpenses(params = {}) {
  const qs = new URLSearchParams(params).toString();
  return request("/expenses" + (qs ? "?" + qs : ""));
}

export function addExpense(data) {
  return request("/expenses", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function updateExpense(id, data) {
  return request("/expenses/" + id, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

// ─── Notices ──────────────────────────────────────────────────────────────────
export function getNotices() {
  return request("/notices");
}

export function addNotice(data) {
  return request("/notices", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function deleteNotice(id) {
  return request("/notices/" + id, {
    method: "DELETE"
  });
}

export function getComments(noticeId) {
  return request(`/notices/${noticeId}/comments`);
}

export function addComment(noticeId, body) {
  return request(`/notices/${noticeId}/comments`, {
    method: "POST",
    body: JSON.stringify({ body }),
  });
}

export function deleteComment(noticeId, commentId) {
  return request(`/notices/${noticeId}/comments/${commentId}`, {
    method: "DELETE",
  });
}

export function toggleReaction(noticeId, reaction) {
  return request(`/notices/${noticeId}/react`, {
    method: "POST",
    body: JSON.stringify({ reaction }),
  });
}

// ─── Complaints ───────────────────────────────────────────────────────────────
export function addComplaint(message) {
  return request("/complaints", {
    method: "POST",
    body: JSON.stringify({ message }),
  });
}

export function getComplaints() {
  return request("/complaints");
}

export function resolveComplaint(id) {
  return request(`/complaints/${id}/resolve`, {
    method: "PUT",
  });
}

// ─── Users ────────────────────────────────────────────────────────────────────
export function getUsers() {
  return request("/users");
}

export function changeUserRole(userId, role) {
  return request(`/users/${userId}/role`, {
    method: "PUT",
    body: JSON.stringify({ role }),
  });
}

export function updateProfile(userId, data) {
  return request(`/users/${userId}/profile`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

// ─── Reports ──────────────────────────────────────────────────────────────────
export async function downloadReport(from, to) {
  const qs = new URLSearchParams({ from, to }).toString();
  const res = await fetch(BASE + "/reports/export?" + qs, {
    headers: { ...authHeaders() },
  });
  
  if (!res.ok) throw new Error("Report download failed");
  
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "mandal_report.xlsx";
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export function getMediaUrl(url) {
  if (!url) return "";
  if (url.startsWith("http")) return url;
  const host = import.meta.env.VITE_API_BASE || "";
  return url.startsWith("/api") ? host + url : BASE + url;
}

export async function downloadReceipt(urlPath) {
  const fullUrl = getMediaUrl(urlPath);
  const res = await fetch(fullUrl, {
    headers: { ...authHeaders() },
  });
  
  if (!res.ok) throw new Error("Receipt download failed");
  
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  const fileName = urlPath.split('/').pop() || "receipt.pdf";
  a.download = fileName;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

// ─── Room Tracker ─────────────────────────────────────────────────────────────
export function getRooms(params = {}) {
  const qs = new URLSearchParams(params).toString();
  return request("/rooms" + (qs ? "?" + qs : ""));
}

export function getRoomsSummary() {
  return request("/rooms/summary");
}

export function addRoom(data) {
  return request("/rooms", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function bulkAddRooms(data) {
  return request("/rooms/bulk", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function updateRoom(id, data) {
  return request("/rooms/" + id, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export function markRoomStatus(id, data) {
  return request("/rooms/" + id + "/status", {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export function deleteRoom(id) {
  return request("/rooms/" + id, {
    method: "DELETE",
  });
}

