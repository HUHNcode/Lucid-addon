# **Lucid Addon - ChatBot Module Documentation**

## **Overview**

The **ChatBot** module in the **Lucid Addon** automatically detects and responds to chat messages based on configurable settings. It extracts relevant messages using **regular expressions (regex)**, filters them according to **needed and forbidden keywords**, and searches for **predefined item triggers** to generate appropriate replies.

This module is highly customizable, allowing users to fine-tune message detection, response timing, and keyword filtering.

---

## **Features**

-   **Regex-based message extraction**: Captures player names and messages using a customizable regex pattern.
-   **Automated replies**: Sends predefined responses based on detected items.
-   **Configurable delay**: Controls the time before responding to messages.
-   **Keyword filtering**: Messages can be required to contain certain words while avoiding forbidden ones.
-   **Smart Mode**: Dynamically detects keywords within specific sections of a message.
-   **Notifications**: Plays a sound and displays a toast notification when a matching message is found.

---

## **Settings**

The module provides several settings that allow users to adjust how messages are detected and processed.

### **1. Message Regex**

-   **Description**:  
    Defines the regex pattern used to extract the player's name and the message content.  
    The keyword **`PLAYER`** acts as a placeholder for the sender's username.

-   **Example Usage**:  
    If the chat message follows this format:
    ```
    [Server] Steve › I want to buy diamonds
    ```
    Using the regex `"\\[Server\\] PLAYER › "` will extract:
    -   **Player**: `Steve`
    -   **Message**: `I want to buy diamonds`

---

### **2. Message Delay**

-   **Default Value**: `1.5`
-   **Description**:  
    Defines how many seconds the bot waits before sending a reply.

---

### **3. Needed Keywords**

-   **Description**:  
    Messages **must** contain at least one of these keywords to be processed.

-   **Example**:  
    If `neededKeywords = ["buy"]`, the message:
    ```
    Does anyone sell diamonds?
    ```
    will be ignored, but:
    ```
    I want to buy diamonds.
    ```
    will be processed.

---

### **4. Forbidden Keywords**

-   **Description**:  
    Messages containing **any** of these words will be ignored.

-   **Example**:  
    If `forbiddenKeywords = ["sell"]`, the message:
    ```
    I want to sell diamonds.
    ```
    will be ignored.

---

### **5. Smart Keyword Detection**

-   **Description**:

    -   When enabled, the bot **only searches for items after a needed keyword** and **ignores text after a forbidden keyword**.
    -   When disabled, the bot checks the entire message.

-   **Example**:  
    With `neededKeywords = ["buy"]` and `forbiddenKeywords = ["not"]`:
    ```
    I want to buy diamonds but not obsidian.
    ```
    -   **Smart Mode ON** → Only detects **"diamonds"**.
    -   **Smart Mode OFF** → Detects both **"diamonds"** and **"obsidian"**.

---

### **6. Trigger and Outputs (Item Data)**

-   **Description**:  
    Defines **item triggers** and their corresponding **responses**.

-   **Format**:

    ```
    "trigger1,trigger2;response"
    ```

    -   **Triggers**: Words that trigger the bot’s response.
    -   **Response**: Message sent when a trigger is found.

-   **Example**:
    ```
    "iron,iron block;20c/block stack"
    ```
    -   If someone says, "I want to buy iron," the bot replies:
        ```
        /msg <player> iron for 20c/block stack
        ```

---

## **How It Works**

### **1. Message Detection**

-   When a chat message is received, the bot applies the **regex pattern** to extract:
    -   The **player’s name**.
    -   The **message content**.

---

### **2. Filtering Messages**

The bot **ignores** messages that:

1. **Do not contain any needed keywords** (if configured).
2. **Contain a forbidden keyword** (if configured).

---

### **3. Item Detection**

If the message is valid, the bot:

-   Checks for **matching item triggers** in the text.
-   Uses **Smart Mode** (if enabled) to find keywords only in relevant parts of the message.

---

### **4. Sending a Response**

If an item is found:

1. The bot **displays a toast notification** and **plays a sound**.
2. After the **configured delay**, it sends a message to the player using:
    ```
    /msg <player> <item> for <price>
    ```

---

## **Code Breakdown (for devs)**

### **1. `parseMessage(String message)`**

Extracts the **player name** and **message content** using the regex pattern.

### **2. `extractMessageInfo(String message)`**

-   Calls `parseMessage()`.
-   Checks if the message contains the needed keywords.
-   Searches for a matching item.

### **3. `findItemSmart(String message)`**

-   Finds the first **needed keyword**.
-   Searches for an item **only after** this keyword.
-   Ignores text **after a forbidden keyword**.

### **4. `containsForbiddenKeyword(String message)`**

Checks if any forbidden word is present in the message.

### **5. `findMatchingItem(String text)`**

Searches for item triggers and returns the corresponding response.

---

## **Example Use Cases**

### **Example 1: Simple Message Matching**

#### **Configuration**

-   `neededKeywords = ["buy"]`
-   `forbiddenKeywords = ["sell"]`
-   `itemData = ["diamonds,Dias;30c/stack"]`

#### **Incoming Chat Message**

```
Alex › I want to buy diamonds
```

#### **Bot Response**

```
/msg Alex diamonds for 30c/stack
```

---

### **Example 2: Smart Mode in Action**

#### **Configuration**

-   `neededKeywords = ["buy"]`
-   `forbiddenKeywords = ["not"]`
-   `itemData = ["obsidian,obi;15c/stack"]`
-   **Smart Mode ON**

#### **Incoming Chat Message**

```
Steve › I want to buy obsidian but not emeralds.
```

#### **Bot Response**

```
/msg Steve obsidian for 15c/stack
```

(_Emeralds are ignored because they appear after the forbidden keyword "not."_)
