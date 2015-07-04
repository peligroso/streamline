ws = new WebSocket("ws://localhost:8080/websocket");

ws.onmessage = function(e) {
    alert(e.data);
}


function send() {
    ws.send(document.getElementById('t').value);

}
