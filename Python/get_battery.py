import requests
from bs4 import BeautifulSoup
import time

def get_element_value(url, element_id):
    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')
    element = soup.find(id = element_id)
    if element:
        value = element.get_text()
        print(f"{element_id} öğesinin değeri: {value}")
    else:
        print(f"{element_id} ID'sine sahip öğe bulunamadı.")

#url = "https://randomapi.com"
url = "http://10.42.0.1/xip/"
element_id = "shutdownTitle"

while True:
    # Elementin değerini al
    get_element_value(url, element_id)
    
    # 5 saniye bekleyin
    time.sleep(2)