#!/usr/bin/env python3
"""
Simple HTTP Server for ChatNet Enhanced Frontend
Serves the enhanced HTML file and provides mock API responses
"""

import http.server
import socketserver
import json
import os
from urllib.parse import urlparse

class ChatNetHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        parsed_path = urlparse(self.path)
        
        # Serve the enhanced frontend
        if parsed_path.path == '/' or parsed_path.path == '/index.html':
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # Read the enhanced frontend file
            frontend_path = '../frontend/index.html'
            try:
                with open(frontend_path, 'r') as f:
                    content = f.read()
                self.wfile.write(content.encode('utf-8'))
            except FileNotFoundError:
                self.wfile.write(b"<h1>Enhanced Frontend Not Found</h1><p>Please check the file path</p>")
                
        # Mock API for file listing
        elif parsed_path.path == '/api/files':
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.send_header('Access-Control-Allow-Origin', '*')
            self.end_headers()
            
            # Mock file list
            files = []
            filestorage_path = './filestorage'
            if os.path.exists(filestorage_path):
                files = [f for f in os.listdir(filestorage_path) if os.path.isfile(os.path.join(filestorage_path, f))]
            
            response = json.dumps(files)
            self.wfile.write(response.encode('utf-8'))
            
        # Mock file download
        elif parsed_path.path.startswith('/api/files/'):
            filename = parsed_path.path.replace('/api/files/', '')
            file_path = os.path.join('./filestorage', filename)
            
            if os.path.exists(file_path):
                self.send_response(200)
                self.send_header('Content-type', 'application/octet-stream')
                self.send_header('Content-Disposition', f'attachment; filename="{filename}"')
                self.send_header('Access-Control-Allow-Origin', '*')
                self.end_headers()
                
                with open(file_path, 'rb') as f:
                    self.wfile.write(f.read())
            else:
                self.send_response(404)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(b'File not found')
        else:
            super().do_GET()

if __name__ == "__main__":
    PORT = 8081
    
    print("🚀 Starting ChatNet Enhanced Frontend Server")
    print(f"🌐 Server running on http://localhost:{PORT}")
    print("✨ Features:")
    print("   • Enhanced modern UI with animations")
    print("   • Real-time connection status")
    print("   • File management with downloads")
    print("   • Statistics and monitoring")
    print("   • Mobile-responsive design")
    print("\nPress Ctrl+C to stop")
    
    try:
        with socketserver.TCPServer(("", PORT), ChatNetHandler) as httpd:
            httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n🛑 Server stopped")
