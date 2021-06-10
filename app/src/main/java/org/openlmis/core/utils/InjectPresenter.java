package org.openlmis.core.utils;


import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.openlmis.core.presenter.DummyPresenter;

@Retention(RUNTIME)
@Target({ElementType.FIELD})
public @interface InjectPresenter {

  Class<? extends org.openlmis.core.presenter.Presenter> value() default DummyPresenter.class;
}
