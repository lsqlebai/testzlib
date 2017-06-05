import com.test.communication.client.GameServerClient;
import com.test.communication.sever.ServerEndPoint;

import java.util.zip.DataFormatException;

public class Main {
    public static void main(String[] args) throws DataFormatException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new ServerEndPoint().start();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GameServerClient().init();
            }
        }).start();
    }




}
