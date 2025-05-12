import { checkLoginAndHandleUI, logout } from './auth.js';

document.addEventListener("DOMContentLoaded", async () => {
  const isLoggedIn = await checkLoginAndHandleUI(loadDiaries);
  if (!isLoggedIn) return;

  document.getElementById("logoutBtn").onclick = logout;
  document.getElementById("sort").addEventListener("change", loadDiaries);
});

const diaryList = document.getElementById("diary-list");

// ✅ 일기 목록 불러오기
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
    const li = document.createElement("div");
    li.className = "diary-item";

    let imageHtml = '';
    if (d.imagePath) {
      imageHtml = `<img src="/${d.imagePath}" alt="첨부 이미지">`;
    }

    li.innerHTML = `
      <div class="diary-title">${d.title}</div>
      <div class="diary-body">
        ${imageHtml}
        <div class="diary-text">${d.content.replace(/\n/g, '<br>')}</div>
      </div>
      <div class="diary-buttons">
        <button onclick="openEditModal(${d.id}, \`${d.title}\`, \`${d.content}\`)" class="edit-btn">✏️ 수정</button>
        <button onclick="deleteDiary(${d.id})" class="delete-btn">🗑️ 삭제</button>
      </div>
      <small>🕒 ${new Date(d.createdAt).toLocaleString()}</small>
    `;

    diaryList.appendChild(li);
  });
}

// ✅ 일기 삭제
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

// ✅ 로그인 필요 모달 띄우기
function showLoginModal() {
  document.getElementById("modal-backdrop").style.display = "block";
  document.getElementById("modal-box").style.display = "block";
  document.getElementById("modal-ok").onclick = () => {
    location.href = "/login.html";
  };
}

// ✅ ✏️ 수정 모달 열기
function openEditModal(id, title, content) {
  document.getElementById("edit-modal-backdrop").style.display = "block";
  document.getElementById("edit-title").value = title;
  document.getElementById("edit-content").value = content;

  const saveBtn = document.getElementById("save-edit-btn");
  saveBtn.onclick = async () => {
    const newTitle = document.getElementById("edit-title").value;
    const newContent = document.getElementById("edit-content").value;

    const res = await fetch(`/diaries/${id}`, {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json"
      },
      credentials: "include",
      body: JSON.stringify({
        title: newTitle,
        content: newContent
      })
    });

    if (res.ok) {
      document.getElementById("edit-modal-backdrop").style.display = "none";
      loadDiaries();
    } else if (res.status === 401) {
      showLoginModal();
    }
  };

  const cancelBtn = document.getElementById("cancel-edit-btn");
  cancelBtn.onclick = () => {
    document.getElementById("edit-modal-backdrop").style.display = "none";
  };
}

// ✅ 전역 등록
window.openEditModal = openEditModal;
window.deleteDiary = deleteDiary;


