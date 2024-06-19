import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class testThread {
    public static void main(String[] args) throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        ExecutorService executorService1 = Executors.newSingleThreadExecutor();
        ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();


        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println(Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }


    }
}
