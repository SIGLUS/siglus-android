package org.openlmis.core.service.sync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rx.Scheduler;
import rx.schedulers.Schedulers;

public final class SchedulerBuilder {

  private SchedulerBuilder() {
  }

  public static Scheduler createScheduler() {
    int threadNumber = Runtime.getRuntime().availableProcessors();
    ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(threadNumber);
    return Schedulers.from(threadPoolExecutor);
  }
}
