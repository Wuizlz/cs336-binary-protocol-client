/*

*/

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

class Client {
   public static void main(String[] args) {
      final BufferedInputStream in = new BufferedInputStream(System.in);
      int byteCounter = 0;
      int b;
      int EOF = -1;
      int EOT = 128;
      try {
         while ((b = in.read()) != EOF) {
            byteCounter++;
            if (b == EOT)
               break;

            if ((b & 0x80) != 0) {
               int length = b & 0x7F;
               for (int i = 0; i < length; i++) {
                  int ch = in.read();
                  if (ch == -1) {
                     // unexpected EOF: report byte count then stop
                     break;
                  }
                  byteCounter++;
                  System.out.print((char) ch);
               }
               System.out.println();
            }

         }

      } catch (IOException e) {

      }

      System.out.printf("\nRead %d bytes from standard input.\n", byteCounter);
   }
}
