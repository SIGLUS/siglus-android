package org.openlmis.core.view.widget;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.core.LMISTestRunner;
import org.openlmis.core.model.Product;
import org.openlmis.core.model.RnrFormItem;
import org.openlmis.core.view.activity.MMIAActivity;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(LMISTestRunner.class)
public class MMIARnrFormTest {

    private MMIARnrForm mmiaRnrForm;

    @Before
    public void setUp() {
        MMIAActivity activity = Robolectric.buildActivity(MMIAActivity.class).create().get();
        mmiaRnrForm = new MMIARnrForm(activity);
    }

    @Test
    public void shouldSortByType() throws Exception {
        ArrayList<RnrFormItem> list = new ArrayList<>();

        RnrFormItem item = new RnrFormItem();
        Product product = new Product();
        product.setCode("08S17");//type is other
        item.setProduct(product);
        list.add(item);

        RnrFormItem item2 = new RnrFormItem();
        Product product2 = new Product();
        product2.setCode("08S32B");//type is other
        item2.setProduct(product2);
        list.add(item2);

        RnrFormItem item3 = new RnrFormItem();
        Product product3 = new Product();
        product3.setCode("08S39Z");//type is other
        item3.setProduct(product3);
        list.add(item3);

        mmiaRnrForm.sortAndSetType(list);
        ArrayList<RnrFormItem> rnrFormItemList = mmiaRnrForm.getRnrFormItemList();

        assertThat(rnrFormItemList.get(0).getProduct().getMedicine_type(), is(Product.MEDICINE_TYPE_ADULT));
        assertThat(rnrFormItemList.get(2).getProduct().getMedicine_type(),is(Product.MEDICINE_TYPE_OTHER));
    }
}
