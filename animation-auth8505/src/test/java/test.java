import cn.hutool.crypto.SecureUtil;
import com.yiguan.TestContoller;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author: lw
 * @CreateTime: 2022-10-11  20:35
 * @Description: TODO
 * @Version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = test.class)
public class test {

    @Test
    public void md5(){
        String s = "/user/*/454";
        System.out.println(s.split("\\*")[0]);
    }
}
