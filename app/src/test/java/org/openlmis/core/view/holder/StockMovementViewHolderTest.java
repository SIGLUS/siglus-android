package org.openlmis.core.view.holder;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.exceptions.LMISException;
import org.openlmis.core.manager.MovementReasonManager;
import org.openlmis.core.model.StockCard;
import org.openlmis.core.model.StockMovementItem;
import org.openlmis.core.model.builder.StockCardBuilder;
import org.openlmis.core.model.builder.StockMovementItemBuilder;
import org.openlmis.core.model.builder.StockMovementViewModelBuilder;
import org.openlmis.core.model.repository.StockRepository;
import org.openlmis.core.utils.DateUtil;
import org.openlmis.core.view.adapter.StockMovementAdapter;
import org.openlmis.core.view.viewmodel.StockMovementViewModel;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowDatePickerDialog;
import org.robolectric.shadows.ShadowToast;

import java.text.ParseException;
import java.util.Date;

import roboguice.RoboGuice;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.roboguice.shaded.goole.common.collect.Lists.newArrayList;

@RunWith(LMISTestRunner.class)
public class StockMovementViewHolderTest {

    private StockMovementViewHolder viewHolder;
    private StockMovementAdapter.MovementChangedListener mockedListener;
    private StockMovementViewModel viewModel;
    private StockCard stockCard;
    private View itemView;

    @Before
    public void setUp() throws LMISException, ParseException {
        itemView = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.item_stock_movement, null, false);
        mockedListener = mock(StockMovementAdapter.MovementChangedListener.class);
        viewHolder = new StockMovementViewHolder(itemView, mockedListener);

