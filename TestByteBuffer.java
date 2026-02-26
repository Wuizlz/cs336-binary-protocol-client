import java.nio.ByteBuffer;
import java.util.Arrays;

public class TestByteBuffer
{
   public static void main(String[] args)
   {
      // float to byte[]
      byte[] b1 = ByteBuffer.allocate(Float.BYTES).putFloat(8765.432F).array();
      System.out.println(Arrays.toString( b1 ));

      // long to byte[]
      byte[] b2 = ByteBuffer.allocate(Long.BYTES).putLong(987654321).array();
      System.out.println(Arrays.toString( b2 ));

      // byte[] to float
      float f = ByteBuffer.wrap(new byte[]{70, 64, -26, -74}).getFloat();
      System.out.println( f );

      // byte[] to long
      byte[] b3 = {1, 55, 105, 35, -6, -8, 11, 78};
      long n = ByteBuffer.wrap( b3 ).getLong();
      System.out.println( n );
   }
}
