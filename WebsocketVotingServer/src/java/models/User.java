/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import javax.json.JsonObject;
import javax.websocket.Session;

/**
 *
 * @author sebastian
 */
public class User {
    private static int IDCOUNT = 0;
    private int id;
    private String name;
    private Session session;
    private Room room;
    
    public User(String name, Session session) {
        id = IDCOUNT++;
        this.name = name;
        this.session = session;
    }

    public int getId() {
        return id;
    }
    
    public Session getSession() {
        return session;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setRoom(Room room) {
        this.room = room;
    }

    public Room getRoom() {
        return room;
    }
}
