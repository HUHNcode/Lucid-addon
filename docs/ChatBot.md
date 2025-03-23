# **Lucid Addon - ChatBot Module Documentation (Extended)**

## **Overview**

The **ChatBot** module in the **Lucid Addon** automatically detects and responds to chat messages based on configurable settings. It extracts relevant messages using **regular expressions (regex)**, filters them according to **needed and forbidden keywords**, and searches for **predefined item triggers** to generate appropriate replies.

This module is highly customizable, allowing users to fine-tune message detection, response timing, keyword filtering, and now even the customizable output message format.

---

## **Features**

-   **Regex-based message extraction**: Captures player names and messages using a customizable regex pattern.
-   **Automated replies**: Sends predefined responses based on detected items.
-   **Configurable delays**: Controls the time before responding to messages.
-   **Duplicate message delay**: Prevents the bot from sending the same response to the same player within a configurable time frame.
-   **Keyword filtering**: Messages can be required to contain certain words while avoiding forbidden ones.
-   **Smart Mode**: Dynamically detects keywords within specific sections of a message.
-   **Notifications**: Plays a sound and displays a toast notification when a matching message is found.
-   **Customizable output message format**: Users can now configure the reply message format with placeholders such as **`TRIGGER`** and **`OUTPUT`**.
-   **Customizable response command**: Users can now fully customize the response command, such as changing from `/msg PLAYER TRIGGER for OUTPUT` to any command format, including placeholders for **`PLAYER`**, **`TRIGGER`**, and **`OUTPUT`**.

---

## **Settings**

The module provides several settings that allow users to adjust how messages are detected and processed.

---

### **1. Message Regex**

-   **Description**:  
    Defines the regex pattern used to extract the player's name and the message content.  
    The keyword **`PLAYER`** acts as a placeholder for the sender's username.

-   **Example Usage**:  
    If the chat message follows this format:

    ```
    [Server] Steve › I want to buy diamonds
    ```

    Using the regex "\\[Server\\] PLAYER › " will extract:

    -   **Player**: `Steve`
    -   **Message**: `I want to buy diamonds`

---

### **2. Message Delay**

-   **Default Value**: `1.5`
-   **Description**:  
    Defines how many seconds the bot waits before sending a reply.

---

### **3. Duplicate Message Delay**

-   **Default Value**: `10` seconds (0 disables this feature)
-   **Range**: `0s - 5min`
-   **Description**:  
    Prevents the bot from sending the same response to the same player with the same message within a configurable time frame. If the delay has expired and the player sends the same message again, the bot will respond and reset the delay timer.

---

### **4. Needed Keywords**

-   **Description**:  
    Messages **must** contain at least one of these keywords to be processed.

---

### **5. Forbidden Keywords**

-   **Description**:  
    Messages containing **any** of these words will be ignored.

---

### **6. Smart Keyword Detection**

-   **Description**:
    -   When enabled, the bot **only searches for items after a needed keyword** and **ignores text after a forbidden keyword**.

---

### **7. Custom Output Message Format**

Users can fully customize the **Output Message**. For example, if you want the bot to respond with a different command such as `/message PLAYER TRIGGER for OUTPUT` instead of `/msg PLAYER TRIGGER for OUTPUT`, you can set the response format in the **Custom Output Message Format** field.

---

### **8. Trigger and Outputs (Item Data)**

-   **Description**:

    -   Defines **item triggers** and their corresponding **responses**.

-   **Format**:

    ```
    "trigger1,trigger2;response"
    ```

---

### **9. Config File**

-   **Description**:
    -   The module stores its configuration in a file called `.minecraft/meteor-client/ChatBot.config`.
    -   **Only** contains the settings for the **triggers/outputs** and is formatted exactly like in the settings with "trigger1,trigger2;response".

---

## **How It Works**

### **1. Message Detection**

-   When a chat message is received, the bot applies the **regex pattern** to extract:
    -   The **player’s name**.
    -   The **message content**.

### **2. Filtering Messages**

The bot **ignores** messages that:

1. **Do not contain any needed keywords** (if configured).
2. **Contain a forbidden keyword** (if configured).

### **3. Item Detection**

If the message is valid, the bot:

-   Checks for **matching item triggers** in the text.
-   Uses **Smart Mode** (if enabled) to find keywords only in relevant parts of the message.

### **4. Sending a Response**

If an item is found:

1. The bot **displays a toast notification** and **plays a sound**.
2. After the **configured delay**, it sends a message to the player using the **customizable output format** with **TRIGGER** and **OUTPUT** placeholders, and the **customizable response command**.

Additionally, the bot ensures that the same message is not sent to the same player again within the **Duplicate Message Delay** period.

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

### **6. `generateCustomResponse(String trigger, String output)`**

Generates a response based on the **custom output format** using the **TRIGGER** and **OUTPUT** placeholders, and allows for customizable response commands.

---

## **Example Use Cases**

### **Example 1: Simple Message Matching**

#### **Configuration**

-   `neededKeywords = ["buy"]`
-   `forbiddenKeywords = ["sell"]`
-   `itemData = ["diamonds,Dias;30c/stack"]`
-   `outputMessage = "TRIGGER for OUTPUT"`

#### **Incoming Chat Message**

```
Alex › I want to buy diamonds
```

#### **Bot Response**

```
/msg Alex diamonds for 30c/stack
```

---

### **Example 2: Custom Output Message**

#### **Configuration**

-   `neededKeywords = ["buy"]`
-   `forbiddenKeywords = ["not"]`
-   `itemData = ["obsidian,obi;15c/stack"]`
-   `outputMessage = "TRIGGER for OUTPUT"`
-   **Smart Mode ON**

#### **Incoming Chat Message**

```
Steve › I want to buy obsidian but not emeralds.
```

#### **Bot Response**

```
/msg Steve obsidian for 15c/stack
```

---

### **Example 3: Customizing the Message Format**

#### **Configuration**

-   `neededKeywords = ["buy"]`
-   `forbiddenKeywords = ["not"]`
-   `itemData = ["lapis,lapis block;25c/block stack"]`
-   `outputMessage = "You are buying TRIGGER for OUTPUT!"`

#### **Incoming Chat Message**

```
Steve › I want to buy lapis block.
```

#### **Bot Response**

```
/msg Steve You are buying lapis block for 25c/block stack!
```

---

### **Example 4: Customizing the Response Command**

#### **Configuration**

-   `neededKeywords = ["buy"]`
-   `forbiddenKeywords = ["not"]`
-   `itemData = ["iron,iron block;20c/block stack"]`
-   `outputMessage = "/message PLAYER TRIGGER for OUTPUT"`

#### **Incoming Chat Message**

```
Steve › I want to buy iron block.
```

#### **Bot Response**

```
/message Steve iron for 20c/block stack
```
