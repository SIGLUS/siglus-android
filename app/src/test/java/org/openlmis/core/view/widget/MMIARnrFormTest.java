package org.openlmis.core.view.widget;

import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISRepositoryUnitTest;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.view.activity.MMIAActivity;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(LMISTestRunner.class)
public class MMIARnrFormTest  extends LMISRepositoryUnitTest {

    private MMIARnrForm mmiaRnrForm;

    @Before
    public void setUp() {
        MMIAActivity activity = Robolectric.buildActivity(MMIAActivity.class).attach().get();
        mmiaRnrForm = new MMIARnrForm(activity);
    }

    @Test
    public void shouldSortByType() throws Exception {
        ArrayList<RnrFormItem> list = new ArrayList<>();

        list.add(getRnrFormItem(1L, "product", "08S17", Product.MEDICINE_TYPE_OTHER));
        list.add(getRnrFormItem(2L, "product2", "08S32Z", Product.MEDICINE_TYPE_BABY));
        list.add(getRnrFormItem(3L, "product3", "08S39Z", Product.MEDICINE_TYPE_ADULT));

        mmiaRnrForm.initView(list);

        ViewGroup leftViewGroup = (ViewGroup) mmiaRnrForm.findViewById(R.id.rnr_from_list_product_name);

        int indexHead = 0;
        int index = indexHead + 1;
        String text = ((TextView) leftViewGroup.getChildAt(index)).getText().toString();

        assertThat(text, is(list.get(2).getProduct().getPrimaryName()));

        index++;
        int indexDivider = 1;
        index += indexDivider + indexDivider;
        String text2 = ((TextView) leftViewGroup.getChildAt(index)).getText().toString();
        assertThat(text2, is(list.get(1).getProduct().getPrimaryName()));

        index++;
        index += indexDivider;
        String text3 = ((TextView) leftViewGroup.getChildAt(index)).getText().toString();
        assertThat(text3, is(list.get(0).getProduct().getPrimaryName()));
    }

    private RnrFormItem getRnrFormItem(long id, String primaryName, String code, String medicineType) {
        RnrFormItem item = new RnrFormItem();
        Product product = new Product();
        product.setId(id);
        product.setPrimaryName(primaryName);
        product.setCode(code);
        product.setMedicine_type(medicineType);
        item.setProduct(product);
        return item;
    }
}

