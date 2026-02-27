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

## TCP Flags + SEQ/ACK Examples (with Hex)

TCP segments include:
- **Flags** (1-bit controls like SYN/ACK/FIN)
- **SEQ** (Sequence Number): byte index of the first byte in this segment's payload
- **ACK** (Acknowledgment Number): next byte expected from the other side (valid when ACK flag is set)

> Note: SYN and FIN each "consume" 1 sequence number even if no data payload is present.

---

### Example: 3-Way Handshake (Connection Setup)

Assume:
- Client chooses initial sequence number **C = 1000**
- Server chooses initial sequence number **S = 5000**

Hex conversions (32-bit):
- `1000₁₀ = 0x000003E8`
- `1001₁₀ = 0x000003E9`
- `5000₁₀ = 0x00001388`
- `5001₁₀ = 0x00001389`

---

#### 1) Client → Server: SYN
- Flags: `SYN`
- Flags Hex: `0x02` (binary `00000010`)
- SEQ (dec): `1000`
- SEQ (hex, 32-bit): `0x000003E8`
- ACK: not valid (ACK flag not set)

Meaning: "Start connection. My ISN is 1000."

---

#### 2) Server → Client: SYN-ACK
- Flags: `SYN + ACK`
- Flags Hex: `0x12` (binary `00010010`)
- SEQ (dec): `5000`
- SEQ (hex, 32-bit): `0x00001388`
- ACK (dec): `1001`  (acknowledges client's SYN: 1000 + 1)
- ACK (hex, 32-bit): `0x000003E9`

Meaning: "I accept. My ISN is 5000. I received your SYN; send 1001 next."

---

#### 3) Client → Server: ACK
- Flags: `ACK`
- Flags Hex: `0x10` (binary `00010000`)
- SEQ (dec): `1001`  (client moved past SYN)
- SEQ (hex, 32-bit): `0x000003E9`
- ACK (dec): `5001`  (acknowledges server's SYN: 5000 + 1)
- ACK (hex, 32-bit): `0x00001389`

Meaning: "Connection established. I received your SYN; send 5001 next."

---

### After Handshake (First Data Example)

If client now sends `"Hello"` (5 bytes) to the server:

Hex conversions:
- `1006₁₀ = 0x000003EE`

#### Client → Server: ACK + Data ("Hello")
- Flags: `ACK`
- Flags Hex: `0x10`
- SEQ (dec): `1001`
- SEQ (hex, 32-bit): `0x000003E9`
- Payload Length: `5 bytes` → bytes `1001..1005`
- ACK (dec): `5001`
- ACK (hex, 32-bit): `0x00001389`

Server acknowledgment:

#### Server → Client: ACK
- Flags: `ACK`
- Flags Hex: `0x10`
- SEQ (dec): `5001` (assuming server sends no payload yet)
- SEQ (hex, 32-bit): `0x00001389`
- ACK (dec): `1006` (1001 + 5)
- ACK (hex, 32-bit): `0x000003EE`

Meaning: "I received bytes through 1005; send 1006 next."

---

## Common TCP Flag Hex Values (Reference)

| Flag | Hex | Binary (8-bit) | Meaning |
|------|-----|-----------------|---------|
| FIN  | `0x01` | `00000001` | Finish/close connection cleanly |
| SYN  | `0x02` | `00000010` | Start connection / sync sequence numbers |
| RST  | `0x04` | `00000100` | Abort/reset connection |
| PSH  | `0x08` | `00001000` | Push data to app ASAP |
| ACK  | `0x10` | `00010000` | ACK field is valid |
| URG  | `0x20` | `00100000` | Urgent pointer is valid |

### Common Combinations
- `SYN + ACK` = `0x12` (`0x02 + 0x10`)
- `FIN + ACK` = `0x11` (`0x01 + 0x10`)
- `RST + ACK` = `0x14` (`0x04 + 0x10`)
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

# 📡 TCP Header & 3-Way Handshake Notes

These notes summarize key TCP concepts reviewed while studying packet structure, handshake mechanics, and header parsing.

---

# 🧱 TCP Header Structure

Every TCP segment contains:

[TCP Header][Payload (optional)]

## 📦 Minimum TCP Header (20 bytes)

| Field | Size |
|-------|------|
| Source Port | 2 bytes |
| Destination Port | 2 bytes |
| Sequence Number | 4 bytes |
| Acknowledgment Number | 4 bytes |
| Data Offset + Reserved + Flags | 2 bytes |
| Window Size | 2 bytes |
| Checksum | 2 bytes |
| Urgent Pointer | 2 bytes |

After this may come:

- Options (optional)
- Padding (if required)

Then:

- Payload / Data

---

# 🔢 Sequence & Acknowledgment Numbers

TCP treats communication as a continuous stream of bytes.

- Each byte is numbered.
- Sequence numbers identify the first byte in a segment.
- Acknowledgment numbers identify the next expected byte.

### Example

If:

SEQ = 1001  
Segment length = 100 bytes  

Then the segment contains:

Bytes 1001 through 1100  

If the receiver sends:

ACK = 1101  

It means:

All bytes up to 1100 were received successfully.

---

# 🤝 3-Way Handshake

Each side chooses a random Initial Sequence Number (ISN).

---

## 🔵 Step 1 — Client Sends SYN

Client selects:

- Source Port: 51515
- Destination Port: 443
- Client ISN: 1000

```
Source Port:        51515
Destination Port:   443
Sequence Number:    1000
Acknowledgment:     0   (ignored)
Data Offset:        5   (20 bytes)
Flags:              SYN
Window Size:        64240
Payload:            none
```

- ACK field exists but is ignored because ACK flag is not set.
- SYN consumes one sequence number.

---

## 🔴 Step 2 — Server Sends SYN+ACK

Server selects:

- Server ISN: 5000
- ACK = 1001 (client ISN + 1)

```
Source Port:        443
Destination Port:   51515
Sequence Number:    5000
Acknowledgment:     1001
Data Offset:        5
Flags:              SYN, ACK
Window Size:        65535
Payload:            none
```

---

## 🔵 Step 3 — Client Sends Final ACK

```
Source Port:        51515
Destination Port:   443
Sequence Number:    1001
Acknowledgment:     5001
Flags:              ACK
Payload:            none
```

Connection is now established.

---

# 📏 Data Offset

Data Offset indicates TCP header length.

- Field size: 4 bits
- Interpreted as: number of 32-bit words (4-byte chunks)

### Formula

Header Length = DataOffset × 4 bytes

### Examples

| Data Offset | Header Length |
|-------------|---------------|
| 5 | 20 bytes |
| 8 | 32 bytes |

Data Offset tells the receiver where the payload begins.

Payload Start Byte = DataOffset × 4

---

# 🧩 Options & Padding

## Options

Optional TCP extensions negotiated during connection setup.

Common examples:

- MSS (Maximum Segment Size)
- Window Scaling
- SACK
- Timestamps

Options increase header size beyond 20 bytes.

---

## Padding

TCP headers must be divisible by 4 bytes.

If options cause misalignment:

Base header = 20 bytes  
Options = 10 bytes  
Total = 30 bytes (invalid)  
Padding = 2 bytes  
Final header = 32 bytes  
Data Offset = 8  

---

# 🚦 Flags

Flags are individual control bits.

Common flags:

- SYN — initiate connection
- ACK — acknowledgment field valid
- FIN — close connection
- RST — reset connection
- PSH — push data to application
- URG — urgent pointer valid
- CWR / ECE / NS — congestion control

Multiple flags can be set simultaneously.

Example:

SYN + ACK = 0x12  
Binary: 00010010  

---

# 🪟 Window Size

Window is used for flow control.

It tells the sender how many bytes can be sent before waiting.

Example:

ACK = 1001  
Window = 5000  

Meaning:

Bytes 1001 through 6000 may be sent.

If:

Window = 0  

Sender must pause transmission.

---

# ✅ Checksum

Checksum provides error detection.

It is computed over:

- TCP header
- TCP payload
- Pseudo-header (IP data)

Receiver recomputes checksum:

- If match → valid
- If mismatch → segment discarded

TCP reliability handles retransmission.

Checksum performs validation, not sanitization.

---

# 🚨 Urgent Pointer

Only valid if URG flag = 1.

If:

SEQ = S  
UrgentPointer = k  

Then:

Bytes S through S + k − 1 are urgent.

Rarely used in modern networking.

---

# 🧠 Core Model

Each TCP segment:

1. Builds full header.
2. Fills fields based on connection state.
3. Sends packet.
4. Receiver parses header.
5. Updates state.
6. Responds.

- Handshake = 3 full TCP packets.
- Data Offset always exists.
- Options may extend header.
- Window controls flow.
- Checksum validates integrity.
