#!/bin/bash

echo "🔍 ChatNet HTTP Server Status Check"
echo "===================================="

echo "📊 Checking if HTTP server is running on port 8081..."

# Check if port 8081 is in use
if lsof -i :8081 >/dev/null 2>&1; then
    echo "✅ HTTP Server is RUNNING on port 8081"
    echo "🌐 You can access your enhanced frontend at: http://localhost:8081"
    echo ""
    echo "👁️ Active processes on port 8081:"
    lsof -i :8081
    echo ""
    echo "🧪 Testing HTTP response..."
    curl -s -I http://localhost:8081 | head -3
else
    echo "❌ NO HTTP server running on port 8081"
    echo ""
    echo "🚀 To start the HTTP server, run these commands:"
    echo "   cd /Users/dumidu/Documents/GitHub/ChatNet/javafx-bridge"
    echo "   javac -d out src/main/java/com/chatnet/bridge/HTTPServer.java src/main/java/com/chatnet/bridge/HTTPServerTest.java"
    echo "   java -cp out com.chatnet.bridge.HTTPServerTest"
    echo ""
    echo "📱 Alternative: Open your enhanced frontend directly:"
    echo "   file:///Users/dumidu/Documents/GitHub/ChatNet/frontend/index.html"
fi

echo ""
echo "🔧 Quick server start (run this in a new terminal):"
echo "cd /Users/dumidu/Documents/GitHub/ChatNet/javafx-bridge && java -cp out com.chatnet.bridge.HTTPServerTest"
