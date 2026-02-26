/*
   Server program for Hw3.

   This program provides the input stream
   of data needed by the client program.

   Do not change this file.
*/

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Random;
import java.nio.ByteBuffer;

class Server
{
   public static void main(String[] args)
   {
      final Random random = new Random();

      // Open the log file.
      final File logFile = new File("log_file.txt");
      PrintWriter log = null;
      try
      {
         log = new PrintWriter(
                   new BufferedWriter(
                       new FileWriter(logFile)));
      }
      catch (IOException e)
      {
         System.err.printf("ERROR! Could not open log file: %s\n", logFile);
         e.printStackTrace(System.err);
         System.exit(-1);
      }

      long byteCounter = 0; // Count the number of bytes sent by this server.

      final long messages;  // Number of messages to send.
      if (0 < args.length)  // Check for a command line argument.
      {
         // Send args[0] number of messages.
         messages = Long.parseLong( args[0] );
      }
      else
      {
         // Send a random number of messages (up to 1,000 messages).
         messages = random.nextInt(1_000);
      }

      log.printf("\nServer log of %d messages.\n\n", 1 + messages);
      log.flush();

      // Send the messages.
      for (long m = 0; m < messages; ++m)
      {
         // "Flip" a coin to determine the message type,
         // either a numeric message or a character message.
         if ( random.nextBoolean() ) // Send a numeric message.
         {
            // Choose a bit pattern for floats and longs.
            int messageHeader = random.nextInt(128);
            // Send the message header.
            System.out.write(messageHeader);
            System.out.flush();
            log.printf("%s\n", toBinary(messageHeader));
            log.flush();
            ++byteCounter;

            // Send the numbers.
            for (int i = 0; i < 7; ++i, messageHeader >>= 1)
            {
               if ((0x01 & messageHeader) == 0x01) // Send a long in little-endian byte order.
               {
                  // Generate a random long.
                  final long n = random.nextLong();

                  // Convert the long into its array of bytes.
                  final byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(n).array();

                  // Record the long in the log file.
                  log.printf("Sending long = %d (0x%02x%02x%02x%02x%02x%02x%02x%02x)\n",
                             n,
                             bytes[0], bytes[1], bytes[2], bytes[3],
                             bytes[4], bytes[5], bytes[6], bytes[7] );

                  // Send the long in little-endian byte order.
                  // NOTE: The JVM stores its longs in big-endian order.
                  for (int j = 7; j >= 0; --j)
                  {
                     System.out.write(bytes[j]);
                            log.printf("%02x  ", bytes[j]);
                  }
                  System.out.flush();
                         log.printf("\n");
                         log.flush();
                  byteCounter += 8;
               }
               else // Send a float in weird-endian byte order.
               {
                  // Generate a random float.
                  final int numDigits = random.nextInt(7);
                  final int n = random.nextInt((int)Math.pow(10, numDigits));
                  final float f = random.nextFloat();
                  final int s = random.nextBoolean() ? 1 : -1;
                  final float k = s*(n + f);

                  // Convert the float into its array of bytes.
                  final byte[] bytes = ByteBuffer.allocate(Float.BYTES).putFloat(k).array();

                  // Record the float in the log file.
                  log.printf("Sending float = %.10f (0x%02x%02x%02x%02x)\n",
                             k,
                             bytes[0], bytes[1], bytes[2], bytes[3] );

                  // Send the float in weird-endian byte order.
                  // NOTE: The JVM stores its floats in big-endian order.
                  System.out.write(          bytes[2]); // 2nd least sig byte
                         log.printf("%02x ", bytes[2]);
                  System.out.write(          bytes[3]); // least sig byte
                         log.printf("%02x ", bytes[3]);
                  System.out.write(          bytes[0]); // most sig byte
                         log.printf("%02x ", bytes[0]);
                  System.out.write(          bytes[1]); // 2nd most sig byte
                         log.printf("%02x",  bytes[1]);

                  System.out.flush();
                         log.printf("\n");
                         log.flush();
                  byteCounter += 4;
               }
            }
            log.printf("\n");
            log.flush();
         }
         else // Send a character message.
         {
            // Number of characters in this message.
            final int count = 1 + random.nextInt(127); // 1 <= count <= 127
            // Make the "message header" byte.
            final int messageHeader = 0x80 | count; // Binary (bitwise) or operation.
            // Send the message header byte.
            System.out.write(messageHeader);
            System.out.flush();
            log.printf("%s (%d characters)\n", toBinary(messageHeader), count);
            log.flush();
            ++byteCounter;
            // Send the characters.
            for (int i = 0; i < count; ++i)
            {
               // A random printable character (see an ascii table).
               final int rndChar = 32 + random.nextInt(95);
               System.out.write(rndChar);
               System.out.flush();
               log.printf("%c", rndChar);
               log.flush();
               ++byteCounter;
            }
            log.printf("\n\n");
            log.flush();
         }
      }

      // Send an "end-of-data" message.
      System.out.write(0x80);
      System.out.flush();
      log.printf("%s\n", toBinary(0x80));
      log.flush();
      ++byteCounter;

      log.printf("\nThe server wrote %d bytes to standard output.\n", byteCounter);
      log.flush();

      log.close();
      System.out.close();
   }


   /**
      @param c  an int whose 8 least significant bits will be converted to a String
      @return the binary String representation of the 8 least significant bits of c
   */
   public static String toBinary(int c)
   {
      String bits = "";
      for (int i = 0; i < 8; ++i)
      {
         if ( (c & 0x80) == 0x80 )
         {
            bits += "1";
         }
         else
         {
            bits += "0";
         }
         c <<= 1;
      }
      return bits;
   }
}
