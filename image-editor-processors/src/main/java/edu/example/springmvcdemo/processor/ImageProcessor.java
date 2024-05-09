package edu.example.springmvcdemo.processor;

import java.io.IOException;
import java.io.InputStream;

public interface ImageProcessor {
    InputStream process(InputStream stream) throws IOException;
}
