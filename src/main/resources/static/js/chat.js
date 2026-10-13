(function () {
    const toggle = document.getElementById('chatToggle');
    const panel = document.getElementById('chatPanel');
    const messages = document.getElementById('chatMessages');
    const input = document.getElementById('chatInput');
    const send = document.getElementById('chatSend');

    if (!toggle) return;

    toggle.addEventListener('click', function () {
        panel.classList.toggle('open');
        if (panel.classList.contains('open')) {
            input.focus();
        }
    });

    function appendMessage(text, who) {
        const div = document.createElement('div');
        div.className = 'chat-msg ' + who;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
    }

    function getCsrfHeaders() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        const headers = { 'Content-Type': 'application/json' };
        if (tokenMeta && headerMeta) {
            headers[headerMeta.content] = tokenMeta.content;
        }
        return headers;
    }

    function sendMessage() {
        const text = input.value.trim();
        if (!text) return;
        appendMessage(text, 'user');
        input.value = '';

        fetch('/api/ai/chat', {
            method: 'POST',
            headers: getCsrfHeaders(),
            body: JSON.stringify({ message: text })
        })
            .then(function (res) { return res.json(); })
            .then(function (data) { appendMessage(data.reply, 'bot'); })
            .catch(function () {
                appendMessage("Sorry, I couldn't reach the assistant right now.", 'bot');
            });
    }

    send.addEventListener('click', sendMessage);
    input.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') sendMessage();
    });
})();
