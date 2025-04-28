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
    const li = document.createElement("div"); // div로 변경
    li.className = "diary-item"; // 클래스 추가
    li.innerHTML = `
      <div class="diary-left">
        <strong>${d.title}</strong><br>
        ${d.content.replace(/\n/g, '<br>')}
        <br><small>🕒 ${new Date(d.createdAt).toLocaleString()}</small>
      </div>
      <div class="diary-right">
        <button onclick="openEditModal(${d.id}, \`${d.title}\`, \`${d.content}\`)" class="edit-btn">✏️ 수정</button>
        <button onclick="deleteDiary(${d.id})" class="delete-btn">🗑️ 삭제</button>
      </div>
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
  // 모달 보이게
  document.getElementById("edit-modal-backdrop").style.display = "block";

  // 기존 제목/내용 채워넣기
  document.getElementById("edit-title").value = title;
  document.getElementById("edit-content").value = content;

  // 저장 버튼 클릭시
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
      loadDiaries(); // 목록 다시 불러오기
    } else if (res.status === 401) {
      showLoginModal();
    }
  };

  // 취소 버튼 클릭시
  const cancelBtn = document.getElementById("cancel-edit-btn");
  cancelBtn.onclick = () => {
    document.getElementById("edit-modal-backdrop").style.display = "none";
  };
}

// ✅ HTML에서 접근할 수 있게 전역 등록
window.openEditModal = openEditModal;
window.deleteDiary = deleteDiary;