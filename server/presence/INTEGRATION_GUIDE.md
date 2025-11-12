# PresenceService - Integration Guide for Team Members

## ✅ Module Status: TESTED & WORKING

**Date:** November 12, 2025  
**Developer:** Thamindu 224137E  
**Status:** All tests passed ✓ - Module works correctly  

---

## 📋 What This Module Does

The **PresenceService** automatically discovers and tracks all users online on the LAN using UDP multicast. It provides a simple API to get the list of currently online users.

**Key Features:**
- ✅ Automatic user discovery (no manual setup)
- ✅ Real-time presence tracking
- ✅ Handles user crashes and timeouts
- ✅ Thread-safe for concurrent access
- ✅ Pure Java (no external dependencies)

---

## 🔧 Integration Instructions

### Step 1: Copy Files to Your Module

Copy these 3 files to your project:

```
server/presence/
  ├── NetworkConfig.java       ⚠️ DO NOT MODIFY - Must be identical for all members
  ├── PresenceService.java    
  └── README.md               
```

**CRITICAL:** All team members MUST use the **same exact NetworkConfig.java** file. If values differ, users won't discover each other.

---

### Step 2: Basic Usage in Your Code

#### Import the Service
```java
import server.presence.PresenceService;
import java.util.Set;
import java.io.IOException;
```

#### Start the Service (at application startup)
```java
PresenceService presence = PresenceService.getInstance();

try {
    // Start with the current user's username
    presence.start(username);
    System.out.println("Presence service started");
} catch (IOException e) {
    System.err.println("Failed to start presence service: " + e.getMessage());
}
```

#### Get Online Users (anytime you need the list)
```java
Set<String> onlineUsers = presence.getOnlineUsers();

// Example: Display the list
System.out.println("Users online: " + onlineUsers.size());
for (String user : onlineUsers) {
    System.out.println("  - " + user);
}
```

#### Stop the Service (at application shutdown)
```java
presence.stop();
```

---

## 👥 Integration Examples by Member

### Member 1: Chat Client

**Use Case:** Display a buddy list of online users in your chat UI

```java
public class ChatClient {
    private PresenceService presence;
    
    public void start(String username) throws IOException {
        // Start presence service
        presence = PresenceService.getInstance();
        presence.start(username);
        
        // In your UI refresh loop or event handler
        updateBuddyList();
    }
    
    public void updateBuddyList() {
        Set<String> buddies = presence.getOnlineUsers();
        
        // Update your UI with the buddy list
        buddyListPanel.clear();
        for (String buddy : buddies) {
            buddyListPanel.addBuddy(buddy);
        }
    }
    
    public void shutdown() {
        presence.stop();
    }
}
```

**Integration Points:**
- Call `presence.start(username)` when user logs in
- Call `getOnlineUsers()` every few seconds to refresh buddy list
- Call `presence.stop()` when user logs out

---

### Member 2: File Transfer

**Use Case:** Show list of available recipients before sending a file

```java
public class FileTransferClient {
    private PresenceService presence;
    
    public void sendFile(String filePath) {
        presence = PresenceService.getInstance();
        
        // Get available recipients
        Set<String> availableUsers = presence.getOnlineUsers();
        
        if (availableUsers.isEmpty()) {
            System.out.println("No users online. Cannot send file.");
            return;
        }
        
        // Display recipient selection
        System.out.println("Select recipient:");
        int i = 1;
        for (String user : availableUsers) {
            System.out.println(i++ + ". " + user);
        }
        
        // Get user selection and send file
        // ... your file transfer logic here
    }
}
```

**Integration Points:**
- Call `getOnlineUsers()` when user clicks "Send File"
- Display the returned list as recipient options
- Only show users who are currently online

---

### Member 5: Web Dashboard

**Use Case:** REST API endpoint to return online users as JSON

```java
public class WebDashboard {
    private PresenceService presence;
    
    public void startServer(String username) throws IOException {
        presence = PresenceService.getInstance();
        presence.start(username);
        
        // Start your web server...
    }
    
    // Handler for GET /api/users
    public String handleGetUsers() {
        Set<String> users = presence.getOnlineUsers();
        
        // Convert to JSON manually (pure Java)
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"count\":").append(users.size()).append(",");
        json.append("\"users\":[");
        
        int i = 0;
        for (String user : users) {
            if (i++ > 0) json.append(",");
            json.append("\"").append(user).append("\"");
        }
        
        json.append("]}");
        
        return json.toString();
    }
}
```

**Example JSON Response:**
```json
{
  "count": 3,
  "users": ["Alice", "Bob", "Charlie"]
}
```

**Integration Points:**
- Start service when web server starts
- Create `/api/users` endpoint that calls `getOnlineUsers()`
- Return users as JSON array

---

## ⚙️ Configuration Details

### NetworkConfig Values (DO NOT CHANGE)

```java
Multicast Address: 230.0.0.0
Multicast Port: 4446
Heartbeat Interval: 5 seconds
User Timeout: 15 seconds
```

