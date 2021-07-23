/*
 * This program is part of the OpenLMIS logistics management information
 * system platform software.
 *
 * Copyright © 2015 ThoughtWorks, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should
 * have received a copy of the GNU Affero General Public License along with
 * this program. If not, see http://www.gnu.org/licenses. For additional
 * information contact info@OpenLMIS.org
 */

package org.openlmis.core.network;

import static javax.net.ssl.HttpsURLConnection.getDefaultHostnameVerifier;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.openlmis.core.LMISApp;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;

@SuppressWarnings({"squid:S4830", "squid:S5527"})
public class SSLFactory {

  private SSLFactory() {
  }

  public static SSLSocketFactory getSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
    boolean trustAll = LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_https_trust_all);
    return trustAll ? getTrustAllSocketFactory() : getNormalSocketFactory();
  }

  public static HostnameVerifier getHostnameVerifier() {
    boolean trustAll = LMISApp.getInstance().getFeatureToggleFor(R.bool.feature_https_trust_all);
    return trustAll ? getTrustALLHostnameVerifier() : getNormalHostnameVerifier();
  }

  private static SSLSocketFactory getNormalSocketFactory() throws NoSuchAlgorithmException {
    final SSLContext sslContext = SSLContext.getDefault();
    return sslContext.getSocketFactory();
  }

  private static SSLSocketFactory getTrustAllSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, new TrustManager[]{new TrustAllTrustManager()}, new SecureRandom());
    return sslContext.getSocketFactory();
  }

  private static HostnameVerifier getTrustALLHostnameVerifier() {
    return (hostname, session) -> true;
  }

  private static HostnameVerifier getNormalHostnameVerifier() {
    return (hostname, session) -> {
      // OVERRIDE HOSTNAME VERIFIER BECAUSE WE CONNECT TO ELB DIRECTLY DUE TO OCCASIONAL DNS
      // ISSUES IN MOZAMBIQUE
      X509Certificate cert;
      try {
        cert = (X509Certificate) session.getPeerCertificates()[0];
        if (cert.getSubjectDN().getName().equals("CN=lmis.cmam.gov.mz")) {
          return true;
        }
      } catch (SSLPeerUnverifiedException e) {
        new LMISException(e, "LMISRestManager,verify").reportToFabric();
      }
      return getDefaultHostnameVerifier().verify(hostname, session);
    };
  }

  private static class TrustAllTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
      // do nothing
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] x509Certificates, String s) {
      // do nothing
    }


    @Override
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[]{};
    }
  }
}
