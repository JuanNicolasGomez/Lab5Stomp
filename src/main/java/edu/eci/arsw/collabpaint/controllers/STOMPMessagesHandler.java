package edu.eci.arsw.collabpaint.controllers;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class STOMPMessagesHandler {

    private ConcurrentHashMap<String, ArrayList<Point>> polygonpts=  new ConcurrentHashMap<>();


    @Autowired
    SimpMessagingTemplate msgt;

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {
        System.out.println("Nuevo punto recibido en el servidor!:"+pt);
        if(!polygonpts.containsKey(numdibujo)){
            polygonpts.put(numdibujo, new ArrayList<>());
            polygonpts.get(numdibujo).add(pt);
        }else{
            polygonpts.get(numdibujo).add(pt);
        }

        if (polygonpts.get(numdibujo).size() == 4) {
            msgt.convertAndSend("/topic/newpolygon."+numdibujo, polygonpts.get(numdibujo));
            polygonpts.get(numdibujo).clear();
        }
        msgt.convertAndSend("/topic/newpoint."+numdibujo, pt);
    }


}
