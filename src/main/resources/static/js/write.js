// # write.html 전용 스크립트
import { redirectToLogin, checkAuthWithDelay, getLoginState } from '/js/auth.js';

document.addEventListener("DOMContentLoaded", async () => {
  // 로컬 스토리지의 로그인 상태 먼저 확인
  const localLoginState = getLoginState();
  console.log("💡 로컬 스토리지 로그인 상태:", localLoginState);

  // 로그인되지 않은 상태로 보이면 UI 초기화
  if (!localLoginState) {
    document.getElementById("modal-backdrop").style.display = "block";
    document.getElementById("modal-box").style.display = "block";
  }

  // 지연 후 서버 인증 확인 (쿠키 설정 시간 확보)
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

form.addEventListener("submit", async e => {
  e.preventDefault();

  const diary = {
    title: title.value,
    content: text.value
  };

  try {
    const res = await fetch("/diaries", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      credentials: "include",
      body: JSON.stringify(diary)
    });

    if (res.ok) {
      alert("일기가 저장되었습니다!");
      title.value = "";
      text.value = "";
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
  // 현재 페이지 정보를 저장하여 로그인 후 돌아올 수 있도록 함
  const currentPath = location.pathname;
  location.href = `/login.html?redirect=${encodeURIComponent(currentPath.replace(/^\//, ''))}`;
};