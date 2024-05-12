package edu.example.springmvcdemo.processor_async;

import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import edu.example.springmvcdemo.processor.ImageProcessor;
import org.springframework.stereotype.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class FixedThreadPoolAsyncProcessor implements AsyncImageProcessor {
    private final ImageProcessor processor;
    private final ExecutorService executorService;
    private final int optimalThreadCount = Runtime.getRuntime().availableProcessors();
    private final StorageRepository storageRepository;

    public FixedThreadPoolAsyncProcessor(ImageProcessor processor, StorageRepository storageRepository) {
        this.processor = processor;
        this.storageRepository = storageRepository;
        this.executorService = Executors.newFixedThreadPool(optimalThreadCount);
    }

    @Override
    public Future<StreamDataDto> process(String imageId, String imageExtension) {
        return executorService.submit(() ->
                processor.process(storageRepository.getObject(imageId), imageExtension));
    }

    @Override
    public int simultaneousTasksOptimalCount() {
        return optimalThreadCount;
    }
}
