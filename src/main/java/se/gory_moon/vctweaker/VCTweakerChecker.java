package se.gory_moon.vctweaker;

import net.minecraftforge.fml.common.CertificateHelper;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import se.gory_moon.vctweaker.util.Log;

import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.Map;

public class VCTweakerChecker implements IFMLCallHook {

    private static final String FINGERPRINT = "F2:AE:88:7A:76:E1:B5:21:B5:1B:B4:4C:F1:36:91:80:15:B5:92:7A".toLowerCase().replace(":", "");

    @Override
    public Void call() throws Exception {
        CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();
        if (codeSource.getLocation().getProtocol().equals("jar")) {
            Certificate[] certificates = codeSource.getCertificates();
            if (certificates != null) {
                for (Certificate cert : certificates) {
                    String fingerprint = CertificateHelper.getFingerprint(cert);
                    if (fingerprint.equals(FINGERPRINT)) {
                        Log.info("Found valid fingerprint for VCTweaker. Certificate fingerprint {}", fingerprint);
                    } else {
                        Log.error("Found invalid fingerprint for VCTweaker: {}", fingerprint);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }
}
