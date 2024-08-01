package org.openlmis.core.view.widget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.Program;
import org.openlmis.core.model.RnRForm;
import org.openlmis.core.model.RnrFormItem;
import androidx.test.core.app.ApplicationProvider;

@RunWith(LMISTestRunner.class)
public class MMTBProductListTest {

  private MMTBRnrFormProductList mmtbProductListTest;
  private ArrayList<RnrFormItem> dataList;
  private RnrFormItem formItem;

  @Before
  public void setUp() {
    mmtbProductListTest = new MMTBRnrFormProductList(ApplicationProvider.getApplicationContext());
    mmtbProductListTest.setLayoutParams(new ViewGroup.MarginLayoutParams(100, 100));
    dataList = new ArrayList<>();
    formItem = new RnrFormItem();
    Product product = new Product();
    product.setId(1L);
    product.setPrimaryName("product");
    product.setCode("08S17");
    formItem.setProduct(product);
    formItem.setIsCustomAmount(false);
    formItem.setForm(RnRForm.init(Program.builder().programCode(Program.MMTB_CODE).build(), new Date()));
    dataList.add(formItem);
  }

  @Test
  public void shouldShowCorrectData() {
    // when
    mmtbProductListTest.setData(dataList);

    // then
    ViewGroup leftViewGroup = mmtbProductListTest.findViewById(R.id.rnr_from_list_product_name);
    String tvProductCode11 = ((TextView) ((LinearLayout) leftViewGroup.getChildAt(0)).getChildAt(0))
        .getText().toString();
    String tvProductName12 = ((TextView) ((LinearLayout) leftViewGroup.getChildAt(0)).getChildAt(1))
        .getText().toString();
    assertThat(tvProductCode11, is(formItem.getProduct().getCode()));
    assertThat(tvProductName12, is(formItem.getProduct().getPrimaryName()));
  }

  @Test
  public void initialAmountShouldEnableWhenIsCustom() {
    // given
    formItem.setIsCustomAmount(true);

    // when
    mmtbProductListTest.setData(dataList);

    // then
    ViewGroup rightViewGroup = mmtbProductListTest.findViewById(R.id.rnr_from_list);
    View initialAmount = ((LinearLayout) rightViewGroup.getChildAt(0)).findViewById(R.id.et_initial_amount);
    assertThat(initialAmount.isEnabled(), is(true));
  }

  @Test
  public void shouldNotCompleteWhenInputNothing() {
    // when
    mmtbProductListTest.setData(dataList);

    // then
    assertThat(mmtbProductListTest.isCompleted(), is(false));
  }

  @Test
  public void shouldCompleteWhenFillAllEditText() {
    // when
    mmtbProductListTest.setData(dataList);
    ((EditText) mmtbProductListTest.findViewById(R.id.et_initial_amount)).setText("1");
    ((EditText) mmtbProductListTest.findViewById(R.id.et_issued)).setText("1");
    ((EditText) mmtbProductListTest.findViewById(R.id.et_adjustment)).setText("1");
    ((EditText) mmtbProductListTest.findViewById(R.id.et_inventory)).setText("1");

    // then
    assertThat(mmtbProductListTest.isCompleted(), is(true));
  }
}

