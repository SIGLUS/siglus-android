package org.openlmis.core.utils;


import org.openlmis.core.presenter.DummyPresenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ ElementType.FIELD})
public @interface InjectPresenter {
    Class<? extends org.openlmis.core.presenter.Presenter> value() default DummyPresenter.class;
}
