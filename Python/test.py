import socket

def check_websocket(ip_address):
    try:
        # Web soketi oluştur
        sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        sock.settimeout(2)  # Zaman aşımını 2 saniye olarak ayarla
        
        # IP adresini ve portu bağlanmaya çalış
        result = sock.connect_ex((ip_address, 80))
        parameters = sock.recv()
        
        if result == 0:
            # Bağlantı başarılıysa, web soketi var demektir
            print(f"{ip_address} adresinde web soketi mevcut.")
        else:
            # Bağlantı başarısızsa, web soketi yok demektir
            print(f"{ip_address} adresinde web soketi mevcut değil.")
        
        # Soketi kapat
        sock.close()
    except socket.error as e:
        print(f"Hata oluştu: {str(e)}")

# IP adresini belirleyin
ip_address = '10.42.0.1'

# Web soketini kontrol et
check_websocket(ip_address)