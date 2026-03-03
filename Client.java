/*
   Course: CS 33600
   Name: Daniel Briseno
   Email: dbriseno@pnw.edu
   Assignment: 3
*/

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

class Client {
   public static void main(String[] args) {
      final BufferedInputStream in = new BufferedInputStream(System.in);
      int byteCounter = 0;
      int b;
      int EOF = -1;
      int EOT = 128;
      boolean unexpectedEOF = false;
      boolean sawEOT = false;
      try {
         // Outer loop label used to break out of all nested loops on parsing failure.
         outer: while ((b = in.read()) != EOF) {
            byteCounter++;
            if (b == EOT) {
               sawEOT = true;
               break outer;
            }

            if ((b & 0x80) != 0) {
               int length = b & 0x7F;
               for (int i = 0; i < length; i++) {
                  int ch = in.read();
                  if (ch == EOF) {
                     // unexpected EOF: report byte count then stop
                     unexpectedEOF = true;
                     break outer;
                  }
                  byteCounter++;
                  System.out.print((char) ch);
               }
               System.out.println();
            }

            else {
               int bitfield = b & 0x7f;
               for (int i = 0; i < 7; i++) {
                  int bit = (bitfield >> i) & 1;

                  if (bit == 0) {
                     byte[] floatByte = in.readNBytes(4);
                     byteCounter += floatByte.length;
                     if (floatByte.length < 4) {
                        unexpectedEOF = true;
                        break outer; /// Exit main immediately after reporting unexpected EOF
                     }
                     byte[] be = new byte[4];
                     be[0] = floatByte[2]; // b0 (MSB)
                     be[1] = floatByte[3]; // b1
                     be[2] = floatByte[0]; // b2
                     be[3] = floatByte[1]; // b3 (LSB)
                     // Convert the reordered big-endian byte sequence into a Java float
                     float f = ByteBuffer.wrap(be).getFloat();
                     System.out.printf("%.10f%n", f);
                  } else {
                     byte[] longByte = in.readNBytes(8);
                     byteCounter += longByte.length;
                     if (longByte.length < 8) {
                        unexpectedEOF = true;
                        break outer;
                     }

                     byte[] be = new byte[8];
                     // Convert little-endian → big-endian
                     for (int j = 0; j < 8; j++) {
                        be[j] = longByte[7 - j];
                     }
                     // Convert the reordered big-endian byte sequence into a Java long
                     long l = ByteBuffer.wrap(be).getLong();
                     System.out.printf("%d%n", l);

                  }

               }

            }

         }
         
         if (!sawEOT && !unexpectedEOF) {
            unexpectedEOF = true; // EOF happened before EOT
         }
         

      } catch (IOException e) {

      }
      System.out.println();
      if (unexpectedEOF) {
         System.out.printf(
               "Error: unexpected end-of-file after reading %d bytes from standard input.%n", byteCounter);
      } else {
         System.out.printf("Read %d bytes from standard input.%n", byteCounter);
      }
   }
}
