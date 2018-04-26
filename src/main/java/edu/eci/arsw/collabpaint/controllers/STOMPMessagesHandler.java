package edu.eci.arsw.collabpaint.controllers;

import edu.eci.arsw.collabpaint.model.Point;
import edu.eci.arsw.collabpaint.persistence.PersistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class STOMPMessagesHandler {


    @Autowired
    private PersistenceService pers;

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {
        pers.handleNewPoint(pt, numdibujo);

    }


}
