#!/bin/bash

echo "🚀 Starting ChatNet Web Frontend..."
echo "====================================="

# Create necessary directories
mkdir -p out/com/chatnet/bridge
mkdir -p filestorage

# Ensure we have a sample file
if [ ! -f "filestorage/welcome.txt" ]; then
    echo "Creating sample welcome file..."
    cat > filestorage/welcome.txt << EOF
Welcome to ChatNet File Storage!

This is a sample file to demonstrate the file sharing functionality.

Features:
- File upload and download via REST API
- Web-based file browser
- Integration with the chat system

Created: $(date)
Component: Member 5 - Web Frontend + WebSocket Bridge & REST API
EOF
fi

# Copy the existing compiled class if available
if [ -f "src/main/java/com/chatnet/bridge/HTTPServer.class" ]; then
    cp src/main/java/com/chatnet/bridge/HTTPServer.class out/com/chatnet/bridge/
fi

# Try to compile the HTTP server components
echo "Compiling HTTP Server components..."
javac -d out src/main/java/com/chatnet/bridge/HTTPServer.java 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ HTTPServer compiled successfully"
    
    # Try to compile the test runner
    javac -d out -cp out src/main/java/com/chatnet/bridge/HTTPServerTest.java 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "✓ HTTPServerTest compiled successfully"
        echo ""
        echo "🌐 Starting HTTP Server..."
        echo "📁 Web interface: http://localhost:8081"
        echo "📋 File API: http://localhost:8081/api/files"
        echo ""
        echo "Press Ctrl+C to stop the server"
        echo ""
        
        # Run the HTTP server
        cd out
        java com.chatnet.bridge.HTTPServerTest
    else
        echo "⚠ Could not compile HTTPServerTest, trying direct approach..."
        echo "You can manually access the HTTP functionality"
    fi
else
    echo "❌ HTTP Server compilation failed"
    echo "Try installing/updating Java or check for syntax errors"
fi
