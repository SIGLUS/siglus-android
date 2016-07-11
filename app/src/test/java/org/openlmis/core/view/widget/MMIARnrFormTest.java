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
import static org.openlmis.core.model.Product.MEDICINE_TYPE_ADULT;
import static org.openlmis.core.model.Product.MEDICINE_TYPE_CHILDREN;
import static org.openlmis.core.model.Product.MEDICINE_TYPE_OTHER;
import static org.openlmis.core.model.Product.MEDICINE_TYPE_SOLUTION;

@RunWith(LMISTestRunner.class)
public class MMIARnrFormTest {

    private MMIARnrForm mmiaRnrForm;

    @Before
    public void setUp() {
        mmiaRnrForm = new MMIARnrForm(RuntimeEnvironment.application);
        mmiaRnrForm.setLayoutParams(new ViewGroup.MarginLayoutParams(100,100));
    }

    @Test
    public void shouldSortByProductCodeAndCategory() throws Exception {
        ArrayList<RnrFormItem> list = new ArrayList<>();
        // Product.MEDICINE_TYPE_SOLUTION
        list.add(getRnrFormItem(1L, "product", "08S17", MEDICINE_TYPE_SOLUTION));
        // Product.MEDICINE_TYPE_ADULT
        list.add(getRnrFormItem(3L, "product3", "08S22Z", MEDICINE_TYPE_ADULT));
        // Product.MEDICINE_TYPE_CHILDREN
        list.add(getRnrFormItem(2L, "product2", "08S32Z", MEDICINE_TYPE_CHILDREN));
        // Product.MEDICINE_TYPE_ADULT
        list.add(getRnrFormItem(4L, "product4", "08S39Z", MEDICINE_TYPE_ADULT));

        mmiaRnrForm.initView(list);

        ViewGroup leftViewGroup = (ViewGroup) mmiaRnrForm.findViewById(R.id.rnr_from_list_product_name);

        String text = ((TextView) leftViewGroup.getChildAt(0)).getText().toString();
        assertThat(text, is(list.get(1).getProduct().getPrimaryName()));

        String text1 = ((TextView) leftViewGroup.getChildAt(1)).getText().toString();
        assertThat(text1, is(list.get(3).getProduct().getPrimaryName()));

        String text2 = ((TextView) leftViewGroup.getChildAt(4)).getText().toString();
        assertThat(text2, is(list.get(2).getProduct().getPrimaryName()));

        String text3 = ((TextView) leftViewGroup.getChildAt(6)).getText().toString();
        assertThat(text3, is(list.get(0).getProduct().getPrimaryName()));
    }

    private RnrFormItem getRnrFormItem(long id, String primaryName, String code, String category) {
        RnrFormItem item = new RnrFormItem();
        Product product = new Product();
        product.setId(id);
        product.setPrimaryName(primaryName);
        product.setCode(code);
        item.setProduct(product);
        item.setCategory(category);
        return item;
    }
}

