package edu.eci.arsw.collabpaint.controllers;

import edu.eci.arsw.collabpaint.model.Point;
import edu.eci.arsw.collabpaint.util.JedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
public class STOMPMessagesHandler {

    private ConcurrentHashMap<String, ArrayList<Point>> polygonpts=  new ConcurrentHashMap<>();
    private ArrayList<Integer> coordX = new ArrayList<>();
    private ArrayList<Integer> coordY = new ArrayList<>();
    private String luaScript = "local xval,yval; \n" +
            "if (redis.call('LLEN','x')==4) then \n" +
            "\txval=redis.call('LRANGE','x',0,-1);\n" +
            "\tyval=redis.call('LRANGE','y',0,-1);\n" +
            "\tredis.call('DEL','x'); \n" +
            "\tredis.call('DEL','y'); \t\t\n" +
            "\treturn {xval,yval}; \n" +
            "else \n" +
            "\treturn {}; \n" +
            "end";

    @Autowired
    SimpMessagingTemplate msgt;

    @MessageMapping("/newpoint.{numdibujo}")
    public void handlePointEvent(Point pt, @DestinationVariable String numdibujo) throws Exception {
        Jedis jedis = JedisUtil.getPool().getResource();
        jedis.watch("x", "y");
        Transaction t = jedis.multi();
        //t.set("points", "x);
        //System.out.println("SIZEEEEEE:   -  " + t.exec().size());
        t.rpush("x", String.valueOf(pt.getX()));
        t.rpush("y", String.valueOf(pt.getY()));


        Response<Object> luares = t.eval(luaScript.getBytes(), 0,"0".getBytes());
        List<Object> res=t.exec();
        System.out.println("SIZEEEE2222: " + res.size());
        if (((ArrayList)luares.get()).size()==2){
            System.out.println(new String((byte[])((ArrayList)(((ArrayList)luares.get()).get(0))).get(0)));
        }
        jedis.close();

        if (res.size() >0) {
            System.out.println("Nuevo punto recibido en el servidor! (agregado a redis):" + pt);
            if (!polygonpts.containsKey(numdibujo)) {
                polygonpts.put(numdibujo, new ArrayList<>());
                polygonpts.get(numdibujo).add(pt);
            } else {
                polygonpts.get(numdibujo).add(pt);
            }

            if (polygonpts.get(numdibujo).size() == 4) {
                msgt.convertAndSend("/topic/newpolygon." + numdibujo, polygonpts.get(numdibujo));
                polygonpts.get(numdibujo).clear();
            }
            msgt.convertAndSend("/topic/newpoint." + numdibujo, pt);
        }
    }


}
