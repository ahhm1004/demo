package com.codesoom.demo;

import com.codesoom.demo.models.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DemoHttpHandler implements HttpHandler {
    static int count = 1;
    private List<Task> tasks =  new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    public DemoHttpHandler(){
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Do nothing");

        tasks.add(task);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String requestMethod = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        InputStream inputStream = exchange.getRequestBody();
        String body = new BufferedReader(new InputStreamReader(inputStream))
                .lines()
                .collect(Collectors.joining("\n"));

        System.out.println(requestMethod + path + "- request Count : " + count);
        count ++;
        if(!body.isBlank()){
            Task task = toTask(body);
            tasks.add(task);
            System.out.println("id : " + task.getId() + "\t" + task.toString());
        }

        String content = "good Choice!";
        if(requestMethod.equals("GET") && path.equals("/tasks")){
            content = tasksToJSON();
            //            for(Task task : tasks)
//            content = "[{\"id\":" + task.getId() + "\"title\":" + task.getTitle()+"}]";
//            content = "[{\"id\":1, \"title\":\"Do nothing\"}]";
        }
        if(requestMethod.equals("POST") && path.equals("/tasks")){
            content = "Create a new task";
        }

        exchange.sendResponseHeaders(200, content.getBytes(StandardCharsets.UTF_8).length);
        OutputStream output = exchange.getResponseBody();
        output.write(content.getBytes(StandardCharsets.UTF_8));
        output.flush();
        output.close();
    }

    private Task toTask(String content) throws JsonProcessingException {
        return objectMapper.readValue(content, Task.class);
    }

    private String tasksToJSON() throws IOException{

        OutputStream outputStream = new ByteArrayOutputStream();
        objectMapper.writeValue(outputStream, tasks);

        return outputStream.toString();
    }
}
