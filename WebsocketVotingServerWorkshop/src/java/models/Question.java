/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author sebastian
 */
public class Question {
    private static int IDCOUNT = 0;
    private int id;
    private String quest;
    private HashMap<String, Integer> answers;
    
    public Question(String quest, String... answers) {
        id = IDCOUNT++;
        this.quest = quest;
        this.answers = new HashMap<>();
        for(String s : answers) {
            this.answers.put(s, 0);
        }
    }

    public int getId() {
        return id;
    }

    public String getQuest() {
        return quest;
    }

    public HashMap<String, Integer> getAnswers() {
        return answers;
    }
    
    public void answer(String answer) {
        System.out.println("answer = " + answer);
        System.out.println(answers);
        System.out.println(answers.containsKey(answer));
        answers.put(answer, answers.get(answer)+1);
    }
}
