import { checkLoginAndHandleUI, logout } from './auth.js';

document.addEventListener("DOMContentLoaded", async () => {
  const isLoggedIn = await checkLoginAndHandleUI(loadDiaries);
  if (!isLoggedIn) return;

  document.getElementById("logoutBtn").onclick = logout;
  document.getElementById("sort").addEventListener("change", loadDiaries);
});

const diaryList = document.getElementById("diary-list");

async function loadDiaries() {
  const sort = document.getElementById("sort").value;
  const res = await fetch(`/diaries?sort=${sort}`, {
    credentials: "include"
  });

  if (res.status === 401) {
    showLoginModal();
    return;
  }

  const data = await res.json();
  diaryList.innerHTML = "";

  data.forEach(d => {
    const li = document.createElement("li");
    li.innerHTML = `
      <strong>${d.title}</strong><br>
      ${d.content.replace(/\n/g,'<br>')}
      <br><small>🕒 ${new Date(d.createdAt).toLocaleString()}</small>
      <button onclick="deleteDiary(${d.id})" style="margin-left:10px;">🗑️ 삭제</button>
    `;
    diaryList.appendChild(li);
  });
}

async function deleteDiary(id) {
  const res = await fetch(`/diaries/${id}`, {
    method: "DELETE",
    credentials: "include"
  });

  if (res.ok) {
    loadDiaries();
  } else if (res.status === 401) {
    showLoginModal();
  }
}

function showLoginModal() {
  document.getElementById("modal-backdrop").style.display = "block";
  document.getElementById("modal-box").style.display = "block";
  document.getElementById("modal-ok").onclick = () => {
    location.href = "/login.html";
  };
}
