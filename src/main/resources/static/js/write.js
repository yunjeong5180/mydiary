// # write.html 전용 스크립트
import { redirectToLogin, checkAuthWithDelay, getLoginState } from '/js/auth.js';

document.addEventListener("DOMContentLoaded", async () => {
  const localLoginState = getLoginState();
  console.log("💡 로컬 스토리지 로그인 상태:", localLoginState);

  if (!localLoginState) {
    document.getElementById("modal-backdrop").style.display = "block";
    document.getElementById("modal-box").style.display = "block";
  }

  try {
    const res = await fetch("/users/me", {
      credentials: "include"
    });

    if (!res.ok) {
      document.getElementById("modal-backdrop").style.display = "block";
      document.getElementById("modal-box").style.display = "block";
      console.log("⚠️ 인증 실패, 모달 표시");
    } else {
      console.log("✅ 인증 성공, 정상 진행");
    }
  } catch (error) {
    console.error("인증 확인 오류:", error);
    document.getElementById("modal-backdrop").style.display = "block";
    document.getElementById("modal-box").style.display = "block";
  }
});

const form = document.getElementById("diary-form");
const title = document.getElementById("title");
const text = document.getElementById("content");
const image = document.getElementById("image");

form.addEventListener("submit", async e => {
  e.preventDefault();

  const formData = new FormData();
  formData.append("title", title.value);
  formData.append("content", text.value);
  if (image.files.length > 0) {
    formData.append("image", image.files[0]);
  }

  try {
    const res = await fetch("/diaries", {
      method: "POST",
      credentials: "include",
      body: formData
    });

    if (res.ok) {
      alert("일기가 저장되었습니다!");
      // ✅ 일기 저장 성공 시 바로 목록 페이지로 이동
      window.location.href = "/list.html";
    } else if (res.status === 401) {
      // ✅ 로그인 필요 모달 표시
      document.getElementById("modal-backdrop").style.display = "block";
      document.getElementById("modal-box").style.display = "block";
    } else {
      alert("저장 실패: " + await res.text());
    }
  } catch (error) {
    console.error("일기 저장 오류:", error);
    alert("네트워크 오류가 발생했습니다. 다시 시도해주세요.");
  }
});

document.getElementById("modal-ok").onclick = () => {
  const currentPath = location.pathname;
  location.href = `/login.html?redirect=${encodeURIComponent(currentPath.replace(/^\//, ''))}`;
};
