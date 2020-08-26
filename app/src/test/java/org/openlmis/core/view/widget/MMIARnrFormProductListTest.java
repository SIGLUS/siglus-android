package org.openlmis.core.view.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.R;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openlmis.core.model.Product.MEDICINE_TYPE_ADULT;
import static org.openlmis.core.model.Product.MEDICINE_TYPE_CHILDREN;
import static org.openlmis.core.model.Product.MEDICINE_TYPE_SOLUTION;

@RunWith(LMISTestRunner.class)
public class MMIARnrFormProductListTest {

    private MMIARnrFormProductList mmiaRnrFormProductList;

    @Before
    public void setUp() {
        mmiaRnrFormProductList = new MMIARnrFormProductList(RuntimeEnvironment.application);
        mmiaRnrFormProductList.setLayoutParams(new ViewGroup.MarginLayoutParams(100, 100));
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

        mmiaRnrFormProductList.initView(list, false);

        ViewGroup leftViewGroup = mmiaRnrFormProductList.findViewById(R.id.rnr_from_list_product_name);

        assertThat(getCodeFromTextView(leftViewGroup, 0, 0, 1), is(list.get(1).getProduct().getCode()));
        assertThat(getPrimaryNameTextView(leftViewGroup, 0, 1, 1), is(list.get(1).getProduct().getPrimaryName()));

        assertThat(getCodeFromTextView(leftViewGroup, 1, 0, 1), is(list.get(3).getProduct().getCode()));
        assertThat(getPrimaryNameTextView(leftViewGroup, 1, 1, 1), is(list.get(3).getProduct().getPrimaryName()));

        assertThat(getCodeFromTextView(leftViewGroup, 4, 0, 1), is(list.get(2).getProduct().getCode()));
        assertThat(getPrimaryNameTextView(leftViewGroup, 4, 1, 1), is(list.get(2).getProduct().getPrimaryName()));

        assertThat(getCodeFromTextView(leftViewGroup, 6, 0, 1), is(list.get(0).getProduct().getCode()));
        assertThat(getPrimaryNameTextView(leftViewGroup, 6, 1, 1), is(list.get(0).getProduct().getPrimaryName()));
    }

    private String getCodeFromTextView(ViewGroup parentViewGroup, int first, int middle, int end) {
        return ((TextView) ((LinearLayout) ((LinearLayout) parentViewGroup.getChildAt(first)).getChildAt(middle)).getChildAt(end)).getText().toString();
    }

    private String getPrimaryNameTextView(ViewGroup parentViewGroup, int first, int middle, int end) {
        return ((TextView) (((LinearLayout) parentViewGroup.getChildAt(first)).getChildAt(middle))).getText().toString();
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

