package edu.example.springmvcdemo.processor;

import edu.example.springmvcdemo.dto.processor.StreamDataDto;
import java.io.*;

public interface ImageProcessor {
    StreamDataDto process(InputStream imageStream, String imageExtension) throws IOException;
}
