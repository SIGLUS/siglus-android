package org.openlmis.core.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import com.google.inject.AbstractModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.SharedPreferenceMgr;
import org.openlmis.core.model.Inventory;
import org.openlmis.core.model.Period;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.ReportTypeForm;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnRForm.Emergency;
import org.openlmis.core.model.RnRForm.Status;
import org.openlmis.core.model.SyncError;
import org.openlmis.core.model.SyncType;
import org.openlmis.core.model.builder.ProgramBuilder;
import org.openlmis.core.model.builder.ReportTypeFormBuilder;
import org.openlmis.core.model.builder.RnRFormBuilder;
import org.openlmis.core.model.repository.InventoryRepository;
import org.openlmis.core.model.repository.ReportTypeFormRepository;
import org.openlmis.core.model.repository.RnrFormRepository;
import org.openlmis.core.model.repository.StockMovementRepository;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.model.repository.SyncErrorsRepository;
import org.openlmis.core.model.service.RequisitionPeriodService;
import org.openlmis.core.utils.Constants;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.viewmodel.RnRFormViewModel;
import roboguice.RoboGuice;

@RunWith(LMISTestRunner.class)
@SuppressWarnings("PMD")
public class RnRFormListPresenterTest {

  RnRFormListPresenter presenter;
  private List<RnRForm> rnRForms;
  SyncErrorsRepository syncErrorsRepository;
  private RnrFormRepository rnrFormRepository;
  private StockMovementRepository stockMovementRepository;
  private StockRepository stockRepository;
  private ReportTypeFormRepository reportTypeFormRepository;
  private SharedPreferenceMgr sharedPreferenceMgr;
  private Period periodFebToMar;
  private Period periodMarToApl;
  private Period periodAplToMay;

  private RequisitionPeriodService requisitionPeriodService;
  private InventoryRepository inventoryRepository;
  private RnRForm rnRForm1;
  private RnRForm rnRForm2;
  private RnRForm rnRForm3;
  private Program program;