**Important:** These values are hardcoded in `NetworkConfig.java`. All team members must use the same values for the system to work.

---

## 🧪 Testing Your Integration

### Quick Test
1. Start your application with username "TestUser1"
2. Open another terminal and start your application again with "TestUser2"
3. Both should see each other in the online users list within 5 seconds

### Verification Checklist
- [ ] Users discover each other automatically
- [ ] Online user list updates in real-time
- [ ] When user exits gracefully, they disappear from lists immediately
- [ ] When user crashes (force quit), they disappear after ~15 seconds

---

## 🔥 Common Issues & Solutions

### Issue: Users not discovering each other

**Possible Causes & Solutions:**

1. **Different NetworkConfig values**
   - ✅ Solution: Ensure all team members use the EXACT same NetworkConfig.java file

2. **Firewall blocking UDP port 4446**
   - ✅ Solution: Add firewall exception for Java or UDP port 4446

3. **Not on same network**
   - ✅ Solution: Ensure all instances are on the same LAN/subnet

4. **Service not started**
   - ✅ Solution: Make sure you called `presence.start(username)` before trying to get users

### Issue: "Address already in use" error

**Cause:** Another instance of your application is already running

**Solution:** Stop the other instance, or test on different computers

### Issue: Users not timing out after crash

**Expected Behavior:** This is normal! Timeout takes 15-25 seconds by design:
- User timeout: 15 seconds
- Reaper check interval: 10 seconds
- Maximum total: ~25 seconds

---

## 📝 Important Notes

### Thread Safety
✅ The service is **fully thread-safe**. Multiple threads can call `getOnlineUsers()` simultaneously without any issues.

### Singleton Pattern
✅ Always use `PresenceService.getInstance()` to get the service. Never try to create a new instance.

### Service Lifecycle
```
Start once  → Use many times → Stop once
    ↓              ↓                ↓
start()    getOnlineUsers()     stop()
```

### Network Requirements
- All instances must be on the same LAN
- UDP port 4446 must be accessible
- Network must support multicast (most do)

---

## 💡 Best Practices

### 1. Start Early
Start the presence service as soon as your application starts:
```java
public static void main(String[] args) {
    PresenceService.getInstance().start(username);
    // ... rest of your application
}
```

### 2. Always Stop on Exit
Use a shutdown hook to ensure cleanup:
```java
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    PresenceService.getInstance().stop();
}));
```

### 3. Handle Exceptions
Always wrap `start()` in try-catch:
```java
try {
    presence.start(username);
} catch (IOException e) {
    System.err.println("Could not start presence service: " + e.getMessage());
    // Decide: Continue without presence or exit?
}
```

### 4. Refresh Periodically
For UI applications, refresh the user list every 3-5 seconds:
```java
Timer timer = new Timer();
timer.scheduleAtFixedRate(new TimerTask() {
    public void run() {
        updateUserList();
    }
}, 0, 3000); // Every 3 seconds
```

---

## 🚀 Quick Start Template

```java
import server.presence.PresenceService;
import java.io.IOException;
import java.util.Set;

public class MyApplication {
    private static PresenceService presence;
    
    public static void main(String[] args) {
        String username = args[0]; // Get username from command line
        
        // 1. Start presence service
        presence = PresenceService.getInstance();
        try {
            presence.start(username);
            System.out.println("✓ Presence service started");
        } catch (IOException e) {
            System.err.println("✗ Failed to start presence: " + e.getMessage());
            System.exit(1);
        }
        
        // 2. Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            presence.stop();
            System.out.println("✓ Presence service stopped");
        }));
        
        // 3. Your application logic here
        runApplication();
    }
    
    private static void runApplication() {
        // Example: Print online users every 5 seconds
        while (true) {
            Set<String> users = presence.getOnlineUsers();
            System.out.println("Online users: " + users);
            
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
```

---

## 📞 Need Help?

### For Questions or Issues:

1. Check the **README.md** for detailed technical documentation
2. Verify all team members use the same **NetworkConfig.java**
3. Test with the Quick Start Template above
4. Check firewall settings if discovery fails

---

## ✅ Integration Checklist

Before considering integration complete:

- [ ] Copied all 3 files (NetworkConfig, PresenceService, README)
- [ ] Using the same NetworkConfig.java as other team members
- [ ] Called `start()` at application startup
- [ ] Called `stop()` at application shutdown
- [ ] Tested with multiple instances (discovery works)
- [ ] Handling IOException from `start()`
- [ ] Getting online users with `getOnlineUsers()`
- [ ] Your application compiles without errors

---

**Module Created By:** Member 4  
**Status:** ✅ Tested and Working  
**Last Updated:** November 12, 2025

---

## Summary

The PresenceService is **ready for immediate integration**. Simply copy the files, call `start()` with a username, and use `getOnlineUsers()` whenever you need the list of online users. The service handles all the networking details automatically.

**All tests passed. Module works correctly. Integration is straightforward.**

Good luck with your integration! 🚀

