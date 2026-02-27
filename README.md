# CS336 HW3 — Binary Message Protocol Client (Client.java)

This project implements the **client** side of a simple client/server system that communicates over a simulated stream using a **binary message protocol**.

The server (`Server.java`) writes binary messages to **stdout**, and the client (`Client.java`) reads those messages from **stdin**. A pipe is used to simulate the communication channel.

# 🧠 CS336 HW3 – Binary Protocol & Networking Notes

These notes summarize the core concepts reviewed while working on the Binary Protocol Client assignment.

## 🧮 Bits and Bytes

1 byte = 8 bits  
2⁸ = 256 possible values (0–255)

Example:

10000000 = 128  
01111111 = 127  

In an 8-bit byte:
- Leftmost bit (MSB) = 128’s place (2⁷)
- Rightmost bit (LSB) = 1’s place (2⁰)

---

## 🏷 MSB vs LSB

**MSB (Most Significant Bit)**  
- Leftmost bit  
- Largest value  
- In HW3: determines message type  

**LSB (Least Significant Bit)**  
- Rightmost bit  
- Smallest value  
- Used when reading numeric bit fields  

---

## 📦 Bit Fields

A bit field means individual bits inside a byte have separate meanings.

### Text Message Header

1xxxxxxx  

- MSB = 1 → text message  
- Remaining 7 bits = length of text message (1–127)  

### Numeric Message Header

0xxxxxxx  

- MSB = 0 → numeric message  
- Remaining 7 bits = 7 independent flags  

Each bit (read from least significant bit upward):
- 0 → expect a float (4 bytes)
- 1 → expect a long (8 bytes)

---

## 🔢 Hexadecimal Refresher

Hex is base-16.

Digits:
0 1 2 3 4 5 6 7 8 9 A B C D E F

Each hex digit = 4 bits.

Example:

0x39  

Decimal:
(3 × 16) + (9 × 1) = 48 + 9 = 57  

Binary:
3 = 0011  
9 = 1001  

0x39 = 00111001  

---

## 🔄 Endianness

Endianness describes byte order.

### Big-endian (Java default)
Most significant byte first.

### Little-endian
Least significant byte first.

### Weird-endian (HW3 float rule)

Server sends float bytes in this order:

2nd least significant  
least significant  
most significant  
2nd most significant  

Client must rearrange back to big-endian before decoding.

---

## 🧰 ByteBuffer Mental Model

### Number → Bytes

ByteBuffer.allocate(size).putFloat(value).array()

- allocate() → static factory method
- putFloat() → mutates buffer
- array() → returns underlying byte[]

Java stores values in big-endian by default.

---

### Bytes → Number

ByteBuffer.wrap(byteArray).getFloat()

- wrap() → creates buffer around existing bytes
- getFloat() → interprets bytes as float (big-endian)

For HW3:
1. Read bytes from stream
2. Reorder if needed
3. Wrap and decode

---

## 📡 HW3 Binary Protocol Structure

Each message:

[1-byte header][body]

### End-of-Transmission

0x80 (10000000)

- Only header
- No body
- Stop reading

### Text Message

1xxxxxxx

- MSB = 1
- Remaining 7 bits = length N
- Read N ASCII bytes immediately after

### Numeric Message

0xxxxxxx

- MSB = 0
- Remaining 7 bits = bit field
- For each of the 7 bits:
  - 0 → read 4 bytes (float)
  - 1 → read 8 bytes (long)

---

## 🔍 Client Processing Rules

- Read one byte at a time
- After each read, check for EOF (-1)
- Process data immediately
- Do NOT store entire input stream
- Print text characters as received
- Print numbers as soon as decoded
- Keep track of total bytes received

If unexpected EOF occurs:
- Print error
- Print total bytes received
- Exit gracefully

---

## 🧩 Core Concept

Binary protocols rely on:

- Metadata first (header)
- Bit-level interpretation
- Correct byte order handling
- Stream-based processing

This assignment simulates how real network protocols (like TCP) interpret structured binary data.



#Addition Info TCP & UDP

# 📡 TCP & Binary Protocol Notes

These notes summarize networking concepts reviewed while working on the CS336 Binary Protocol Client assignment.

---

## 🧱 Networking Stack Context

Application-layer protocols like HTTP run on top of TCP.


- **HTTP**: Application protocol (200 OK, 404 Not Found, etc.)
- **TCP**: Reliable byte-stream transport
- **IP**: Routes packets between machines

---

## 🔌 Ports

A port identifies which application on a machine should receive data.

