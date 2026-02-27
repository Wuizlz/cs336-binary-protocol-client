# CS336 HW3 — Binary Message Protocol Client (Client.java)

This project implements the **client** side of a simple client/server system that communicates over a simulated stream using a **binary message protocol**.

The server (`Server.java`) writes binary messages to **stdout**, and the client (`Client.java`) reads those messages from **stdin**. A pipe is used to simulate the communication channel.

Example:
```bash
java Server 10 | java Client













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
