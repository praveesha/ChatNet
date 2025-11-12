#!/bin/bash

echo "🔧 ChatNet HTTP Server Startup"
echo "==============================="

# Clean and create output directory
echo "🧹 Cleaning previous builds..."
rm -rf out
mkdir -p out

# Compile step by step
echo "🔨 Compiling HTTPServer..."
javac -d out src/main/java/com/chatnet/bridge/HTTPServer.java

if [ $? -ne 0 ]; then
    echo "❌ Failed to compile HTTPServer"
    exit 1
fi

echo "🔨 Compiling HTTPServerTest..."
javac -cp out -d out src/main/java/com/chatnet/bridge/HTTPServerTest.java

if [ $? -ne 0 ]; then
    echo "❌ Failed to compile HTTPServerTest"
    exit 1
fi

echo "✅ Compilation successful!"
echo "📁 Compiled classes:"
ls -la out/com/chatnet/bridge/

echo ""
echo "🚀 Starting HTTP Server on port 8081..."
echo "🌐 Your enhanced frontend will be available at: http://localhost:8081"
echo "📁 File API will be available at: http://localhost:8081/api/files"
echo ""
echo "Press Ctrl+C to stop the server"
echo "================================="

# Start the server
java -cp out com.chatnet.bridge.HTTPServerTest
