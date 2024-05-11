package edu.example.springmvcdemo.processor_async;

import edu.example.springmvcdemo.processor.ImageProcessor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class ExecutorServiceAsyncProcessor implements AsyncImageProcessor {
    private final ImageProcessor processor;
    private final ExecutorService executorService;
    private final int optimalThreadCount = Runtime.getRuntime().availableProcessors();

    public ExecutorServiceAsyncProcessor(ImageProcessor processor) {
        this.processor = processor;
        this.executorService = Executors.newFixedThreadPool(optimalThreadCount);
    }

    @Override
    public Future<InputStream> process(InputStream imageStream, String imageExtension) {
        return executorService.submit(() -> processor.process(imageStream, imageExtension));
    }

    @Override
    public int simultaneousTasksOptimalCount() {
        return optimalThreadCount;
    }
}
