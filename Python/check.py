import websocket

def get_websocket_parameters(url):
    try:
        # Web soketine bağlan
        ws = websocket.create_connection(url)
        
        # Parametreleri al
        parameters = ws.recv()
        
        # Parametreleri ekrana yazdır
        print("Web soketi parametreleri:")
        print(parameters)
        
        # Web soket bağlantısını kapat
        ws.close()
    except websocket.WebSocketException as e:
        print(f"Hata oluştu: {str(e)}")

# Web soketin URL'sini belirleyin
url = "ws://10.42.0.1/xip/"  # Örnek bir URL, kendi URL'nizle değiştirin

# Web soketi parametrelerini al
get_websocket_parameters(url)