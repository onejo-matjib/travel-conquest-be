document.addEventListener('DOMContentLoaded', () => {
  const bookmarkList = document.getElementById('logArea');
  const addBookmarkBtn = document.getElementById('addBookmarkBtn');
  const deleteBookmarkBtn = document.getElementById('deleteBookmarkBtn');
  const loadAllBtn = document.getElementById('loadAllBtn');
  const checkLocalStorageBtn = document.getElementById('checkLocalStorageBtn');
  const routeIdInput = document.getElementById('routeIdInput');
  const bookmarkIdInput = document.getElementById('bookmarkIdInput');

  // 로컬 스토리지에서 JWT 토큰 가져오기
  let token = localStorage.getItem('authToken');

  // 토큰이 없거나 형식이 잘못된 경우 처리
  if (!token || !token.startsWith('Bearer ')) {
    alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
    window.location.href = 'http://localhost:8080/login.html'; // 올바른 로그인 경로로 수정
    return;
  }
  // "Bearer " 접두사가 없을 경우 추가
  if (!token.startsWith('Bearer ')) {
    token = `Bearer ${token}`;
  }

  // API 호출 함수 설정
  const api = {
    getBookmarks: async () => {
      try {
        const response = await fetch('/api/bookmarks', {
          headers: {
            'Authorization': token
          }
        });
        if (response.ok) {
          return await response.json();
        } else {
          console.error('즐겨찾기 가져오기 실패:', await response.text());
          return [];
        }
      } catch (error) {
        console.error('API 호출 에러:', error);
        return [];
      }
    },
    addBookmark: async (routeId) => {
      try {
        const response = await fetch('/api/bookmarks', {
          method: 'POST',
          headers: {
            'Authorization': token,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ routeId }),
        });
        return response.ok;
      } catch (error) {
        console.error('즐겨찾기 추가 실패:', error);
        return false;
      }
    },
    deleteBookmark: async (bookmarkId) => {
      try {
        const response = await fetch(`/api/bookmarks/${bookmarkId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': token,
          },
        });
        return response.ok;
      } catch (error) {
        console.error('즐겨찾기 삭제 실패:', error);
        return false;
      }
    }
  };

  // 즐겨찾기 추가 버튼
  addBookmarkBtn.addEventListener('click', async () => {
    const routeId = routeIdInput.value.trim();
    if (!routeId) {
      alert('추가할 경로 ID를 입력하세요.');
      return;
    }

    const success = await api.addBookmark(routeId);
    if (success) {
      alert('즐겨찾기가 추가되었습니다.');
      routeIdInput.value = '';
    } else {
      alert('즐겨찾기 추가에 실패했습니다.');
    }
  });

  // 즐겨찾기 삭제 버튼
  deleteBookmarkBtn.addEventListener('click', async () => {
    const bookmarkId = bookmarkIdInput.value.trim();
    if (!bookmarkId) {
      alert('삭제할 즐겨찾기 ID를 입력하세요.');
      return;
    }

    const success = await api.deleteBookmark(bookmarkId);
    if (success) {
      alert('즐겨찾기가 삭제되었습니다.');
      bookmarkIdInput.value = '';
    } else {
      alert('즐겨찾기 삭제에 실패했습니다.');
    }
  });

  // 모든 즐겨찾기 조회 및 동기화 버튼
  loadAllBtn.addEventListener('click', async () => {
    const response = await api.getBookmarks(); // 서버에서 응답 가져오기
    const bookmarks = response.content || []; // content 필드에서 즐겨찾기 데이터 추출

    if (bookmarks.length > 0) {
      // 서버에서 즐겨찾기 데이터가 있을 경우 로컬 스토리지에 저장
      localStorage.setItem('bookmarks', JSON.stringify(bookmarks));
      bookmarkList.textContent = '즐겨찾기가 로컬 스토리지에 동기화되었습니다.';
    } else {
      // 서버에서 즐겨찾기 데이터가 없을 경우 로컬 스토리지 초기화
      localStorage.removeItem('bookmarks'); // 로컬 스토리지 초기화
      bookmarkList.textContent = '현재 즐겨찾기가 없습니다. 즐겨찾기를 추가해주세요.';
    }
  });

  // 로컬 스토리지 즐겨찾기 확인 버튼
  checkLocalStorageBtn.addEventListener('click', () => {
    const storedBookmarks = localStorage.getItem('bookmarks');
    if (storedBookmarks) {
      bookmarkList.textContent = JSON.stringify(JSON.parse(storedBookmarks), null, 2);
    } else {
      bookmarkList.textContent = '로컬 스토리지에 저장된 즐겨찾기가 없습니다.';
    }
  });
});
