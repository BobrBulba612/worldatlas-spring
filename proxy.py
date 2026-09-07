#!/usr/bin/env python3
import socket, struct, threading, sys, time

SOCKS_HOST, SOCKS_PORT, PROXY_PORT = '127.0.0.1', 20170, 8081

def socks5_connect(host, port):
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.settimeout(60)  # Увеличили до 60 секунд
    s.connect((SOCKS_HOST, SOCKS_PORT))
    s.send(b'\x05\x01\x00')
    r = s.recv(2)
    if len(r) < 2 or r[1] != 0:
        s.close(); raise Exception('Handshake failed')
    addr = host.encode()
    s.send(b'\x05\x01\x00\x03' + bytes([len(addr)]) + addr + struct.pack('>H', port))
    r = s.recv(256)
    if len(r) < 2 or r[1] != 0:
        s.close(); raise Exception(f'Connect failed: {r[1]}')
    return s

def forward(src, dst):
    try:
        while True:
            d = src.recv(8192)
            if not d: break
            dst.sendall(d)
    except: pass
    finally:
        try: src.close()
        except: pass
        try: dst.close()
        except: pass

def handle(client):
    try:
        client.settimeout(120)  # Увеличили до 120 секунд
        data = b''
        while b'\r\n\r\n' not in data:
            c = client.recv(4096)
            if not c: client.close(); return
            data += c
        if not data.startswith(b'CONNECT'):
            client.close(); return
        line = data.split(b'\r\n')[0]
        parts = line.split(b' ')[1].split(b':')
        host, port = parts[0].decode(), int(parts[1])
        print(f'CONNECT {host}:{port}', flush=True)
        remote = socks5_connect(host, port)
        client.send(b'HTTP/1.1 200 Connection Established\r\n\r\n')
        threading.Thread(target=forward, args=(client, remote), daemon=True).start()
        threading.Thread(target=forward, args=(remote, client), daemon=True).start()
    except Exception as e:
        print(f'Error: {e}', flush=True)
        try: client.close()
        except: pass

srv = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
srv.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
srv.bind(('127.0.0.1', PROXY_PORT))
srv.listen(50)
print(f'✅ HTTP CONNECT proxy on 127.0.0.1:{PROXY_PORT} -> SOCKS5 {SOCKS_HOST}:{SOCKS_PORT}', flush=True)
while True:
    c, a = srv.accept()
    threading.Thread(target=handle, args=(c,), daemon=True).start()
