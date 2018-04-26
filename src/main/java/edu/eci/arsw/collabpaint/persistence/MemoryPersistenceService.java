package edu.eci.arsw.collabpaint.persistence;

import edu.eci.arsw.collabpaint.model.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

//@Service
public class MemoryPersistenceService implements PersistenceService {

    @Autowired
    private SimpMessagingTemplate msgt;


    private ConcurrentHashMap<String, ArrayList<Point>> polygonpts=  new ConcurrentHashMap<>();


    @Override
    public void handleNewPoint(Point pt, String numDibujo) {
        //System.out.println("Using memory handler");
        System.out.println("Nuevo punto recibido en el servidor! (Persistencia usando Memoria) :" + pt);
        if (!polygonpts.containsKey(numDibujo)) {
            polygonpts.put(numDibujo, new ArrayList<>());
            polygonpts.get(numDibujo).add(pt);
        } else {
            polygonpts.get(numDibujo).add(pt);
        }

        if (polygonpts.get(numDibujo).size() == 4) {
            msgt.convertAndSend("/topic/newpolygon." + numDibujo, polygonpts.get(numDibujo));
            polygonpts.get(numDibujo).clear();
        }
        msgt.convertAndSend("/topic/newpoint." + numDibujo, pt);

    }
}
