# MCBans TCP Protocol

This document describes the binary TCP protocol used to communicate between the MCBans plugin and the MCBans API server.

---

## 1. Overview

MCBans uses a persistent, stateful binary TCP connection. Each plugin instance maintains a pool of connections that are reused across requests.

| Property | Value |
|---|---|
| Default host | `api.v4.direct.mcbans.com` |
| Default port | `8082` |
| Transport | TCP with Keep-Alive |
| Socket timeout | 5 000 ms |
| Max pool size | 10 connections |
| Keepalive interval | 30 s (first ping after 5 s) |
| Encryption | RSA-2048/OAEP (optional, enabled by default) |
| Object serialization | Java `ObjectOutputStream` |

Environment variables `OVERRIDE_API` and `OVERRIDE_PORT` can redirect traffic to a different endpoint (useful for testing).

---

## 2. Connection Lifecycle

```
Plugin starts
     │
     ▼
┌──────────────────────┐
│  Open TCP socket to  │
│  api.v4.mcbans.com   │
│  :8082               │
│  + set SO_KEEPALIVE  │
│  + set timeout 5 s   │
└──────────┬───────────┘
           │
           ▼
   ┌───────────────┐   MCBans.encryptAPI == true?
   │EncryptConnection│◄─────────────────────────────────YES
   │  handshake    │
   └──────┬────────┘
          │
          ▼
   ┌──────────────────┐
   │ SessionRegister  │  send API key + secret
   │   handshake      │
   └──────┬───────────┘
          │
          ▼
   ┌──────────────────┐
   │ Connection added │
   │  to pool (max 10)│
   └──────┬───────────┘
          │
          ▼
   ┌──────────────────┐
   │  Timer: every    │◄──── keepalive every 30 s
   │  VerifyConnection│      (first at 5 s)
   └──────┬───────────┘
          │  failure
          └──────────────► remove from pool, close socket
```

---

## 3. Frame Formats

### 3.1 Scalar Encoding

All multi-byte integers are **big-endian**.

| Type | Wire size | Encoding |
|---|---|---|
| `byte` | 1 byte | raw |
| `boolean` | 1 byte | `0x00` = false, `0x01` = true |
| `int` | 4 bytes | signed big-endian |
| `long` | 8 bytes | signed big-endian |
| `double` | 8 bytes | IEEE 754 big-endian |
| `String` | 4 + N bytes | 4-byte length prefix then UTF-8 bytes |
| `byte[]` | 8 + N bytes | 8-byte length prefix then raw bytes |

### 3.2 Command Frame

Every message starts with a **command frame**:

```
┌──────────────────────────┐
│  command  │  referenceId │
│  1 byte   │   8 bytes    │
└──────────────────────────┘
```

`referenceId` is a 64-bit identifier used to correlate requests and responses.

---

## 4. Command Codes

| Code | Name | Direction |
|---|---|---|
| `127` | `SessionRegister` | C → S |
| `126` | `VerifyConnection` / ACK | C → S / S → C |
| `125` | `EncryptConnection` | C → S |
| `124` | Error response | S → C |
| `-127` | `SessionClose` | C → S |
| `-126` | Bad API key | S → C |
| `10` | `BanStatusByPlayerName` | C → S / S → C |
| `11` | `BanStatusByPlayerUUID` | C → S / S → C |
| `20` | `BanPlayer` | C → S |
| `21` | `BanIp` | C → S |
| `25` | `BanSync` | C → S / S → C |
| `28` | `UnBanPlayer` | C → S |
| `29` | `UnBanIp` | C → S |
| `40` | `InformationCallback` | C → S |
| `50` | `GetPlayerInventory` | C → S / S → C |
| `51` | `SavePlayerInventory` | C → S |
| `60` | `PendingActions` | C → S / S → C |
| `70` | `PlayerLookup` | C → S / S → C |
| `71` | `BanLookup` | C → S / S → C |

---

## 5. Handshakes

### 5.1 Encryption Handshake

