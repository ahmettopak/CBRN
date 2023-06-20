const WebSocket = require('ws');

// WebSocket sunucusunun URL'si
const wsUrl = 'ws://10.42.0.1/xip/api/device/stream';

// WebSocket bağlantısını oluştur
const ws = new WebSocket(wsUrl);

// WebSocket açıldığında gerçekleşecek olay
ws.on('open', () => {
    console.log('WebSocket bağlantısı başarıyla açıldı.');
});

ws.onmessage = function (event) {
    try {
        var msg = JSON.parse(event.data);
        switch (msg.type) {
            case "sensors":
                console.log(event.data);
                break;
            case "meas":
                break;
            case "status":
                break;
            case "shutdown":
                break;
            case "info":
                break;
            case "event":
                break;
            case "gaslibmeas":
                break;
            default:
                console.log(event.data);
                break;
        }
    } catch (ex) {
        console.log(ex);
        console.log("DADA: " + event.data);
    }
};

// WebSocket hata aldığında gerçekleşecek olay
ws.on('error', (error) => {
    console.error('WebSocket hatası:', error);
});

// WebSocket kapatıldığında gerçekleşecek olay
ws.on('close', () => {
    console.log('WebSocket bağlantısı kapatıldı.');
});
