// # write.html 전용 스크립트

document.addEventListener("DOMContentLoaded", async () => {
  const res = await fetch("/users/me", {
    credentials: "include"
  });

  if (!res.ok) {
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
});

document.getElementById("modal-ok").onclick = () => {
  location.href = "/login.html";
};
