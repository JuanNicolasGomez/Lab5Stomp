package edu.eci.arsw.collabpaint.persistence;

import edu.eci.arsw.collabpaint.model.Point;

public interface PersistenceService {


    void handleNewPoint(Point pt, String numDibujo);
}
