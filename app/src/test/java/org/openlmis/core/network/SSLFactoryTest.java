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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;

@RunWith(LMISTestRunner.class)
public class SSLFactoryTest {

  @Test
  public void testGetNormalSocketFactory() throws NoSuchAlgorithmException {
    Assert.assertNotNull(SSLFactory.getNormalSocketFactory());
  }

  @Test
  public void testGetTrustAllSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
    Assert.assertNotNull(SSLFactory.getTrustAllSocketFactory());
  }

  @Test
  public void testGetTrustALLHostnameVerifier() {
    Assert.assertNotNull(SSLFactory.getTrustALLHostnameVerifier());
  }

  @Test
  public void testGetNormalHostnameVerifier() {
    Assert.assertNotNull(SSLFactory.getNormalHostnameVerifier());
  }
}