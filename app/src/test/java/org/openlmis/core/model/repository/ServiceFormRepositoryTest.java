package org.openlmis.core.model.repository;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.Service;
import org.robolectric.RuntimeEnvironment;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
public class ServiceFormRepositoryTest extends LMISRepositoryUnitTest {

  ServiceFormRepository serviceFormRepository;
  private Service service;

  @Before
  public void setup() throws LMISException {
    serviceFormRepository = RoboGuice
        .getInjector(RuntimeEnvironment.application)
        .getInstance(ServiceFormRepository.class);
    service = new Service();
    service.setCode("serviceCode");
    service.setName("serviceName");
    service.setActive(false);
  }

  @Test
  public void shouldQueryByCode() throws LMISException {
    Service result = serviceFormRepository.queryByCode(service.getCode());
    assertThat(result, is(nullValue()));
  }

  @Test
  public void shouldListAllActive() throws LMISException {
    List<Service> result = serviceFormRepository.listAllActive();
    assertThat(result, is(empty()));
  }

  @Test
  public void shouldListAllActiveWithProgram() throws LMISException {
    Program program = new Program();
    program.setId(123L);
    List<Service> result = serviceFormRepository.listAllActiveWithProgram(program);
    assertThat(result, is(empty()));
  }

  @Test
  public void shouldCreateOrUpdate() throws LMISException {
    Service result = serviceFormRepository.queryByCode(service.getCode());
    assertThat(result, is(nullValue()));
    serviceFormRepository.createOrUpdate(service);
    result = serviceFormRepository.queryByCode(service.getCode());
    assertThat(result.getCode(), is(service.getCode()));

    result.setName("change service name");
    serviceFormRepository.createOrUpdate(result);
    assertThat(serviceFormRepository.queryByCode(service.getCode()).getName(),
        is(result.getName()));
  }

  @Test
  public void shouldBatchCreateOrUpdate() throws LMISException {
    Service result = serviceFormRepository.queryByCode(service.getCode());
    assertThat(result, is(nullValue()));
    List<Service> serviceList = new ArrayList<>();
    serviceList.add(service);
    serviceFormRepository.batchCreateOrUpdateServiceList(serviceList);
    result = serviceFormRepository.queryByCode(service.getCode());
    assertThat(result.getCode(), is(service.getCode()));

    result.setName("change service name");
    serviceList.clear();
    serviceList.add(result);
    serviceFormRepository.batchCreateOrUpdateServiceList(serviceList);
    assertThat(serviceFormRepository.queryByCode(service.getCode()).getName(),
        is(result.getName()));
  }

}
