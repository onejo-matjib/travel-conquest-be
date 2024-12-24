document.addEventListener('DOMContentLoaded', () => {
  const bookmarkList = document.getElementById('logArea');
  const addBookmarkBtn = document.getElementById('addBookmarkBtn');
  const deleteBookmarkBtn = document.getElementById('deleteBookmarkBtn');
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
      const updatedBookmarks = await api.getBookmarks(); // 서버에서 최신 목록 가져오기
      localStorage.setItem('bookmarks', JSON.stringify(updatedBookmarks.content || [])); // 로컬 스토리지 갱신
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

      // 서버에서 최신 즐겨찾기 데이터 가져오기
      const response = await api.getBookmarks();
      const bookmarks = response.content || [];

      if (bookmarks.length > 0) {
        // 로컬 스토리지 갱신
        localStorage.setItem('bookmarks', JSON.stringify(bookmarks));
      } else {
        // 즐겨 찾기 없을 시 로컬 스토리지 비우기
        localStorage.removeItem('bookmarks');
      }
    } else {
      alert('즐겨찾기 삭제에 실패했습니다.');
    }
  });

  // 로컬 스토리지 즐겨찾기 확인 버튼
  checkLocalStorageBtn.addEventListener('click', () => {
    const storedBookmarks = localStorage.getItem('bookmarks');
    if (storedBookmarks) {
      const bookmarks = JSON.parse(storedBookmarks);

      // createdAt 기준으로 최신순 정렬
      bookmarks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

      bookmarkList.textContent = JSON.stringify(bookmarks, null, 2);
    } else {
      bookmarkList.textContent = '로컬 스토리지에 저장된 즐겨찾기가 없습니다.';
    }
  });
});
