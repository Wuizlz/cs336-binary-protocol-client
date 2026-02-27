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

# CS336 HW3 — Binary Protocol Parsing Notes (Complete)

These notes explain in detail how to correctly read and interpret the server’s byte stream for the HW3 client.

The most important idea:

> You are NOT reading “text” or “numbers” directly.  
> You are reading a structured binary protocol where the first byte of each message is a HEADER that tells you what comes next.

---

# 1) Streams, Bytes, and EOF in Java

## InputStream.read()

```java
int b = in.read();
```

`read()` returns an **int**, not a byte, because it must represent:

- A valid byte value: `0–255`
- EOF: `-1`

### Important distinctions

| Value | Meaning |
|--------|----------|
| -1 | End of file (stream ended unexpectedly or naturally) |
| 0x80 (128) | End-of-Transmission (protocol-defined message) |

⚠️ `0x80` is NOT EOF.  
It is a real byte sent by the server.

---

# 2) The Stream Structure

The server sends a continuous stream of bytes:

```
[header][body][header][body][header][body]...
```

Each message:
- starts with exactly **1 header byte**
- is followed by some number of body bytes
- ends when the next header begins

---

# 3) Header Byte Structure (8 Bits)

Each message begins with 1 byte:

```
bit7 bit6 bit5 bit4 bit3 bit2 bit1 bit0
MSB                                LSB
```

- MSB (bit7) = Most Significant Bit = 128 = `0x80`
- LSB (bit0) = Least Significant Bit = 1

---

# 4) How We Read Bits (Masking)

We never convert to strings like `"10101010"` to compute logic.

We use **bit masks**.

## Check MSB

```java
boolean msbIs1 = (b & 0x80) != 0;
```

- `0x80 = 10000000`
- If result ≠ 0 → MSB is 1

## Extract lower 7 bits

```java
int lower = b & 0x7F;
```

- `0x7F = 01111111`
- Clears MSB
- Keeps bits 0–6

---

# 5) TEXT Messages (MSB = 1, but not 0x80)

Header format:

```
1 LLLLLLL
```

- MSB = 1 → TEXT message
- Lower 7 bits = LENGTH (in BYTES)

Important:

> The 7 bits are NOT the text.
> They are the number of ASCII bytes that follow.

## Extracting the length

```java
int length = b & 0x7F;
```

Length is a normal decimal integer.

If lower bits = `01111100` → length = 124.

That means:

- Read next 124 BYTES
- Each byte is one ASCII character
- Print each character immediately

---

# 6) End-of-Transmission (EOT)

`0x80 = 10000000₂ = 128`

This header means:

- No body
- No more messages
- Server will close output

When you read `0x80`:
- Count that byte
- Stop parsing
- Print total bytes received
- Exit cleanly

Do NOT treat it as a text message of length 0.

---

# 7) NUMERIC Messages (MSB = 0)

Header format:

```
0 b6 b5 b4 b3 b2 b1 b0
```

- MSB = 0 → Numeric message
- Lower 7 bits form a bitfield

Each bit describes the next value:

- bit = 0 → next value is FLOAT (4 bytes)
- bit = 1 → next value is LONG (8 bytes)

## Reading order

Bits are read from **LSB to MSB**:

```java
for (int i = 0; i < 7; i++) {
    int bit = (bitfield >> i) & 1;
}
```

Bit 0 first, then bit 1, etc.

---

# 8) Example: Header = 10110000

Binary:

```
10110000
```

Decimal:
- 176

Step 1: Not EOF
- 176 != -1

Step 2: Not EOT
- 176 != 0x80

Step 3: MSB check

```
10110000
10000000
---------
10000000  (non-zero)
```

MSB = 1 → TEXT

Step 4: Extract length

```
10110000
01111111
---------
00110000
```

`00110000₂ = 48`

So this means:

> TEXT message of length 48 bytes

The client must:
- read next 48 bytes
- print them as ASCII
- then read the next header

---

# 9) Why Length is in BYTES (Not Bits)

Streams operate in BYTES.

- `read()` reads 1 byte.
- ASCII characters are 1 byte.
- Protocol lengths are in bytes.

If length = 124:
- Read 124 bytes.
- That equals 124 × 8 = 992 bits of actual data.
- But you never read bits individually — only bytes.

---

# 10) Why Masking Works (Deep Explanation)

When you do:

```java
b & 0x80
```

The CPU does NOT convert to binary strings.

It already stores numbers in binary internally.

Example:

If `b = 252`

Binary:
```
11111100
```

Mask:
```
10000000
```

AND:
```
10000000
```

Non-zero → MSB = 1.

The CPU directly performs bitwise logic on stored bits.

---

# 11) Clean Loop Structure

Outer loop:

```java
while ((b = in.read()) != -1) {
    byteCount++;

    if (b == 0x80) {
        break;
    }

    if ((b & 0x80) != 0) {
        // TEXT branch
    } else {
        // NUMERIC branch
    }
}
```

Important:

- Increment byte count for every successful read.
- If `-1` occurs before EOT → unexpected EOF.
- `0x80` is counted, then exit cleanly.

---

# 12) Unexpected EOF

If you are expecting more bytes (based on length or numeric bitfield) and `read()` returns `-1`:

- Print error message
- Print total bytes received
- Exit cleanly

Never crash.

---

# 13) Core Mental Model

You are building a STREAM PARSER.

Each header byte:
- tells you the type of message
- tells you how many bytes follow
- tells you how to interpret those bytes

You:
1. Read header
2. Interpret header
3. Read exactly what header specifies
4. Repeat

This is exactly how real protocols (like TCP/IP headers) work.

---

# Final Key Takeaways

- Streams are byte-based.
- Header byte is metadata, not content.
- MSB determines message type.
- Lower 7 bits determine structure.
- Masking extracts bits safely.
- EOT is a real byte (0x80), not EOF.
- Parsing is incremental and structured.

You are now thinking like a protocol engineer.
