package edu.example.springmvcdemo.dto.processor;

import lombok.Data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Data
public class StreamDataDto {
    private InputStream stream;
    private long size;

    public StreamDataDto(ByteArrayOutputStream data) {
        var dataBytes = data.toByteArray();
        this.stream = new ByteArrayInputStream(dataBytes);
        this.size = dataBytes.length;
    }
}
