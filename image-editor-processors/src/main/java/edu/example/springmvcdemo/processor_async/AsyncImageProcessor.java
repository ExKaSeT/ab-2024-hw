package edu.example.springmvcdemo.processor_async;

import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import java.util.concurrent.Future;

public interface AsyncImageProcessor {
    Future<StreamDataDto> process(String imageId, String imageExtension);

    int simultaneousTasksOptimalCount();
}
