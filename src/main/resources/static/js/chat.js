document.addEventListener('DOMContentLoaded', () => {
    const chatMessages = document.getElementById('chatMessages');
    const chatForm = document.getElementById('chatForm');
    const messageInput = document.getElementById('messageInput');
    const exitButton = document.getElementById('exitButton');

    const roomId = new URLSearchParams(window.location.search).get('roomId');
    const token = localStorage.getItem('authToken');

    // JWT 토큰 디코딩 함수
    function decodeToken(token) {
        if (!token.startsWith('Bearer ')) {
            console.log('유효하지 않은 토큰');
            return null;
        }
        try {
            const jwtToken = token.replace('Bearer ', '');
            return jwt_decode(jwtToken);
        } catch (error) {
            console.error('토큰 디코딩 실패:', error);
            return null;
        }
    }

    const decodedToken = decodeToken(token);
    if (!decodedToken) return;

    const nickname = decodedToken.nickname;
    const userId = decodedToken.userId;

    // WebSocket 설정
    const socket = new SockJS('/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect(
        { Authorization: 'Bearer ' + token },
        () => {
            stompClient.subscribe(`/sub/chat/${roomId}`, message => {
                const chatMessage = JSON.parse(message.body);
                const isOwnMessage = chatMessage.nickname === nickname;
                const messageElement = createMessageElement(chatMessage, isOwnMessage);
                chatMessages.appendChild(messageElement);
                scrollToBottom(chatMessages);
            });

            chatForm.addEventListener('submit', event => {
                event.preventDefault();
                if (messageInput.value.trim() === '') return;
                const message = messageInput.value.trim();
                stompClient.send(`/pub/chat/${roomId}`, {}, JSON.stringify({
                    roomId: roomId,
                    nickname: nickname,
                    message: message
                }));
                messageInput.value = '';
            });

            exitButton.addEventListener('click', () => {
                stompClient.send(`/pub/exit/${roomId}`, {}, JSON.stringify({ userId: userId }));
                window.location.href = '/chat.html';
            });
        }
    );

    // 채팅 메시지 DOM 요소 생성
    function createMessageElement(chatMessage, isOwnMessage) {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message', isOwnMessage ? 'message-right' : 'message-left');
        messageElement.innerHTML = `<strong>${chatMessage.nickname}:</strong> ${chatMessage.message}`;
        return messageElement;
    }

    // 채팅 메시지 영역 스크롤
    function scrollToBottom(element) {
        element.scrollTop = element.scrollHeight;
    }
});