Performed first, before session registration, when `MCBans.encryptAPI` is `true`.

```
Client                                           Server
  │                                                │
  │── EncryptConnection (cmd=125) ────────────────►│
  │                                                │
  │  [Client wraps streams with RSA encryption]    │
  │                                                │
  │── client public key (byte[]) ─────────────────►│
  │   (2048-bit RSA DER-encoded)                   │
  │                                                │
  │◄── ACK (cmd=126) ──────────────────────────────│
  │     OR                                         │
  │◄── ERR (cmd=124) + String:message ─────────────│
  │                                                │
```

After the handshake, all subsequent data is encrypted (see §6).

### 5.2 Session Registration Handshake

```
Client                                           Server
  │                                                │
  │── SessionRegister (cmd=127) ──────────────────►│
  │── String: apiKey ─────────────────────────────►│
  │── String: "secret" ───────────────────────────►│
  │                                                │
  │◄── ACK (cmd=126) ──────────────────────────────│  success
  │     OR                                         │
  │◄── BAD_KEY (cmd=-126) + String:message ────────│  failure
  │                                                │
```

### 5.3 Keepalive

```
Client                                           Server
  │    (every 30 s)                                │
  │── VerifyConnection (cmd=126) ─────────────────►│
  │◄── ACK (cmd=126) ──────────────────────────────│
  │                                                │
```

If the keepalive fails the connection is discarded and the pool size decremented.

---

## 6. Encryption Layer

When encryption is enabled each side uses **RSA-2048 with OAEP (SHA-256 / MGF1)**.

- The **server's public key** is embedded in the client (Base64-encoded DER) and is used to encrypt data sent *to* the server.
- The **client generates a fresh 2048-bit key pair** per connection; the public key is sent to the server during the encryption handshake so the server can encrypt data sent *back* to the client.

### 6.1 Encrypted Chunk Format

Because RSA can only encrypt blocks smaller than the key size, data is split into **190-byte plaintext chunks**:

```
┌──────────────────────────────────────────────┐
│  total plaintext length   │  8 bytes (long)  │
├──────────────────────────────────────────────┤
│  number of chunks         │  8 bytes (long)  │
├──────────────────────────────────────────────┤
│  chunk 1 length           │  8 bytes (long)  │
│  chunk 1 ciphertext       │  N bytes         │
├──────────────────────────────────────────────┤
│  chunk 2 length           │  8 bytes (long)  │
│  chunk 2 ciphertext       │  N bytes         │
├──────────────────────────────────────────────┤
│  …                                           │
└──────────────────────────────────────────────┘
```

Each ciphertext chunk is one RSA-OAEP encrypted block (≈256 bytes for a 2048-bit key).

---

## 7. Request / Response Examples

### 7.1 Ban Status by Player Name

```
Client                                           Server
  │                                                │
  │── cmd=10 ─────────────────────────────────────►│
  │── String: playerName ─────────────────────────►│
  │── String: ipAddress ──────────────────────────►│
  │── boolean: loginRequest ──────────────────────►│
  │                                                │
  │◄── cmd=10 ─────────────────────────────────────│
  │◄── String: uuid (max 32) ───────────────────────│
  │◄── String: name (max 128) ──────────────────────│
  │◄── byte[]: serialized List<Ban> ────────────────│
  │◄── double: reputation ──────────────────────────│
  │◄── byte[]: serialized Ban (active ban or null) ─│
  │◄── boolean: isMCBansStaff ──────────────────────│
  │                                                │
  │  (on error)                                    │
  │◄── cmd=-126 ────────────────────────────────────│
  │                                                │
```

### 7.2 Ban Player

