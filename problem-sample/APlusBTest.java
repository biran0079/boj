import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class APlusBTest {
  @Test
  public void test() {
    APlusB a = new APlusB();
    assertEquals(7, a.add(2,4));
  }
}
