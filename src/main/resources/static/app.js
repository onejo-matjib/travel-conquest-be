document.getElementById('localLoginForm').addEventListener('submit', async function(e) {
  e.preventDefault();
  const email = e.target.email.value.trim();
  const password = e.target.password.value.trim();
  const messageEl = document.getElementById('message');
  messageEl.textContent = '';

  try {
    const response = await fetch('/api/users/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });

    if (response.ok) {
      const token = response.headers.get('Authorization');
      messageEl.textContent = '로그인 성공!';
      alert('추가 정보가 저장되었습니다! 토큰이 발급되었습니다.');
      document.body.innerHTML += `
          <div>
            <h3>발급된 JWT 토큰</h3>
            <textarea style="width: 100%; height: 100px;" readonly>${token.replace('Bearer ', '')}</textarea>
          </div>
        `;
      messageEl.classList.remove('message');
      messageEl.classList.add('success-msg');
      // 필요하면 토큰을 localStorage에 저장:
      localStorage.setItem('authToken', token);
      // 이후 페이지 이동
      setTimeout(() => {
       window.location.href = '/chat.html'; // 로그인 성공 후 이동할 페이지
      }, 10000);
    } else {
      const errText = await response.text();
      messageEl.textContent = errText || '로그인 실패. 이메일 혹은 비밀번호를 확인해주세요.';
      messageEl.classList.remove('success-msg');
      messageEl.classList.add('message');
    }
  } catch (error) {
    messageEl.textContent = '로그인 요청 중 에러가 발생했습니다.';
    messageEl.classList.remove('success-msg');
    messageEl.classList.add('message');
  }
});
