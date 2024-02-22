package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.dao.MessageRepository;
import edu.example.springmvcdemo.exception.EntityNotFoundException;
import edu.example.springmvcdemo.model.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message getMessage(Long id) {
        return messageRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Message createMessage(String content) {
        var Message = new Message();
        Message.setContent(content);
        return messageRepository.save(Message);
    }
}