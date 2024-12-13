document.addEventListener('DOMContentLoaded', () => {
    const chatRoomList = document.getElementById('chatRoomList');
    const createRoomForm = document.getElementById('createRoomForm');
    const roomNameInput = document.getElementById('roomName');
    const maxUsersInput = document.getElementById('maxUsers');
    const hasPasswordCheckbox = document.getElementById('hasPassword');
    const roomPasswordInput = document.getElementById('roomPassword');

    // 비밀번호 사용 여부에 따라 입력 필드 활성화/비활성화
    hasPasswordCheckbox.addEventListener('change', () => {
        roomPasswordInput.disabled = !hasPasswordCheckbox.checked;
        if (!hasPasswordCheckbox.checked) {
            roomPasswordInput.value = '';
        }
    });

// 채팅방 목록 불러오기
    fetch('/api/chat/rooms')
        .then(response => response.json())
        .then(rooms => {
            rooms.forEach(room => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                <td>${room.title}</td>
                <td>${room.currentUsers}/${room.maxUsers}</td>
                <td>${room.hasPassword ? '🔒' : '-'}</td>
                <td><button class="enter-button" onclick="enterRoom(${room.id}, ${room.hasPassword})">입장</button></td>
            `;
                chatRoomList.querySelector('tbody').appendChild(tr);
            });
        });

    // 채팅방 생성
    createRoomForm.addEventListener('submit', event => {
        event.preventDefault();

        const roomData = {
            title: roomNameInput.value,
            maxUsers: parseInt(maxUsersInput.value, 10),
            hasPassword: hasPasswordCheckbox.checked,
            password: hasPasswordCheckbox.checked ? roomPasswordInput.value : null,
        };

        fetch('/api/chat/rooms', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(roomData)
        })
            .then(response => response.json())
            .then(newRoom => {
                alert(`채팅방 ${newRoom.title}이 생성되었습니다.`);
                window.location.reload();
            });
    });
});

// 채팅방 입장
function enterRoom(roomId, hasPassword) {
    if (hasPassword) {
        const password = prompt('비밀번호를 입력하세요');
        if (password) {
            window.location.href = `/chatroom.html?roomId=${roomId}&password=${password}`;
        }
    } else {
        window.location.href = `/chatroom.html?roomId=${roomId}`;
    }
}