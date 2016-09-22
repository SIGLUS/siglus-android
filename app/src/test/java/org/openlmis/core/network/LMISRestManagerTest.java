package org.openlmis.core.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLSession;

import retrofit.client.Client;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(LMISTestRunner.class)
public class LMISRestManagerTest {
    @Test
    public void shouldVerify() throws Exception {
        SSLSession sslSession = mock(SSLSession.class);
        X509Certificate certificate = mock(X509Certificate.class);
        Principal principal = new Principal() {
            @Override
            public String getName() {
                return "CN=lmis.cmam.gov.mz";
            }
        };
        when(sslSession.getPeerCertificates()).thenReturn(new Certificate[]{certificate});
        when(certificate.getSubjectDN()).thenReturn(principal);

        LMISRestManagerMock.mockClient = mock(Client.class);
        LMISRestManagerMock lmisRestManagerMock = new LMISRestManagerMock(RuntimeEnvironment.application);
        lmisRestManagerMock.superGetSSLClient();
        assertTrue(lmisRestManagerMock.okHttpClient.getHostnameVerifier().verify("whateverName", sslSession));
    }
}