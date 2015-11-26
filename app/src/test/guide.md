# Test Guidelines

This is the document about how to write our unit test in Android codebase. We used [Robolectric](http://robolectric.org/)
to test-driven the development of Android app.
(You need to install 'Andrid Studio Unit Test' plugin if you are using andriod studio.)

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

Before we begin to test Observable and Subscriber. We need to understood `onError`, `onNext` and `onCompleted` callback in subscriber.
Please read [here](http://reactivex.io/documentation/observable.html) if you're not clear when and how should we call these methods.
Generally speaking, `onNext(Object o)` method could called for many times and followed by `onCompleted()` method. But when we got some
exception in try catch, `onError(Throwable throwable)` method is a good choice to emit errors to presenter layer.

To make sure a high test coverage for unit testing, we highly recommended extract Observable and Subscriber/Action to separated protected
methods. We'll use `InventoryPresenter` as the example for RxJava.

RxAndroid has integrated some testing class for a better unit testing convenience. Please make sure we use the latest libraries for RxJava and RxAndroid:

```
    compile 'io.reactivex:rxjava:1.0.14'
    compile 'io.reactivex:rxandroid:1.0.1'
```

### Testing Observable

As recommended, we extract the Observable create to a separate method in Presenter:

```
    public Observable<List<StockCardViewModel>> loadStockCardList() {
        return Observable.create(new Observable.OnSubscribe<List<StockCardViewModel>>() {
            @Override
            public void call(Subscriber<? super List<StockCardViewModel>> subscriber) {
                List<StockCard> list;
                try {
                    list = stockRepository.list();
                    subscriber.onNext(from(list).transform(new Function<StockCard, StockCardViewModel>() {
                        @Override
                        public StockCardViewModel apply(StockCard stockCard) {
                            return new StockCardViewModel(stockCard);
                        }
                    }).toList());
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io());
    }
```

We will use the mocked stockPresenter to mock some data and make sure subscriber called method `onNext(Object o)`.

Before we started to test Observable, we need to config SchedulerHooks in Robolectric @setup method. Then the Async operations would
execute once we subscribe it. With the support of `TestSubscriber`, it's easily to check the emitted data to `onNext(Object o)`. And
the Schedulers reset should always be called in `@After tearDown()` method. Otherwise all the tests would be failed.

```
    @Before
    public void setup() throws Exception{
        stockRepositoryMock = mock(StockRepository.class);

        view = mock(InventoryPresenter.InventoryView.class);
        RoboGuice.overrideApplicationInjector(RuntimeEnvironment.application, new MyTestModule());

        inventoryPresenter = RoboGuice.getInjector(RuntimeEnvironment.application).getInstance(InventoryPresenter.class);
        inventoryPresenter.attachView(view);

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return Schedulers.immediate();
            }
        });
    }
```

Given we successfully configured the RxAndroid. It's time to do unit tests for Observables.

```
    @Test
    public void shouldLoadStockCardList() throws LMISException {
        StockCard stockCard1 = StockCardBuilder.buildStockCard();
        StockCard stockCard2 = StockCardBuilder.buildStockCard();
        List<StockCard> stockCards = Arrays.asList(stockCard1, stockCard2);
        when(stockRepositoryMock.list()).thenReturn(stockCards);

        TestSubscriber<List<StockCardViewModel>> subscriber = new TestSubscriber<>();
        Observable<List<StockCardViewModel>> observable = inventoryPresenter.loadStockCardList();
        observable.subscribe(subscriber);

        subscriber.awaitTerminalEvent();

        verify(stockRepositoryMock).list();
        subscriber.assertNoErrors();
        subscriber.assertValue(Arrays.asList(new StockCardViewModel(stockCard1), new StockCardViewModel(stockCard2)));
    }
```

While `onNext(Object o)` method could be called for many times, that's why subscribe.assertValue only takes for `List<T>` params.
The key is to use `subscriber.awaitTerminalEvent()` to wait until `onError` or `onCompleted` method is called. That's why we need
to call `onCompleted` after our operation has finished.

### Testing Subscriber

Testing subscriber is kindly simply, cause subscriber is indeed the collection of callbacks. We could make the subscriber be protected
instance variable. So that we can get it and call the method manually to verify whether it works.

Before we begin testing, we need to know `Subscriber` could also be updated to `Action1`.

```
    new Subscriber<Object>() {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {

        }
    };
```

It has the same effect with:

```
    new Action0(){
        @Override
        public void call() {

        }
    };

    new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {

        }
    };

    new Action1<Objects>() {
        @Override
        public void call(Objects objects) {

        }
    };
```

Observable could take `Action1<Object>`, `Action1<Throwable>` and `Action0()` as callbacks.

Below is the example how we test Actions:

```
    @Test
    public void shouldGoToMainPageWhenOnNextCalled() {
        inventoryPresenter.nextMainPageAction.call(null);

        verify(view).loaded();
        verify(view).goToMainPage();
    }

    @Test
    public void shouldShowErrorWhenOnErrorCalled() {
        String errorMessage = "This is throwable error";
        inventoryPresenter.errorAction.call(new Throwable(errorMessage));

        verify(view).loaded();
        verify(view).showErrorMessage(errorMessage);
    }
```

That's all about Observable and Subscriber testing. To make it easy be tested, we use Observable like below:

```
    protected Observable<Object> stockMovementObservable(final List<StockCardViewModel> list) {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    for (StockCardViewModel model : list) {
                        stockRepository.addStockMovement(model.getStockCardId(), calculateAdjustment(model));
                    }
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                } catch (LMISException e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    protected Action1<Object> nextMainPageAction = new Action1<Object>() {
        @Override
        public void call(Object o) {
            view.loaded();
            view.goToMainPage();
        }
    };

    protected Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable throwable) {
            view.loaded();
            view.showErrorMessage(throwable.getMessage());
        }
    };

    public void doInitialInventory(final List<StockCardViewModel> list) {
        if (view.validateInventory()) {
            view.loading();
            initStockCardObservable(list).subscribe(nextMainPageAction);
        }
    }
```

## Useful Robolectric testing way

Below are some common ways for Android unit test. Please add more if you think it's necessary.

### Start Activity with Intent

When we want to initialize Activity with specified Intent, we need to create an Intent with data setted and pass it to Robolectric

```
    @Test
    public void shouldPassIntentToActivity() {
        Intent intent = new Intent();
        intent.putExtra("EXTRA_USERNAME", "gongmingqm10");
        intent.putExtra("USER_ID", "asre234hj234asf54398gtwg");
        loginActivity = Robolectric.buildActivity(LoginActivity.class).withIntent(intent).create().get();

        //loginActivity could get all the Intent values
        assertThat(...);
    }
```

### Test next Activity

Use `ShadowApplication` to get the next started Application:

```
    @Test
    public void shouldTestStartedActivity() {
        loginActivity.btnLogin.performClick();

        Intent startedIntent = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();

        String pageTitle = startedIntent.getStringExtra("Ming");

        assertThat(startedIntent.getComponent().getClassName()).isEqualTo(HomeActivity.class.getName());
        assertThat(pageTitle).isEqualTo("Ming");
    }

```

### Test next AlertDialog

Use `AlertDialog.getLatestDialog` to get the latest started Dialog. With the help of `ShadowAlertDialog`, it's easy to get title, message
and trigger the button event.

```
    @Test
    public void shouldTestAlertDialog() {
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(dialog);

        assertThat(shadowAlertDialog.getTitle()).isEqualTo("Title test");
        shadowAlertDialog.clickOn(Dialog.BUTTON_POSITIVE);

        // given clear the positive button will navigte to new page
        Intent intent = shadowOf(loginActivity).getNextStartedActivity();
        assertThat(intent.getComponent().getClassName()).isEqualTo(HomeActivity.class.getName());
    }
```

Read more about [Robolectric](http://robolectric.org/) here.