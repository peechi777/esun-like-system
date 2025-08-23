async function okOrThrow(res) {
  if (res.ok) return res;
  let msg = `HTTP ${res.status} ${res.statusText}`;
  try {
    const text = await res.text();
    if (text) {
      try {
        const j = JSON.parse(text);
        msg = j?.message || JSON.stringify(j);
      } catch {
        msg = text;
      }
    }
  } catch {}
  throw new Error(msg);
}

async function jsonOrEmpty(res) {
  if (res.status === 204) return {};
  const text = await res.text();
  if (!text) return {};
  try { return JSON.parse(text); } catch { return {}; }
}

const LIKE_BASE = "http://localhost:8080/api/likes";

export async function addOrMergeLike(payload) {
  const res = await fetch(LIKE_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  await okOrThrow(res);
  return jsonOrEmpty(res);
}

export async function updateLikeBySn(sn, payload) {
  const res = await fetch(`${LIKE_BASE}/${sn}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  await okOrThrow(res);
  return jsonOrEmpty(res);
}

export async function deleteLikeBySn(sn) {
  const res = await fetch(`${LIKE_BASE}/${sn}`, { method: "DELETE" });
  await okOrThrow(res);
  return { ok: true };
}

export async function listLikesByUser(userId) {
  const res = await fetch(`${LIKE_BASE}/user/${encodeURIComponent(userId)}`);
  await okOrThrow(res);
  return jsonOrEmpty(res);
}

const PRODUCT_BASE = "http://localhost:8080/api/products";

export async function createProduct(payload) {
  const res = await fetch(PRODUCT_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  await okOrThrow(res);
  return jsonOrEmpty(res); 
}

export async function listProducts() {
  const res = await fetch(PRODUCT_BASE);
  await okOrThrow(res);
  return jsonOrEmpty(res);
}

export async function deleteProduct(productNo) {
  const res = await fetch(`${PRODUCT_BASE}/${productNo}`, { method: "DELETE" });
  if (res.status === 204) return { ok: true };

  let msg = `HTTP ${res.status} ${res.statusText}`;
  try {
    const text = await res.text();
    if (text) {
      try {
        const j = JSON.parse(text);
        msg = j?.message || JSON.stringify(j);
      } catch {
        msg = text;
      }
    }
  } catch {}
  throw new Error(msg);
}

const USER_BASE = "http://localhost:8080/api/users";

export async function saveUser(payload) {
  const res = await fetch(USER_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  await okOrThrow(res);
  return jsonOrEmpty(res);
}