```
Client                                           Server
  │                                                │
  │── cmd=20 ─────────────────────────────────────►│
  │── String: playerName ─────────────────────────►│
  │── boolean: hasUUID ───────────────────────────►│
  │   [if true] String: uuid (no dashes) ─────────►│
  │── String: reason ─────────────────────────────►│
  │── String: adminUUID ──────────────────────────►│
  │── String: playerIP ───────────────────────────►│
  │── byte: banType ──────────────────────────────►│
  │         1 = global, 2 = local, 3 = temporary   │
  │   [if banType==3] String: expiryTime ─────────►│
  │                                                │
  │◄── cmd=126 (ACK) ───────────────────────────────│  success
  │     OR                                         │
  │◄── cmd=124 + String: errorMsg (max 50) ─────────│  failure
  │                                                │
```

### 7.3 Unban Player

```
Client                                           Server
  │                                                │
  │── cmd=28 ─────────────────────────────────────►│
  │── String: playerName ─────────────────────────►│
  │── String: adminUUID ──────────────────────────►│
  │                                                │
  │◄── cmd=126 (ACK) ───────────────────────────────│
  │     OR                                         │
  │◄── cmd=124 + String: errorMsg ──────────────────│
  │                                                │
```

### 7.4 Ban Sync

```
Client                                           Server
  │                                                │
  │── cmd=25 ─────────────────────────────────────►│
  │                                                │
  │◄── cmd=25 ─────────────────────────────────────│
  │◄── byte[]: serialized ban data ────────────────│
  │                                                │
```

---

## 8. Connection Pool

```
┌──────────────────────────────────────────────────────────────┐
│                       ConnectionPool                         │
│                                                              │
│  Queue<Client>  (capacity: 10)                               │
│  ┌───┐ ┌───┐ ┌───┐ ┌───┐  …                                 │
│  │ C1│ │ C2│ │ C3│ │ C4│                                     │
│  └───┘ └───┘ └───┘ └───┘                                     │
│                                                              │
│  Map<Client, Timer>  keepAliveTimers                         │
│                                                              │
│  acquire():                                                  │
│    poll queue → if null AND connections < 10 → new Client()  │
│    else block / return null                                  │
│                                                              │
│  release(client):                                            │
│    schedule keepalive timer (delay=5s, period=30s)           │
│    add client back to queue                                  │
└──────────────────────────────────────────────────────────────┘
```

---

## 9. Error Codes Summary

| Code | Meaning | Followed by |
|---|---|---|
| `126` | Success / ACK | — |
| `124` | Generic error | `String` error message |
| `-126` | Bad API key | `String` detail message |
| `-127` | Session close | — |

---

## 10. Data-Type Reference

```
String  ──►  [ int:length (4 B) ][ UTF-8 bytes (N B) ]

byte[]  ──►  [ long:length (8 B) ][ raw bytes (N B) ]

boolean ──►  [ 0x00 | 0x01 (1 B) ]

int     ──►  [ signed big-endian (4 B) ]

long    ──►  [ signed big-endian (8 B) ]

double  ──►  [ IEEE-754 big-endian (8 B) ]
```

---

## 11. Relevant Source Files

| File | Purpose |
|---|---|
| `src/main/java/com/mcbans/client/Client.java` | Core connection, encryption & registration |
| `src/main/java/com/mcbans/client/ConnectionPool.java` | Pool management and keepalive |
| `src/main/java/com/mcbans/client/ServerMCBansCommands.java` | Command code enum |
| `src/main/java/com/mcbans/client/BanStatusClient.java` | Ban status queries |
| `src/main/java/com/mcbans/client/BanClient.java` | Ban / unban requests |
| `src/main/java/com/mcbans/utils/WriteToOutputStream.java` | Serialization helpers |
| `src/main/java/com/mcbans/utils/ReadFromInputStream.java` | Deserialization helpers |
| `src/main/java/com/mcbans/utils/ObjectSerializer.java` | Java object serialization |
| `src/main/java/com/mcbans/utils/encryption/EncryptionSettings.java` | RSA key generation |
| `src/main/java/com/mcbans/utils/encryption/EncryptedOutputStream.java` | Encrypting output stream |
| `src/main/java/com/mcbans/utils/encryption/EncryptedInputStream.java` | Decrypting input stream |
