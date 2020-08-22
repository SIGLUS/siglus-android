package org.openlmis.core.view.widget;

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
        mmiaRnrFormProductList.setLayoutParams(new ViewGroup.MarginLayoutParams(100,100));
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

        mmiaRnrFormProductList.initView(list);

        ViewGroup leftViewGroup =  mmiaRnrFormProductList.findViewById(R.id.rnr_from_list_product_name);

        String text11 =((TextView)((LinearLayout)leftViewGroup.getChildAt(0)).getChildAt(0)).getText().toString();
        String text12 =((TextView)((LinearLayout)leftViewGroup.getChildAt(0)).getChildAt(1)).getText().toString();
        assertThat(text11, is(list.get(1).getProduct().getCode()));
        assertThat(text12, is(list.get(1).getProduct().getPrimaryName()));

        String text21 =((TextView)((LinearLayout)leftViewGroup.getChildAt(1)).getChildAt(0)).getText().toString();
        String text22 =((TextView)((LinearLayout)leftViewGroup.getChildAt(1)).getChildAt(1)).getText().toString();
        assertThat(text21, is(list.get(3).getProduct().getCode()));
        assertThat(text22, is(list.get(3).getProduct().getPrimaryName()));

        String text41 =((TextView)((LinearLayout)leftViewGroup.getChildAt(4)).getChildAt(0)).getText().toString();
        String text42 =((TextView)((LinearLayout)leftViewGroup.getChildAt(4)).getChildAt(1)).getText().toString();
        assertThat(text41, is(list.get(2).getProduct().getCode()));
        assertThat(text42, is(list.get(2).getProduct().getPrimaryName()));

        String text61 =((TextView)((LinearLayout)leftViewGroup.getChildAt(6)).getChildAt(0)).getText().toString();
        String text62 =((TextView)((LinearLayout)leftViewGroup.getChildAt(6)).getChildAt(1)).getText().toString();
        assertThat(text61, is(list.get(0).getProduct().getCode()));
        assertThat(text62, is(list.get(0).getProduct().getPrimaryName()));
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

