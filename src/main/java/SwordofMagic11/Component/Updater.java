package SwordofMagic11.Component;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static SwordofMagic11.SomCore.Log;
import static SwordofMagic11.SomCore.plugin;

public class Updater {

    public static void UpdatePlugin() {
        try {
            Log("§a[Updater]§eDownloading...");
            URL url = new URL("http://192.168.0.16:25515/SwordofMagic11/DataBase/SwordofMagic11.jar");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.connect();

            int httpStatusCode = conn.getResponseCode();
            if (httpStatusCode != HttpURLConnection.HTTP_OK) {
                throw new Exception("HTTP Status " + httpStatusCode);
            }

            DataInputStream dataInStream = new DataInputStream(conn.getInputStream());
            DataOutputStream dataOutStream = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(plugin().getDataFolder().getPath() + ".jar"))));
            byte[] b = new byte[4096];
            int readByte;
            while (-1 != (readByte = dataInStream.read(b))) {
                dataOutStream.write(b, 0, readByte);
            }
            dataInStream.close();
            dataOutStream.close();
            Log("§a[Updater]§bComplete!");
        } catch (Exception e) {
            Log("§a[Updater]§cError");
            throw new RuntimeException(e);
        }
    }
}
