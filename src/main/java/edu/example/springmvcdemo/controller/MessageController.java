package edu.example.springmvcdemo.controller;

import edu.example.springmvcdemo.model.Message;
import edu.example.springmvcdemo.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @QueryMapping
    public Message getMessageById(@Argument Long id) {
        return messageService.getMessage(id);
    }

    @QueryMapping
    public List<Message> getMessages() {
        return messageService.getAllMessages();
    }

    @MutationMapping
    public Message sendMessage(@Argument String content) {
        return messageService.createMessage(content);
    }
}
