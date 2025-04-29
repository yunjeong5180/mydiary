// # write.html 전용 스크립트
import { redirectToLogin, checkAuthWithDelay, getLoginState } from '/js/auth.js';

document.addEventListener("DOMContentLoaded", async () => {
  // 로컬 스토리지 로그인 상태 확인
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

  // 🔥 FormData 객체로 데이터 묶기
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
      // 🔥 headers 설정 "절대 금지" (Content-Type을 FormData가 알아서 설정함)
    });

    if (res.ok) {
      alert("일기가 저장되었습니다!");
      title.value = "";
      text.value = "";
      image.value = "";
    } else if (res.status === 401) {
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
