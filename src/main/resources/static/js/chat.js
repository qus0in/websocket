// DOM이 완전히 로드된 후 스크립트 실행
document.addEventListener('DOMContentLoaded', () => {

    // DOM 요소 참조
    const messageForm = document.getElementById('message-form');
    const messageInput = document.getElementById('message-input');
    const sendButton = document.getElementById('send-button');
    const messagesContainer = document.getElementById('messages-container');
    let stompClient = null;

    // 스크롤을 항상 맨 아래로 이동시키는 헬퍼 함수
    const scrollToBottom = () => {
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    };

     /**
     * ISO 형식의 시간 문자열을 받아서 브라우저 시간대에 맞는 '오전/오후 hh:mm' 형식으로 변환합니다.
     * @param {string} isoString - 서버에서 받은 ZonedDateTime 문자열
     * @returns {string} - 포맷팅된 시간 문자열
     */
    const formatToLocalTime = (isoString) => {
        if (!isoString) return '';
        const date = new Date(isoString);
        return date.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    };

    // 메시지를 화면에 동적으로 추가하는 함수
    const displayMessage = (msg) => {
        const messageElement = document.createElement('div');
        messageElement.classList.add('message');

        const isMyMessage = msg.sender === currentUser;
        messageElement.classList.add(isMyMessage ? 'my-message' : 'other-message');

        // 상대방 메시지일 경우에만 보낸 사람 이름 표시
        if (!isMyMessage) {
            const senderElement = document.createElement('div');
            senderElement.classList.add('sender');
            senderElement.textContent = msg.sender;
            messageElement.appendChild(senderElement);
        }

        // 메시지 내용
        const contentElement = document.createElement('div');
        contentElement.classList.add('message-content');
        contentElement.textContent = msg.message;
        messageElement.appendChild(contentElement);

        // 타임스탬프 (JavaScript에서 포맷팅)
//        const timestampElement = document.createElement('span');
//        timestampElement.classList.add('timestamp');
//        const sentDate = new Date(msg.sentAt); // ISO 8601 형식의 날짜 문자열을 Date 객체로 변환
//        timestampElement.textContent = sentDate.toLocaleTimeString('ko-KR', {
//            hour: '2-digit',
//            minute: '2-digit',
//            hour12: true
//        });
        // 타임스탬프
        const timestampElement = document.createElement('span');
        timestampElement.classList.add('timestamp');
        // 포맷팅 함수 사용
        timestampElement.textContent = formatToLocalTime(msg.sentAt);
        messageElement.appendChild(timestampElement);

        messagesContainer.appendChild(messageElement);
        scrollToBottom();
    };

    /**
     * 페이지에 이미 렌더링된 모든 타임스탬프를 브라우저 시간대에 맞게 변환합니다.
     */
    const initializeTimestamps = () => {
        const timestampElements = document.querySelectorAll('.timestamp[data-timestamp]');
        timestampElements.forEach(el => {
            const utcTimestamp = el.getAttribute('data-timestamp');
            // 포맷팅 함수를 사용해 화면에 시간 표시
            el.textContent = formatToLocalTime(utcTimestamp);
        });
    };


    // WebSocket 연결 성공 시 콜백
    const onConnected = () => {
        console.log('✅ WebSocket에 성공적으로 연결되었습니다.');
        sendButton.disabled = false; // 연결 성공 시 전송 버튼 활성화

        // 해당 채팅방 토픽 구독 시작
        stompClient.subscribe(`/topic/chat/${roomId}`, (payload) => {
            const message = JSON.parse(payload.body);
            displayMessage(message);
        });
    };

    // WebSocket 연결 실패 시 콜백
    const onError = (error) => {
        console.error('❌ WebSocket 연결에 실패했습니다. 서버를 확인해주세요.', error);
        alert('채팅 서버에 연결할 수 없습니다. 페이지를 새로고침 해주세요.');
        sendButton.disabled = true;
    };

    // WebSocket 연결 함수
    const connect = () => {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);
        // STOMP 디버그 메시지를 끄려면 아래 주석을 해제하세요.
        // stompClient.debug = null;
        stompClient.connect({}, onConnected, onError);
    };

    // 폼 제출 이벤트 처리
    messageForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const messageContent = messageInput.value.trim();
        if (messageContent && stompClient) {
            const chatMessage = {
                content: messageContent
            };

            // 서버의 @MessageMapping 경로로 메시지 전송
            stompClient.send(`/app/chat/${roomId}`, {}, JSON.stringify(chatMessage));
            messageInput.value = '';
            messageInput.focus(); // 메시지 전송 후 다시 입력창에 포커스
        }
    });

    // --- 스크립트 실행 시작 ---
    initializeTimestamps(); // 페이지 로드 시 기존 메시지들의 시간 포맷팅
    scrollToBottom(); // 초기 메시지 로드 후 스크롤을 아래로
    connect(); // WebSocket 연결 시작
});