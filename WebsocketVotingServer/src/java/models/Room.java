/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sebastian
 */
public class Room {
    private static int IDCOUNT = 0;
    private int id;
    private User master;
    private Set<User> users = Collections.synchronizedSet(new HashSet<User>());;
    private String name;
    private Question question;
    
    public Room(User master, String name) {
        id = IDCOUNT++;
        this.master = master;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public User getMaster() {
        return master;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void addUser(User user) {
        users.add(user);
    }
    
    public void removeUser(User user) {
        users.remove(user);
    }

    public Set<User> getUsers() {
        return users;
    }
    
    public Question getQuestion() {
        return question;
    }
    
    public void addQuestion(Question question) {
        this.question = question;
    }
    
    public void answerQuestion(String answer) {
        question.answer(answer);
    }
}
