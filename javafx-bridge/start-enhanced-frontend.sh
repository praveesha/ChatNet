#!/bin/bash

echo "🚀 Starting ChatNet Enhanced Frontend Server..."
echo "🔧 Working directory: $(pwd)"

# Navigate to the project directory
cd "$(dirname "$0")"

echo "📁 Current directory: $(pwd)"
echo "🔍 Checking compiled classes..."

if [ ! -d "out/com/chatnet/bridge" ]; then
    echo "❌ Compiled classes not found. Compiling now..."
    mkdir -p out
    javac -d out src/main/java/com/chatnet/bridge/HTTPServer.java src/main/java/com/chatnet/bridge/HTTPServerTest.java
    if [ $? -ne 0 ]; then
        echo "❌ Compilation failed!"
        exit 1
    fi
    echo "✅ Compilation successful!"
fi

echo "🌐 Starting HTTP Server on port 8081..."
echo "📱 Your enhanced frontend will be available at: http://localhost:8081"
echo "💬 WebSocket server should be running on port 8082"
echo ""
echo "🎯 Features of your enhanced frontend:"
echo "   • Modern responsive design"
echo "   • Real-time chat with animations"
echo "   • File management with download"
echo "   • Connection status monitoring" 
echo "   • Message statistics"
echo "   • Emoji support"
echo "   • Toast notifications"
echo ""
echo "Press Ctrl+C to stop the server"
echo "----------------------------------------"

# Start the HTTP server
java -cp out com.chatnet.bridge.HTTPServerTest
