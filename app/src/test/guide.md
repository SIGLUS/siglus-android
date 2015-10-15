# Test Guidelines

This is the document about how to write our unit test in Android codebase. We used [Robolectric](http://robolectric.org/)
to test-driven the development of Android app.

Unit testing in Android should cover most UI interaction and details. Unlikely with Java unit test, Android unit tests can cover
the UI interactions and UI details. For all the UI interaction, we should try to verify the UI element properties. For the logic
that can not reflected in UI layer, maybe mock is a good choice.

## Libraries or patterns we used

Before we start unit testing, please understand how we design our project.

## MVP pattern

If you're not familiar with MVP pattern, you can refer this [article](http://antonioleiva.com/mvp-android/). Each Activity is connected with
one presenter. Activity will set the content view, initialize all the UI elements or bind some event for elements. And all the business logic would
be put in presenter.

### How to test MVP

Let's use LoginActivity as an example. LoginActivity implemented the LoginPresenter.LoginView:

```
    public interface LoginView extends View {
        void clearPassword();
        void goToHomePage();
        void goToInitInventory();
        boolean needInitInventory();
        void showInvalidAlert();
        void showUserNameEmpty();
        void showPasswordEmpty();
        boolean isConnectionAvailable();
        boolean hasGetProducts();
        void setHasGetProducts(boolean hasGetProducts);
        void clearErrorAlerts();
    }
}
```

In the LoginActivityTest, we directly invoke these methods, and check UI elements to verify whether it works.

For LoginPresenter, it contains business logic. We should follow the LoginPresenterTest's way to test it. We need to
verify whether the mocked LoginView has called the related methods.

## Roboguice

> RoboGuice is a framework that brings the simplicity and ease of Dependency Injection to Android, using Google's own Guice library.

Want to find more details, refer [here](https://github.com/roboguice/roboguice).

During the testing, if we want to mock the LoginPresenter in LoginActivityTest, please inject it as belows:

```
    private LoginPresenter mockedPresenter;

    @Before
    public void setUp() {
        mockedPresenter = mock(LoginPresenter.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new AbstractModule() {
            @Override
            protected void configure() {
                bind(LoginPresenter.class).toInstance(mockedPresenter);
            }
        });
        loginActivity = Robolectric.buildActivity(LoginActivity.class).create().get();
    }

    @After
    public void teardown() {
        RoboGuice.Util.reset();
    }
```

## RxXJava/RxAndroid

> RxJava is a Java VM implementation of Reactive Extensions: a library for composing asynchronous and event-based programs by using observable sequences. RxAndroid adds the minimum classes to RxJava that make writing reactive components in Android applications easy and hassle-free. More specifically, it provides a Scheduler that schedules on the main UI thread or any given Handler.



