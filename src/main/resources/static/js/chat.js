document.addEventListener('DOMContentLoaded', () => {
    const chatMessages = document.getElementById('chatMessages');
    const chatForm = document.getElementById('chatForm');
    const messageInput = document.getElementById('messageInput');
    const exitButton = document.getElementById('exitButton');

    const roomId = new URLSearchParams(window.location.search).get('roomId');
    const token = localStorage.getItem('jwtToken'); // localStorage에서 JWT 토큰 가져오기

    // jwt-decode 라이브러리를 이용하여 토큰 디코딩
    const decodedToken = jwt_decode(token);
    const nickname = decodedToken.nickname; // JWT에서 닉네임 추출 (여기서 'nickname'은 JWT에 포함된 사용자 닉네임의 키)

    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    // 이미 연결되었는지 확인하기 위한 변수
    let isConnected = false;

    stompClient.connect(
        {
            Authorization: 'Bearer ' + token // WebSocket 연결 시 헤더에 JWT 토큰 포함
        },
        () => {
            // 이미 연결되었으면 구독하지 않음
            if (isConnected) return;
            isConnected = true;

            // 채팅방 구독
            stompClient.subscribe(`/sub/chat/${roomId}`, message => {
                const chatMessage = JSON.parse(message.body);

                // 메시지 DOM 요소 생성
                const messageElement = document.createElement('div');
                messageElement.classList.add('message');

                // 내 메시지와 다른 사람의 메시지 구분
                if (chatMessage.nickname === nickname) { // 실제 사용자 닉네임을 비교
                    messageElement.classList.add('message-right'); // 오른쪽 정렬
                } else {
                    messageElement.classList.add('message-left'); // 왼쪽 정렬
                }

                // 메시지 내용 설정
                messageElement.innerHTML = `<strong>${chatMessage.nickname}:</strong> ${chatMessage.message}`;
                chatMessages.appendChild(messageElement);

                // 새 메시지가 보이도록 스크롤 자동 이동
                chatMessages.scrollTop = chatMessages.scrollHeight;
            });

            // 메시지 전송
            chatForm.addEventListener('submit', event => {
                event.preventDefault();

                // 이미 메시지가 전송 중인지 확인하는 변수
                if (messageInput.value.trim() === '') return;  // 비어있는 메시지는 전송하지 않음
                const message = messageInput.value.trim();

                // 메시지 전송
                stompClient.send(`/pub/chat/${roomId}`, {}, JSON.stringify({
                    roomId: roomId,
                    nickname: nickname, // 실제 사용자 닉네임 사용
                    message: message
                }));

                messageInput.value = ''; // 메시지 전송 후 입력 필드 초기화
            });

            // 채팅방 퇴장
            exitButton.addEventListener('click', () => {
                stompClient.send(`/pub/exit/${roomId}`, {}, JSON.stringify({ userId: decodedToken.userId })); // 실제 사용자 ID 사용
                window.location.href = '/chat.html';
            });
        }
    );
});