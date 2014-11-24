
package simple.home.jtbuaa;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import android.util.Log;

public class ShellInterface {

    public static String doExec(String[] commands) {
        return doExec(commands, false);
    }

    public static String doExec(String[] commands, boolean resNeeded) {
        Process process = null;
        DataOutputStream os = null;
        DataInputStream osRes = null;
        DataInputStream osErr = null;
        String res = "";

        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            osRes = new DataInputStream(process.getInputStream());
            osErr = new DataInputStream(process.getErrorStream());

            String line = "";
            for (String single : commands) {
                os.writeBytes(single + "\n");
                os.flush();
            }

            os.writeBytes("exit\n");
            os.flush();

            if (resNeeded) {
                while ((line = osRes.readLine()) != null) {
                    res += line + "\n";
                }
                while ((line = osErr.readLine()) != null) {
                    res += line + "\n";
                }
            }

            process.waitFor();

        } catch (Exception e) {
            res += e.getMessage();
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (osRes != null) {
                    osRes.close();
                }
                if (osErr != null) {
                    osErr.close();
                }
                process.destroy();
            } catch (Exception e) {
                // nothing
            }
        }
        return res;
    }
}
