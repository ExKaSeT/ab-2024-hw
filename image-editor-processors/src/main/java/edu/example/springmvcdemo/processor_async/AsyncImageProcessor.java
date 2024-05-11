package edu.example.springmvcdemo.processor_async;

import java.io.InputStream;
import java.util.concurrent.Future;

public interface AsyncImageProcessor {
    Future<InputStream> process(InputStream imageStream, String imageExtension);

    int simultaneousTasksOptimalCount();
}
