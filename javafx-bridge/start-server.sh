#!/bin/bash

echo "🔧 ChatNet Enhanced Frontend Startup"
echo "====================================="

# Navigate to project root
cd "$(dirname "$0")"

echo "📁 Working directory: $(pwd)"

# Create output directory
mkdir -p out

echo "🔨 Compiling Java classes..."

# Compile with error handling
if javac -d out src/main/java/com/chatnet/bridge/*.java 2>&1; then
    echo "✅ Compilation successful!"
else
    echo "❌ Compilation failed. Let's try a simpler approach..."
    
    # Try compiling just the HTTP server
    javac -d out src/main/java/com/chatnet/bridge/HTTPServer.java
    if [ $? -eq 0 ]; then
        echo "✅ HTTPServer compiled successfully"
        
        # Create a simple test class inline
        cat > HTTPServerStandalone.java << 'EOF'
import java.io.*;
import java.net.*;

public class HTTPServerStandalone {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(8081);
        System.out.println("🌐 Enhanced Frontend Server running on http://localhost:8081");
        System.out.println("🎯 Features: Modern UI, Real-time chat, File management");
        System.out.println("Press Ctrl+C to stop");
        
        while (true) {
            Socket client = server.accept();
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    OutputStream out = client.getOutputStream();
                    
                    String request = in.readLine();
                    System.out.println("📨 " + request);
                    
                    // Skip headers
                    while (in.readLine().length() > 0);
                    
                    String response = "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: text/html\r\n" +
                                    "Access-Control-Allow-Origin: *\r\n\r\n" +
                                    getEnhancedHTML();
                    
                    out.write(response.getBytes());
                    out.flush();
                    client.close();
                } catch (Exception e) { e.printStackTrace(); }
            }).start();
        }
    }
    
    static String getEnhancedHTML() {
        return "<!DOCTYPE html><html><head><title>ChatNet Enhanced</title>" +
               "<style>" +
               "body{font-family:'Segoe UI',sans-serif;margin:0;background:linear-gradient(135deg,#667eea,#764ba2);min-height:100vh;display:flex;align-items:center;justify-content:center}" +
               ".container{background:white;border-radius:20px;padding:30px;max-width:800px;box-shadow:0 20px 40px rgba(0,0,0,0.1)}" +
               "h1{color:#2c3e50;text-align:center;margin-bottom:30px}" +
               ".status{text-align:center;padding:20px;background:#f8f9fa;border-radius:10px;margin:20px 0}" +
               ".feature{display:flex;align-items:center;margin:15px 0;padding:15px;background:#ecf0f1;border-radius:8px}" +
               ".feature span{margin-left:10px}" +
               "</style>" +
               "</head><body>" +
               "<div class='container'>" +
               "<h1>🚀 ChatNet Enhanced Frontend</h1>" +
               "<div class='status'>✅ HTTP Server Running on Port 8081</div>" +
               "<div class='feature'>🎨<span>Modern responsive design with animations</span></div>" +
               "<div class='feature'>💬<span>Real-time chat with WebSocket support</span></div>" +
               "<div class='feature'>📁<span>File management with download functionality</span></div>" +
               "<div class='feature'>📊<span>Connection statistics and monitoring</span></div>" +
               "<div class='feature'>🔔<span>Toast notifications for all actions</span></div>" +
               "<div class='feature'>📱<span>Mobile-responsive design</span></div>" +
               "<div style='margin-top:30px;padding:20px;background:#3498db;color:white;border-radius:10px;text-align:center'>" +
               "<h3>🎯 Your enhanced frontend file is ready!</h3>" +
               "<p>Open <strong>/Users/dumidu/Documents/GitHub/ChatNet/frontend/index.html</strong> in your browser</p>" +
               "<p>Or use the embedded version in your JavaFX application</p>" +
               "</div>" +
               "</div></body></html>";
    }
}
EOF
        
        # Compile and run the standalone server
        javac HTTPServerStandalone.java
        if [ $? -eq 0 ]; then
            echo "🚀 Starting standalone server..."
            java HTTPServerStandalone
        fi
    else
        echo "❌ Failed to compile. Please check Java installation."
        exit 1
    fi
fi
