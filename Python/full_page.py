import requests
from bs4 import BeautifulSoup

def print_all_elements(url):
    response = requests.get(url)
    soup = BeautifulSoup(response.text, 'html.parser')
    
    for element in soup.find_all():
        print(element)

url = "http://10.42.0.1/xip/"  # Örnek bir URL, kendi web sitenizi buraya yazın
print_all_elements(url)
