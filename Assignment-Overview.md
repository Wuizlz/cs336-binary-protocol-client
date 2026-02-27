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
