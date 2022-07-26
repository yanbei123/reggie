import org.junit.jupiter.api.Test;

/**
 * Created with IntelliJ IDEA.
 *
 * @Author: 燕北
 * @Date: 2022/06/27/10:53
 * @Description:
 */
public class FileUploadTest {
    @Test
    public void test01(){
        String fileName = "sadad.jpg";
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        System.out.println(suffix);
    }
}
