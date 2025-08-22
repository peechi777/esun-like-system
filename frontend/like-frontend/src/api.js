const BASE = "http://localhost:8080/api/likes";
const PRODUCT_BASE = "http://localhost:8080/api/products";

export async function addOrMergeLike(payload) {
  const res = await fetch(BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error((await res.text()) || "Request failed");
  return true;
}

export async function updateLikeBySn(sn, payload /* { account, orderName } */) {
  const res = await fetch(`${BASE}/${encodeURIComponent(sn)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error((await res.text()) || "Request failed");
  return true;
}

export async function deleteLikeBySn(sn) {
  const res = await fetch(`${BASE}/${encodeURIComponent(sn)}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error((await res.text()) || "Request failed");
  return true;
}

export async function listLikesByUser(userId) {
  const res = await fetch(`${BASE}/user/${encodeURIComponent(userId)}`, {
    headers: { "Accept": "application/json" },
  });
  if (!res.ok) throw new Error((await res.text()) || "Request failed");
  return res.json();
}

export async function createProduct(payload /* { productName, price, feeRate } */) {
  const res = await fetch(PRODUCT_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText}${txt ? " - " + txt : ""}`);
  }
  return res.json(); // -> { productNo: 123 }
}

export async function listProducts() {
  const res = await fetch(PRODUCT_BASE);
  if (!res.ok) {
    const txt = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status} ${res.statusText}${txt ? " - " + txt : ""}`);
  }
  return res.json(); // -> [{ productNo, productName, price, feeRate }, ...]
}