        viewModel = new StockMovementViewModelBuilder()
                .withMovementDate("2015-11-11")
                .withDocumentNo("12345")
                .withNegativeAdjustment(null)
                .withPositiveAdjustment(null)
                .withIssued("30")
                .withReceived(null)
                .withStockExistence("70")
                .withIsDraft(false)
                .withMovementReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "ISSUE_1", "issue description")).build();

        StockRepository stockRepository = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(StockRepository.class);

        stockCard = StockCardBuilder.saveStockCardWithOneMovement(stockRepository);
    }

    @Test
    public void shouldPopulateTextDataWhenPopulatingData() {
        viewHolder.populate(viewModel, stockCard);

        assertEquals("12345", viewHolder.etDocumentNo.getText().toString());
        assertEquals("2015-11-11", viewHolder.txMovementDate.getText().toString());
        assertEquals("", viewHolder.etReceived.getText().toString());
        assertEquals("", viewHolder.etNegativeAdjustment.getText().toString());
        assertEquals("", viewHolder.etPositiveAdjustment.getText().toString());
        assertEquals("30", viewHolder.etIssued.getText().toString());
        assertEquals("70", viewHolder.txStockExistence.getText().toString());
        assertEquals("issue description", viewHolder.txReason.getText().toString());
    }

    @Test
    public void shouldDisableLineWhenPopulatingData() {
        viewHolder.populate(viewModel, stockCard);

        assertFalse(viewHolder.etDocumentNo.isEnabled());
        assertFalse(viewHolder.etReceived.isEnabled());
        assertFalse(viewHolder.etNegativeAdjustment.isEnabled());
        assertFalse(viewHolder.etPositiveAdjustment.isEnabled());
        assertFalse(viewHolder.etIssued.isEnabled());
    }

    @Test
    public void shouldHideUnderline() {
        viewHolder.populate(viewModel, stockCard);

        assertNull(viewHolder.etIssued.getBackground());
        assertNull(viewHolder.etPositiveAdjustment.getBackground());
        assertNull(viewHolder.etNegativeAdjustment.getBackground());
        assertNull(viewHolder.etReceived.getBackground());
        assertNull(viewHolder.etDocumentNo.getBackground());
    }

    @Test
    public void shouldSetFontColorRedIfNotIssueAdjustment() {
        viewModel.setReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.NEGATIVE_ADJUST, "negative_adjust", "negative_adjust description"));
        viewHolder.populate(viewModel, stockCard);

        int red = RuntimeEnvironment.application.getResources().getColor(R.color.color_red);

        assertEquals(red, viewHolder.txMovementDate.getCurrentTextColor());
        assertEquals(red, viewHolder.txReason.getCurrentTextColor());
        assertEquals(red, viewHolder.etDocumentNo.getCurrentTextColor());
        assertEquals(red, viewHolder.etReceived.getCurrentTextColor());
        assertEquals(red, viewHolder.etPositiveAdjustment.getCurrentTextColor());
        assertEquals(red, viewHolder.etNegativeAdjustment.getCurrentTextColor());
        assertEquals(red, viewHolder.txStockExistence.getCurrentTextColor());
    }

    @Test
    public void shouldSetFontColorBlackIfIssueAdjustment() {
        viewModel.setReason(new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "ISSUE_1", "issue description"));
        viewHolder.populate(viewModel, stockCard);

        int black = RuntimeEnvironment.application.getResources().getColor(R.color.color_black);

        assertEquals(black, viewHolder.txMovementDate.getCurrentTextColor());
        assertEquals(black, viewHolder.txReason.getCurrentTextColor());
        assertEquals(black, viewHolder.etDocumentNo.getCurrentTextColor());
        assertEquals(black, viewHolder.etReceived.getCurrentTextColor());
        assertEquals(black, viewHolder.etPositiveAdjustment.getCurrentTextColor());
        assertEquals(black, viewHolder.etNegativeAdjustment.getCurrentTextColor());
        assertEquals(black, viewHolder.txStockExistence.getCurrentTextColor());
    }

    @Test
    public void shouldShowMovementTypeDialogOnClick() {
        viewModel.setDraft(true);
        viewHolder.populate(viewModel, stockCard);

        viewHolder.txReason.performClick();

        AlertDialog typeDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(typeDialog);
    }

    @Test
    public void shouldSetReasonAndDateOnComplete() {
        MovementReasonManager.MovementReason reason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.RECEIVE, "DON", "Donations");
        String today = DateUtil.formatDate(new Date());
        viewHolder.populate(viewModel, stockCard);
        viewHolder.txMovementDate.setText("");
        viewHolder.etIssued.setText("100");

        StockMovementViewHolder.MovementSelectListener listener = viewHolder.new MovementSelectListener(viewModel);
        listener.onComplete(reason);

        assertEquals(reason.getDescription(), viewHolder.txReason.getText().toString());
        assertEquals(reason.getDescription(), viewModel.getReason().getDescription());
        assertEquals(today, viewHolder.txMovementDate.getText().toString());
        assertEquals(today, viewModel.getMovementDate());
        assertTrue(viewHolder.etReceived.isEnabled());
        assertEquals(viewHolder.etIssued.getText().toString(), "");
        verify(mockedListener).movementChange();
    }

    @Test
    public void shouldShowMovementDateDialogOnClick() {
        viewModel.setDraft(true);
        viewHolder.populate(viewModel, stockCard);

        viewHolder.txMovementDate.performClick();

        DatePickerDialog datePickerDialog = (DatePickerDialog) ShadowDatePickerDialog.getLatestDialog();
        assertNotNull(datePickerDialog);
    }

    @Test
    public void shouldValidateMovementDateOnSelectionAndShowToastIfInvalid() throws ParseException, LMISException {
        viewHolder.populate(viewModel, stockCard);

        StockMovementViewHolder.MovementDateListener movementDateListener = viewHolder.new MovementDateListener(viewModel, new Date());
        movementDateListener.onDateSet(mock(DatePicker.class), 2015, 11, 10);
        assertNotNull(ShadowToast.getLatestToast());
    }

    @Test
    public void shouldValidateMovementDateOnSelectionAnd() throws ParseException, LMISException {
        viewHolder.populate(viewModel, stockCard);

        StockMovementViewHolder.MovementDateListener movementDateListener = viewHolder.new MovementDateListener(viewModel, DateUtil.parseString("11-11-2015", "MM-dd-YYYY"));
        movementDateListener.onDateSet(mock(DatePicker.class), 2015, 10, 15);
        assertEquals("15 Nov 2015", viewHolder.txMovementDate.getText().toString());
        assertEquals("15 Nov 2015", viewModel.getMovementDate());
        assertNull(ShadowToast.getLatestToast());
    }

    @Test
    public void shouldOnlyEnableCurrentSelectedEditTextWhenChoseMovementType() throws ParseException, LMISException {

        viewModel.setDraft(true);
        viewHolder.populate(viewModel, stockCard);
        viewHolder.txMovementDate.setText("");

        StockMovementViewHolder.MovementSelectListener listener = viewHolder.new MovementSelectListener(viewModel);

        MovementReasonManager.MovementReason receiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.RECEIVE, "DON", "Donations");
        listener.onComplete(receiveReason);

        assertTrue(viewHolder.etReceived.isEnabled());
        assertFalse(viewHolder.etPositiveAdjustment.isEnabled());
        assertFalse(viewHolder.etNegativeAdjustment.isEnabled());
        assertFalse(viewHolder.etIssued.isEnabled());
        assertFalse(viewHolder.etRequested.isEnabled());

        MovementReasonManager.MovementReason positiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.POSITIVE_ADJUST, "POSITIVE", "BOUGHT");
        listener.onComplete(positiveReason);

        assertFalse(viewHolder.etReceived.isEnabled());
        assertTrue(viewHolder.etPositiveAdjustment.isEnabled());
        assertFalse(viewHolder.etNegativeAdjustment.isEnabled());
        assertFalse(viewHolder.etIssued.isEnabled());
        assertFalse(viewHolder.etRequested.isEnabled());
        assertFalse(viewHolder.etRequested.isEnabled());
    }

    @Test
    public void shouldEnableIssueAndRequetedEditTextWhenChoseIssueMovementType() throws ParseException, LMISException {

        viewModel.setDraft(true);
        viewHolder.populate(viewModel, stockCard);
        viewHolder.txMovementDate.setText("");

        StockMovementViewHolder.MovementSelectListener listener = viewHolder.new MovementSelectListener(viewModel);

        MovementReasonManager.MovementReason receiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "ISSUE_1", "issue description");

        listener.onComplete(receiveReason);

        assertFalse(viewHolder.etReceived.isEnabled());
        assertFalse(viewHolder.etPositiveAdjustment.isEnabled());
        assertFalse(viewHolder.etNegativeAdjustment.isEnabled());
        assertTrue(viewHolder.etIssued.isEnabled());
        assertTrue(viewHolder.etRequested.isEnabled());
    }

    @Test
    public void shouldEnableMovementTypeAndReasonIfModelIsDraft() {
        viewModel.setDraft(true);
        viewHolder.populate(viewModel, stockCard);

        assertTrue(viewHolder.txMovementDate.isEnabled());
        assertTrue(viewHolder.txReason.isEnabled());
    }

    @Test
    public void shouldSetValueAfterTextChange() {
        viewHolder.populate(viewModel, stockCard);

        viewHolder.etIssued.setText("30");
        assertEquals("30", viewModel.getIssued());
        assertEquals("70", viewModel.getStockExistence());
        assertEquals("70", viewHolder.txStockExistence.getText().toString());
    }

    @Test
    public void shouldEnableIssueEditTextWhenModelWithIssueType() {
        viewModel.setDraft(true);
        viewHolder.populate(viewModel, stockCard);

        assertTrue(viewHolder.etIssued.isEnabled());
    }

    @Test
    public void shouldEnableReceivedEditTextWhenModelWithReceivedType() {
        MovementReasonManager.MovementReason receivedReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.RECEIVE, "RECEIVE_1", "receive description");
        viewModel.setDraft(true);
        viewModel.setReason(receivedReason);
        viewHolder.populate(viewModel, stockCard);

        assertTrue(viewHolder.etReceived.isEnabled());
    }

    @Test
    public void shouldEnableNegativeAdjustmentEditTextWhenModelWithNegativeAdjustmentType() {
        MovementReasonManager.MovementReason negativeReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.NEGATIVE_ADJUST, "NEGATIVE_1", "negative adjustment description");
        viewModel.setDraft(true);
        viewModel.setReason(negativeReason);
        viewHolder.populate(viewModel, stockCard);

        assertTrue(viewHolder.etNegativeAdjustment.isEnabled());
    }


    @Test
    public void shouldClearDocumentNumberWhenReselectReason() {
        viewHolder.populate(viewModel, stockCard);
        viewHolder.etDocumentNo.setText("888");

        StockMovementViewHolder.MovementSelectListener listener = viewHolder.new MovementSelectListener(viewModel);
        MovementReasonManager.MovementReason receiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.RECEIVE, "DON", "Donations");
        listener.onComplete(receiveReason);

        assertThat(viewHolder.etDocumentNo.getText().toString(), is(""));
    }

    @Test
    public void shouldClearInputWhenReSelectSameReason() {
        viewHolder.populate(viewModel, stockCard);
        StockMovementViewHolder.MovementSelectListener listener = viewHolder.new MovementSelectListener(viewModel);
        MovementReasonManager.MovementReason receiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.RECEIVE, "DON", "Donations");
        listener.onComplete(receiveReason);
        viewHolder.etReceived.setText("10");

        listener.onComplete(receiveReason);

        assertThat(viewHolder.etReceived.getText().toString(), is(""));
    }

    @Test
    public void shouldEnablePositiveAdjustmentEditTextWhenModelWithPositiveAdjustmentType() {
        MovementReasonManager.MovementReason positiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.POSITIVE_ADJUST, "POSITIVE_1", "positive adjustment description");
        viewModel.setDraft(true);
        viewModel.setReason(positiveReason);
        viewHolder.populate(viewModel, stockCard);

        assertTrue(viewHolder.etPositiveAdjustment.isEnabled());
    }

    @Test
    public void shouldNotClearInputWhenRotate() {
        viewHolder.populate(viewModel, stockCard);
        MovementReasonManager.MovementReason positiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.POSITIVE_ADJUST, "POSITIVE_1", "positive adjustment description");
        viewModel.setDraft(true);
        viewModel.setReason(positiveReason);
        viewHolder.etPositiveAdjustment.setText("10");

        viewHolder.populate(viewModel, stockCard);

        assertThat(viewHolder.etPositiveAdjustment.getText().toString(), is("10"));
    }

    @Test
    public void shouldResetTxReasonValueWhenReuseViewModel() {
        viewHolder.populate(viewModel, stockCard);

        assertEquals(viewHolder.txReason.getText().toString(), "issue description");

        viewModel.setDraft(true);
        viewModel.setReason(null);
        viewHolder.populate(viewModel, stockCard);

        assertEquals(viewHolder.txReason.getText().toString(), "");
    }

    @Test
    public void shouldResetTextColorWhenReselectReason() {
        viewHolder.populate(viewModel, stockCard);

        StockMovementViewHolder.MovementSelectListener listener = viewHolder.new MovementSelectListener(viewModel);
        MovementReasonManager.MovementReason receiveReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.POSITIVE_ADJUST, "INVENTORY_POSITIVE", "Inventory description...");
        listener.onComplete(receiveReason);

        assertThat(viewHolder.txReason.getCurrentTextColor(), is(RuntimeEnvironment.application.getResources().getColor(R.color.color_red)));

        MovementReasonManager.MovementReason issueReason = new MovementReasonManager.MovementReason(StockMovementItem.MovementType.ISSUE, "PAV", "PAV Description");
        listener.onComplete(issueReason);

        assertThat(viewHolder.txReason.getCurrentTextColor(), is(RuntimeEnvironment.application.getResources().getColor(R.color.color_black)));
    }

    @Test
    public void shouldGetLatestMovementDateAsThePreviousMovementDate() throws ParseException {
        StockCard stockCard = new StockCard();
        StockMovementItem stockMovementItem1 = new StockMovementItemBuilder().withMovementDate("2015-10-10").build();
        StockMovementItem stockMovementItem2 = new StockMovementItemBuilder().withMovementDate("2015-11-12").build();
        StockMovementItem stockMovementItem3 = new StockMovementItemBuilder().withMovementDate("2015-09-10").build();
        stockCard.setStockMovementItemsWrapper(newArrayList(stockMovementItem1, stockMovementItem2, stockMovementItem3));

        Date previousMovementDate = viewHolder.getPreviousMovementDate(stockCard);
        assertThat(DateUtil.formatDate(previousMovementDate), is("12 Nov 2015"));
    }

}