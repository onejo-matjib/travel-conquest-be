document.addEventListener('DOMContentLoaded', () => {
    const chatMessages = document.getElementById('chatMessages');
    const chatForm = document.getElementById('chatForm');
    const messageInput = document.getElementById('messageInput');
    const exitButton = document.getElementById('exitButton');

    const roomId = new URLSearchParams(window.location.search).get('roomId');
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    // 이미 연결되었는지 확인하기 위한 변수
    let isConnected = false;

    stompClient.connect({}, () => {
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
            if (chatMessage.nickname === 'User') { // 'User'는 실제 사용자 닉네임으로 변경
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
                nickname: 'User', // 실제 사용자 닉네임으로 변경
                message: message
            }));

            messageInput.value = ''; // 메시지 전송 후 입력 필드 초기화
        });

        // 채팅방 퇴장
        exitButton.addEventListener('click', () => {
            stompClient.send(`/pub/exit/${roomId}`, {}, JSON.stringify({ userId: 1 }));
            window.location.href = '/index.html';
        });
    });
});