  @Before
  public void setUp() throws LMISException {
    rnrFormRepository = mock(RnrFormRepository.class);
    stockRepository = mock(StockRepository.class);
    stockMovementRepository = mock(StockMovementRepository.class);
    syncErrorsRepository = mock(SyncErrorsRepository.class);
    sharedPreferenceMgr = mock(SharedPreferenceMgr.class);
    requisitionPeriodService = mock(RequisitionPeriodService.class);
    inventoryRepository = mock(InventoryRepository.class);
    reportTypeFormRepository = mock(ReportTypeFormRepository.class);

    Application application = ApplicationProvider.getApplicationContext();
    RoboGuice.overrideApplicationInjector(application, new AbstractModule() {
      @Override
      protected void configure() {
        bind(SyncErrorsRepository.class).toInstance(syncErrorsRepository);
        bind(RnrFormRepository.class).toInstance(rnrFormRepository);
        bind(StockRepository.class).toInstance(stockRepository);
        bind(StockMovementRepository.class).toInstance(stockMovementRepository);
        bind(SharedPreferenceMgr.class).toInstance(sharedPreferenceMgr);
        bind(RequisitionPeriodService.class).toInstance(requisitionPeriodService);
        bind(InventoryRepository.class).toInstance(inventoryRepository);
        bind(ReportTypeFormRepository.class).toInstance(reportTypeFormRepository);
      }
    });

    presenter = RoboGuice.getInjector(application)
        .getInstance(RnRFormListPresenter.class);
    rnRForms = createRnRForms();
    program = new ProgramBuilder().setProgramCode("VIA").build();

    periodFebToMar = new Period(
        new DateTime(DateUtil.parseString("2016-02-18", DateUtil.DB_DATE_FORMAT)),
        new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    periodMarToApl = new Period(
        new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)),
        new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    periodAplToMay = new Period(
        new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)),
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)));

    rnRForm1 = createRnrFormByPeriod(Status.AUTHORIZED, periodFebToMar.getBegin().toDate(),
        periodFebToMar.getEnd().toDate(), program);
    rnRForm2 = createRnrFormByPeriod(Status.AUTHORIZED, periodMarToApl.getBegin().toDate(),
        periodMarToApl.getEnd().toDate(), program);
    rnRForm3 = createRnrFormByPeriod(Status.DRAFT, periodAplToMay.getBegin().toDate(),
        periodAplToMay.getEnd().toDate(), program);

    ReportTypeForm reportTypeForm = new ReportTypeFormBuilder().
        setActive(true).
        setCode(Constants.VIA_REPORT).
        setName(Constants.VIA_PROGRAM_CODE).
        setStartTime(
            new DateTime(DateUtil.parseString("2016-02-5", DateUtil.DB_DATE_FORMAT)).toDate())
        .build();
    when(reportTypeFormRepository.queryByCode(Constants.VIA_REPORT)).thenReturn(reportTypeForm);

  }

  @Test
  public void shouldBuildFormListViewModels() throws LMISException {
    presenter.setProgramCode("MMIA");
    Collections.reverse(rnRForms);
    when(rnrFormRepository.listInclude(any(RnRForm.Emergency.class), anyString(), anyObject()))
        .thenReturn(rnRForms);
    when(syncErrorsRepository.getBySyncTypeAndObjectId(any(SyncType.class), anyLong()))
        .thenReturn(Arrays.asList(new SyncError("Error1", SyncType.RNR_FORM, 1),
            new SyncError("Error2", SyncType.RNR_FORM, 1)));

    ReportTypeForm reportTypeForm = new ReportTypeFormBuilder().
        setActive(true).
        setCode(Constants.MMIA_REPORT).
        setName(Constants.MMIA_PROGRAM_CODE).
        setStartTime(
            new DateTime(DateUtil.parseString("2016-02-5", DateUtil.DB_DATE_FORMAT)).toDate())
        .build();
    when(reportTypeFormRepository.queryByCode(Constants.MMIA_REPORT)).thenReturn(reportTypeForm);
    List<RnRFormViewModel> resultViewModels = presenter.buildFormListViewModels();
    assertThat(resultViewModels.size()).isEqualTo(3);
    assertThat(resultViewModels.get(0).getSyncServerErrorMessage()).isEqualTo("Error2");
  }

  protected RnRForm createRnrFormByPeriod(Status status, Date periodBegin, Date periodEnd,
      Program program) {
    RnRForm rnRForm1 = new RnRForm();
    rnRForm1.setPeriodBegin(periodBegin);
    rnRForm1.setPeriodEnd(periodEnd);
    rnRForm1.setStatus(status);
    rnRForm1.setProgram(program);
    return rnRForm1;
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryAnd2HistoricalViewModelsWhenThereIsNoMissedRnrAndTwoRnrDoneAndItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, program.getProgramCode(), getTypeForm(presenter)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryForNoMovementsAnd2HistoricalViewModelsWhenThereIsNoMissedRnrAndTwoRnrDoneAndItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, program.getProgramCode(), getTypeForm(presenter)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1CanDoInventoryAnd2HistoricalViewModelsAndSortCorrectlyWhenThereIsNoMissedRnrAndTwoRnrDoneAndItIs20May()
      throws Exception {
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, program.getProgramCode(), getTypeForm(presenter)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(new ArrayList<>());
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryForNoMovementsAnd2HistoricalViewModelsAndSortCorrectlyWhenThereIsNoMissedRnrAndTwoRnrDoneAndItIs20May()
      throws Exception {
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, program.getProgramCode(), getTypeForm(presenter)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(new ArrayList<>());
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1InventoryDoneAnd2HistoricalViewModelsAndSortCorrectlyWhenThereIsNoMissedRnrAndTwoRnrDoneAndInventoryDoneForThisPeriod()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, program.getProgramCode(), getTypeForm(presenter)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(newArrayList(new Inventory()));
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_INVENTORY_DONE);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryForNoMovementsAnd2HistoricalViewModelsAndSortCorrectlyWhenThereIsNoMissedRnrAndTwoRnrDoneAndInventoryDoneForThisPeriod()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(RnRForm.Emergency.YES, program.getProgramCode(), getTypeForm(presenter)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(newArrayList(new Inventory()));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1CreatedNotCompletedAndTwoHistoricalViewModelsAndSortCorrectlyWhenThereIsNoMissedRnrAndTwoRnrDoneAndItIs20May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-20", DateUtil.DB_DATE_FORMAT)).getMillis());

    RnRForm rnRForm3 = createRnrFormByPeriod(Status.DRAFT,
        periodAplToMay.getBegin().toDate(), periodAplToMay.getEnd().toDate(), program);
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository
        .listInclude(any(RnRForm.Emergency.class), anyString(), any(ReportTypeForm.class)))
        .thenReturn(newArrayList(rnRForm1, rnRForm2, rnRForm3));
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null, typeForm))
        .thenReturn(periodAplToMay);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_DRAFT);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth()).isEqualTo(periodMarToApl.getEnd());
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth()).isEqualTo(periodFebToMar.getEnd());
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryViewModelsWhenThereIsNoMissedRnrAndThereIsNoHistoricalRnrAndItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository.list()).thenReturn(new ArrayList<>());
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryForNoMovementsViewModelsWhenThereIsNoMissedRnrAndThereIsNoHistoricalRnrAndItIs17May()
      throws Exception {
    // given
    String programCode = "VIA";
    presenter.setProgramCode(programCode);

    ReportTypeForm typeForm = new ReportTypeForm();
    typeForm.setActive(true);

    when(reportTypeFormRepository.queryByCode(programCode)).thenReturn(typeForm);
    when(rnrFormRepository.listInclude(Emergency.YES, programCode, typeForm)).thenReturn(new ArrayList<>());
    when(requisitionPeriodService.hasMissedPeriod(programCode, null))
        .thenReturn(false);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(programCode, typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(programCode, null))
        .thenReturn(periodAplToMay);
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(programCode)).thenReturn(null);
    // when
    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();
    // then
    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1CanDoInventoryViewModelsWhenThereIsNoMissedRnrAndThereIsNoHistoricalRnrAndItIs18May()
      throws Exception {
    // given
    String programCode = "VIA";
    presenter.setProgramCode(programCode);

    ReportTypeForm typeForm = new ReportTypeForm();
    typeForm.setActive(true);

    when(reportTypeFormRepository.queryByCode(programCode)).thenReturn(typeForm);
    when(rnrFormRepository.listInclude(Emergency.YES, programCode, typeForm)).thenReturn(new ArrayList<>());
    when(requisitionPeriodService.hasMissedPeriod(programCode, null))
        .thenReturn(false);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(programCode, typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(programCode, null))
        .thenReturn(periodAplToMay);

    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT));

    when(stockMovementRepository.queryMalariaStockMovementDates()).thenReturn(newArrayList("2024-07-08"));
    when(inventoryRepository.queryPeriodInventory(periodAplToMay)).thenReturn(null);
    // when
    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();
    // then
    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1CanNotDoInventoryForNoMovementsViewModelsWhenThereIsNoMissedRnrAndThereIsNoHistoricalRnrAndItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository.list()).thenReturn(new ArrayList<>());
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(new ArrayList<>());

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1InventoryDoneViewModelsWhenThereIsNoMissedRnrAndThereIsNoHistoricalRnrAndItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository.list()).thenReturn(new ArrayList<>());
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(newArrayList(new Inventory()));
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(), typeForm))
        .thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_INVENTORY_DONE);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1InventoryCanNotDoneForNoMovementsViewModelsWhenThereIsNoMissedRnrAndThereIsNoHistoricalRnrAndItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository.list()).thenReturn(new ArrayList<>());
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(newArrayList(new Inventory()));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(), typeForm))
        .thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1CreatedNotCompletedViewModelsWhenThereIsNoMissedRnrAndThereIsOneDraftRnrAndItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(any(RnRForm.Emergency.class), anyString(), any(ReportTypeForm.class)))
        .thenReturn(newArrayList(rnRForm3));
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodAplToMay);
    when(inventoryRepository.queryPeriodInventory(periodAplToMay))
        .thenReturn(newArrayList(new Inventory()));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(false);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(1);
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_DRAFT);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth()).isEqualTo(periodAplToMay.getEnd());
  }

  @Test
  public void shouldGenerate1MissedPeriodAnd1HistoricalRnRViewModelsWhenThereIsOneRnrDoneAndMissed2RnrAndThereIsNoInventoryIsDoneForTheFirstMissedRnrItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(any(RnRForm.Emergency.class), anyString(), any(ReportTypeForm.class)))
        .thenReturn(newArrayList(rnRForm1));
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodMarToApl);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode()))
        .thenReturn(1);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(new ArrayList<>());
    when(stockMovementRepository.queryEarliestStockMovementDateByProgram(program.getProgramCode())).thenReturn(
        DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT));
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(), typeForm))
        .thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(2);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNCOMPLETE_INVENTORY_IN_CURRENT_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
  }

  @Test
  public void shouldGenerate1MissedPeriodCanNotDoInventoryForNoMovementsAnd1HistoricalRnRViewModelsWhenThereIsOneRnrDoneAndMissed2RnrAndThereIsNoInventoryIsDoneForTheFirstMissedRnrItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());

    when(rnrFormRepository
        .listInclude(any(RnRForm.Emergency.class), anyString(), any(ReportTypeForm.class)))
        .thenReturn(newArrayList(rnRForm1));
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodMarToApl);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode())).thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode()))
        .thenReturn(1);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(new ArrayList<>());
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(), typeForm))
        .thenReturn(true);

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(2);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType())
        .isEqualTo(RnRFormViewModel.TYPE_CANNOT_DO_MONTHLY_INVENTORY_NO_MOVEMENTS);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
  }

  @Test
  public void shouldGenerate1CreateRnrFormAnd1MissedPeriodAnd1HistoricalViewModelsWhenThereIsNoRnrInDBAndMissed2RnrAndThereInventoryIsDoneForTheFirstMissedRnrItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());
    ReportTypeForm typeForm = getTypeForm(presenter);
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(rnrFormRepository.listInclude(RnRForm.Emergency.YES, program.getProgramCode(), typeForm))
        .thenReturn(newArrayList(rnRForm1));
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodMarToApl);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode(), typeForm))
        .thenReturn(1);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(newArrayList(new Inventory()));

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-21", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_INVENTORY_DONE);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
  }

  @Test
  public void shouldGenerate1SelectPeriodAnd2MissedPeriodAnd1HistoricalViewModelsWhenThereIsNoRnrInDBAndMissed3RnrAndItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository.listInclude(RnRForm.Emergency.YES, program.getProgramCode(), typeForm))
        .thenReturn(newArrayList(rnRForm1));
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodMarToApl);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode(), typeForm))
        .thenReturn(2);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(new ArrayList<>());

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(4);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-21", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-21", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(3).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(3).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
  }

  @Test
  public void shouldGenerate2MissedPeriodAnd1HistoricalAnd1CreatedNotCpmletedViewModelsWhenThereIs1RnrDoneAnd1DraftRnrAndMissed2RnrAndItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());
    Period periodMayToJun = new Period(
        new DateTime(DateUtil.parseString("2016-05-21", DateUtil.DB_DATE_FORMAT)),
        new DateTime(DateUtil.parseString("2016-06-21", DateUtil.DB_DATE_FORMAT)));
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository
        .listInclude(any(RnRForm.Emergency.class), anyString(), any(ReportTypeForm.class)))
        .thenReturn(newArrayList(rnRForm2, rnRForm3));
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodMayToJun);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode(), typeForm))
        .thenReturn(1);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-05-21", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(new ArrayList<>());

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(4);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-06-21", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-21", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(2).getType()).isEqualTo(RnRFormViewModel.TYPE_DRAFT);
    assertThat(rnRFormViewModels.get(3).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(3).getType())
        .isEqualTo(RnRFormViewModel.TYPE_UNSYNCED_HISTORICAL);
  }

  @Test
  public void shouldGenerate1SelectPeriodAnd2MissedPeriodViewModelsWhenThereIsNoRnrInDBAndMissed3RnrAndThereIsNoInventoryIsDoneForTheFirstMissedRnrItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository.listInclude(RnRForm.Emergency.NO, program.getProgramCode(), typeForm))
        .thenReturn(new ArrayList<>());
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodFebToMar);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode(), typeForm))
        .thenReturn(2);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(new ArrayList<>());

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(2).getType())
        .isEqualTo(RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD);
  }

  @Test
  public void shouldGenerate1CreateRnrFormAnd2MissedPeriodViewModelsWhenThereIsNoRnrInDBAndMissed3RnrAndThereInventoryIsDoneForTheFirstMissedRnrItIs17May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-17", DateUtil.DB_DATE_FORMAT)).getMillis());
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository.listInclude(RnRForm.Emergency.NO, program.getProgramCode(), typeForm))
        .thenReturn(new ArrayList<>());
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(
        program.getProgramCode(), typeForm)
    ).thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodFebToMar);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode(), typeForm))
        .thenReturn(2);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(newArrayList(new Inventory()));

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(3);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(2).getType()).isEqualTo(RnRFormViewModel.TYPE_INVENTORY_DONE);
  }

  @Test
  public void shouldGenerate1SelectPeriodAnd3MissedPeriodViewModelsWhenThereIsNoRnrInDBAndMissed3RnrAndThereIsNoInventoryIsDoneForTheFirstMissedRnrItIs18May()
      throws Exception {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    presenter.setProgramCode(program.getProgramCode());
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());
    ReportTypeForm typeForm = getTypeForm(presenter);

    when(rnrFormRepository.listInclude(RnRForm.Emergency.NO, program.getProgramCode(), typeForm))
        .thenReturn(new ArrayList<>());
    when(requisitionPeriodService.isAllRnrFormInDBCompletedOrNoRnrFormInDB(program.getProgramCode(),
        typeForm)).thenReturn(true);
    when(requisitionPeriodService.generateNextPeriod(program.getProgramCode(), null))
        .thenReturn(periodFebToMar);
    when(requisitionPeriodService.hasMissedPeriod(program.getProgramCode(), typeForm))
        .thenReturn(true);
    when(requisitionPeriodService.getMissedPeriodOffsetMonth(program.getProgramCode(), typeForm))
        .thenReturn(3);
    when(requisitionPeriodService.getCurrentPeriodBeginDate())
        .thenReturn(new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)));
    when(inventoryRepository.queryPeriodInventory(any(Period.class)))
        .thenReturn(new ArrayList<>());

    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();

    assertThat(rnRFormViewModels.size()).isEqualTo(4);
    assertThat(rnRFormViewModels.get(0).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-06-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(0).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(1).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(1).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(2).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-04-18", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(2).getType()).isEqualTo(RnRFormViewModel.TYPE_MISSED_PERIOD);
    assertThat(rnRFormViewModels.get(3).getPeriodEndMonth())
        .isEqualTo(new DateTime(DateUtil.parseString("2016-03-20", DateUtil.DB_DATE_FORMAT)));
    assertThat(rnRFormViewModels.get(3).getType())
        .isEqualTo(RnRFormViewModel.TYPE_FIRST_MISSED_PERIOD);
  }

  @Test
  public void buildFormListViewModels_shouldNotGenerateWhenTypeFormIsActiveAndExistsRejectedRnrForms()
      throws LMISException {
    Program program = new ProgramBuilder().setProgramCode("VIA").build();
    String programCode = program.getProgramCode();
    presenter.setProgramCode(programCode);
    LMISTestApp.getInstance().setCurrentTimeMillis(
        new DateTime(DateUtil.parseString("2016-05-18", DateUtil.DB_DATE_FORMAT)).getMillis());

    ReportTypeForm typeForm = ReportTypeForm.builder().active(true).build();

    when(reportTypeFormRepository.queryByCode(programCode)).thenReturn(typeForm);
    when(rnrFormRepository.listInclude(RnRForm.Emergency.YES, programCode, typeForm))
        .thenReturn(new ArrayList<>());
    when(requisitionPeriodService.hasMissedPeriod(programCode, typeForm))
        .thenReturn(false);
    when(requisitionPeriodService.generateNextPeriod(programCode, null))
        .thenReturn(periodFebToMar);
    RnRForm rejectedRnRForm = new RnRFormBuilder().setStatus(Status.REJECTED).build();
    when(rnrFormRepository.listInclude(RnRForm.Emergency.NO, programCode, typeForm))
        .thenReturn(newArrayList(rejectedRnRForm));
    // when
    List<RnRFormViewModel> rnRFormViewModels = presenter.buildFormListViewModels();
    // then
    assertThat(rnRFormViewModels.size()).isEqualTo(0);
  }

  private List<RnRForm> createRnRForms() {
    return newArrayList(createRnRForm(Status.DRAFT),
        createRnRForm(Status.AUTHORIZED), createRnRForm(Status.AUTHORIZED));
  }

  private ReportTypeForm getTypeForm(RnRFormListPresenter presenter) throws LMISException {
    return reportTypeFormRepository.queryByCode(programCode2ReportType(presenter.programCode));
  }

  public String programCode2ReportType(final String programCode) {
    for (Constants.Program program : Constants.PROGRAMS) {
      if (program.getCode().equals(programCode)) {
        return program.getReportType();
      }
    }
    return Constants.Program.VIA_PROGRAM.getReportType();
  }

  private RnRForm createRnRForm(Status status) {
    Program program = new Program();
    program.setProgramCode("MMIA");
    program.setProgramName("MMIA");

    RnRForm rnRForm = RnRForm.init(program, DateUtil.today());
    rnRForm.setId(1L);
    rnRForm.setStatus(status);
    rnRForm.setSynced(true);
    return rnRForm;
  }
}