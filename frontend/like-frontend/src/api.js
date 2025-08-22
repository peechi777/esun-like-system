const BASE = "http://localhost:8080/api/likes";

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
