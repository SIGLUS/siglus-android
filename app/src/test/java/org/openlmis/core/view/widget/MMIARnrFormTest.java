package org.openlmis.core.view.widget;

import android.view.ViewGroup;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestApp;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(LMISTestRunner.class)
public class MMIARnrFormTest {

    private MMIARnrForm mmiaRnrForm;

    @Before
    public void setUp() {
        mmiaRnrForm = new MMIARnrForm(RuntimeEnvironment.application);
        mmiaRnrForm.setLayoutParams(new ViewGroup.MarginLayoutParams(100,100));
    }

    @Test
    public void shouldSortByType() throws Exception {
        ArrayList<RnrFormItem> list = new ArrayList<>();
        // Product.MEDICINE_TYPE_OTHER
        list.add(getRnrFormItem(1L, "product", "08S17"));
        // Product.MEDICINE_TYPE_BABY
        list.add(getRnrFormItem(2L, "product2", "08S32Z"));
        // Product.MEDICINE_TYPE_ADULT
        list.add(getRnrFormItem(3L, "product3", "08S39Z"));

        mmiaRnrForm.initView(list);

        ViewGroup leftViewGroup = (ViewGroup) mmiaRnrForm.findViewById(R.id.rnr_from_list_product_name);

        int index = 0;
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

    @Test
    public void shouldIncludeTheNewMMIAProductsWhichIsNotInList() throws Exception {
        LMISTestApp.getInstance().setFeatureToggle(R.bool.feature_deactivate_program_product, true);
        List<RnrFormItem> rnrFormItems = Arrays.asList(
                getRnrFormItem(1L, "product1", "08S17"),
                getRnrFormItem(2L, "produc2", "01A01"),
                getRnrFormItem(3L, "produc3", "08S20"));

        mmiaRnrForm.initView(rnrFormItems);
        ViewGroup leftViewGroup = (ViewGroup) mmiaRnrForm.findViewById(R.id.rnr_from_list_product_name);

        assertEquals(7, leftViewGroup.getChildCount());
    }

    private RnrFormItem getRnrFormItem(long id, String primaryName, String code) {
        RnrFormItem item = new RnrFormItem();
        Product product = new Product();
        product.setId(id);
        product.setPrimaryName(primaryName);
        product.setCode(code);
        item.setProduct(product);
        return item;
    }
}

