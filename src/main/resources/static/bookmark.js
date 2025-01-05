document.addEventListener('DOMContentLoaded', () => {
  // DOM 요소 참조
  const bookmarkList = document.getElementById('logArea');
  const addBookmarkBtn = document.getElementById('addBookmarkBtn');
  const deleteBookmarkBtn = document.getElementById('deleteBookmarkBtn');
  const checkLocalStorageBtn = document.getElementById('checkLocalStorageBtn');
  const routeIdInput = document.getElementById('routeIdInput');
  const bookmarkIdInput = document.getElementById('bookmarkIdInput');

  // 로컬 스토리지에서 JWT 토큰 가져오기
  let token = localStorage.getItem('authToken');

  // 토큰 유효성 검사
  if (!token || !token.startsWith('Bearer ')) {
    alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
    window.location.href = 'http://localhost:8080/login.html'; // 로그인 경로로 리다이렉트
    return;
  }
  if (!token.startsWith('Bearer ')) {
    token = `Bearer ${token}`;
  }

  // API 호출을 위한 함수 모음
  const api = {
    // 서버에서 즐겨찾기 목록 가져오기
    getBookmarks: async () => {
      try {
        const response = await fetch('/api/bookmarks', {
          headers: {'Authorization': token},
        });
        return response.ok ? await response.json() : {content: []}; // 실패 시 빈 배열 반환
      } catch (error) {
        console.error('API 호출 에러:', error);
        return {content: []}; // 예외 처리
      }
    },
    // 서버에 즐겨찾기 추가
    addBookmark: async (routeId) => {
      try {
        const response = await fetch('/api/bookmarks', {
          method: 'POST',
          headers: {
            'Authorization': token,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({routeId}),
        });
        return response.ok; // 성공 여부 반환
      } catch (error) {
        console.error('즐겨찾기 추가 실패:', error);
        return false; // 실패 시 false 반환
      }
    },
    // 서버에서 즐겨찾기 삭제
    deleteBookmark: async (bookmarkId) => {
      try {
        const response = await fetch(`/api/bookmarks/${bookmarkId}`, {
          method: 'DELETE',
          headers: {'Authorization': token},
        });
        return response.ok; // 성공 여부 반환
      } catch (error) {
        console.error('즐겨찾기 삭제 실패:', error);
        return false; // 실패 시 false 반환
      }
    },
  };

  // 로컬 스토리지 갱신 함수
  const updateLocalStorage = async () => {
    const response = await api.getBookmarks(); // 서버에서 최신 즐겨찾기 목록 가져오기
    const bookmarks = response.content || []; // 데이터가 없으면 빈 배열

    if (bookmarks.length > 0) {
      // 로컬 스토리지에 저장
      localStorage.setItem('bookmarks', JSON.stringify(bookmarks));
    } else {
      // 즐겨찾기가 없으면 로컬 스토리지에서 제거
      localStorage.removeItem('bookmarks');
    }
  };

  // 즐겨찾기 추가 버튼 이벤트 리스너
  addBookmarkBtn.addEventListener('click', async () => {
    const routeId = routeIdInput.value.trim(); // 사용자 입력 값 가져오기
    if (!routeId) {
      alert('추가할 경로 ID를 입력하세요.');
      return;
    }
    const success = await api.addBookmark(routeId); // 즐겨찾기 추가 API 호출
    if (success) {
      alert('즐겨찾기가 추가되었습니다.');
      routeIdInput.value = ''; // 입력 필드 초기화
      await updateLocalStorage(); // 로컬 스토리지 갱신
    } else {
      alert('즐겨찾기 추가에 실패했습니다.');
    }
  });
  // 즐겨찾기 삭제 버튼 이벤트 리스너
  deleteBookmarkBtn.addEventListener('click', async () => {
    const bookmarkId = bookmarkIdInput.value.trim(); // 사용자 입력 값 가져오기
    if (!bookmarkId) {
      alert('삭제할 즐겨찾기 ID를 입력하세요.');
      return;
    }
    const success = await api.deleteBookmark(bookmarkId); // 즐겨찾기 삭제 API 호출
    if (success) {
      alert('즐겨찾기가 삭제되었습니다.');
      bookmarkIdInput.value = ''; // 입력 필드 초기화
      await updateLocalStorage(); // 로컬 스토리지 갱신
    } else {
      alert('즐겨찾기 삭제에 실패했습니다.');
    }
  });
  // 로컬 스토리지 즐겨찾기 조회 버튼 이벤트 리스너
  checkLocalStorageBtn.addEventListener('click', () => {
    const storedBookmarks = localStorage.getItem('bookmarks'); // 로컬 스토리지에서 데이터 가져오기
    if (storedBookmarks) {
      const bookmarks = JSON.parse(storedBookmarks);
      // 최신순 정렬 (createdAt 기준)
      bookmarks.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      bookmarkList.textContent = JSON.stringify(bookmarks, null, 2); // 정렬된 데이터 출력
    } else {
      bookmarkList.textContent = '로컬 스토리지에 저장된 즐겨찾기가 없습니다.';
    }
  });
});