- **IP address** → identifies the machine
- **Port number** → identifies the application on that machine

Example:
- Destination port `443` → HTTPS
- Destination port `80` → HTTP

A TCP connection is uniquely identified by:

(source IP, source port, destination IP, destination port)

---

## 🔢 TCP Sequence Numbers

TCP treats communication as a **stream of bytes**.

- Each byte is numbered.
- Sequence numbers track where each byte belongs.

### Rule:

If a segment starts at:

SEQ = S

and carries:

N bytes

Then it contains bytes:

S through S + N − 1

---

## ✅ TCP Acknowledgment Numbers

ACK tells the sender:

> "I have received everything up to byte X−1. I expect byte X next."

If the receiver sends:

ACK = 1006

It means:
- Bytes 0–1005 were received successfully.
- The next expected byte is 1006.

---

## 🤝 3-Way Handshake (Connection Setup)

Each side chooses a random **Initial Sequence Number (ISN)**.

Example:

### 1️⃣ Client → Server (SYN)

SEQ = 1000

### 2️⃣ Server → Client (SYN-ACK)

SEQ = 5000  
ACK = 1001

### 3️⃣ Client → Server (ACK)

SEQ = 1001  
ACK = 5001

After this:
- Client’s first data byte starts at 1001
- Server’s first data byte starts at 5001

---

## TCP Flags (Hex + Meaning)

TCP control flags are **single-bit fields** inside the TCP header.
A TCP segment can combine multiple flags by **bitwise OR / addition** of their hex values.

### Common Flags Table

| Flag | Hex | Binary (8-bit) | Meaning (What it indicates) |
|------|-----|-----------------|-----------------------------|
| FIN  | `0x01` | `00000001` | **Finish**: sender wants to close the connection cleanly |
| SYN  | `0x02` | `00000010` | **Synchronize**: used to start a connection and exchange initial sequence numbers |
| RST  | `0x04` | `00000100` | **Reset**: aborts the connection immediately (often used for errors / closed ports) |
| PSH  | `0x08` | `00001000` | **Push**: tells receiver to deliver data to the application ASAP (don’t wait to buffer) |
| ACK  | `0x10` | `00010000` | **Acknowledge**: ACK field is valid; confirms receipt of bytes |
| URG  | `0x20` | `00100000` | **Urgent**: urgent pointer field is valid (rare in modern usage) |

### Common Combinations

#### SYN (Client → Server)
- Flags: `SYN`
- Hex: `0x02`
- Meaning: "I want to start a TCP connection. Here is my initial sequence number."

#### SYN-ACK (Server → Client)
- Flags: `SYN + ACK`
- Hex: `0x12` (`0x10 + 0x02`)
- Meaning: "I accept. Here is my sequence number, and I acknowledge yours."

#### ACK (Client → Server)
- Flags: `ACK`
- Hex: `0x10`
- Meaning: "Acknowledgment is valid. Connection is established (or data was received)."

#### FIN-ACK (Closing handshake)
- Flags: `FIN + ACK`
- Hex: `0x11` (`0x10 + 0x01`)
- Meaning: "I’m done sending data and I acknowledge what you sent. Begin closing."

#### RST-ACK (Abort)
- Flags: `RST + ACK`
- Hex: `0x14` (`0x10 + 0x04`)
- Meaning: "Stop. Connection is invalid/aborted, and I acknowledge what was seen."

### Notes
- Flags are stored as **bits**, so multiple flags can be set at once.
- Example: `SYN + ACK = 0x02 + 0x10 = 0x12`.
- These flags are part of how TCP performs reliable connection setup, data transfer control, and teardown.

## 🎯 Important Clarifications

### ✔ Sequence numbers count bytes
- 1 byte = 8 bits
- TCP increments sequence numbers by number of bytes sent
- Not by bits
- Not by 32-bit words

### ✔ 32-bit header diagrams
TCP header diagrams are drawn in 32-bit rows for formatting.
This does NOT mean TCP counts in 32-byte chunks.

The sequence number field itself is 32 bits wide:

0 → 2³² − 1

Then it wraps around.

---

## 🔐 Why Random Initial Sequence Numbers?

Security reasons:
- Prevent attackers from predicting valid sequence numbers
- Prevent old delayed packets from interfering with new connections

---

## 🔄 How This Connects to HW3

HW3 binary protocol:
- Uses a header byte
- Uses bit fields
- Uses metadata to determine how many bytes to read next

TCP:
- Uses structured headers
- Uses fields to determine how to interpret following bytes

Both require:
- Reading raw bytes
- Interpreting them based on a defined bit layout
