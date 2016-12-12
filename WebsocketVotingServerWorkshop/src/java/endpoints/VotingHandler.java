package endpoints;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import javax.websocket.Session;
import models.Question;
import models.Room;
import models.User;

/**
 *
 * @author sebastian
 */
@ApplicationScoped
public class VotingHandler {

    private static Set<User> users = Collections.synchronizedSet(new HashSet<User>());
    private static Set<Room> rooms = Collections.synchronizedSet(new HashSet<Room>());

    public void addSession(Session session) {
        users.add(new User("", session));
    }

    public void removeSession(Session session) {
        User user = null;
        for (User u : users) {
            if (u.getSession().getId() == session.getId()) {
                user = u;
            }
        }
        users.remove(user);
    }

    public void setUserName(Session session, String name) {
        getUserBySession(session).setName(name);
        for (User user : users) {
            System.out.println(user.getSession().getId() + " " + user.getName());
        }
    }

    private User getUserBySession(Session session) {
        return users.stream()
                .filter(u -> u.getSession().getId() == session.getId())
                .findFirst()
                .get();
    }

    public void getRoomList(Session session) {
        JsonObject message = createRoomListMessage();
        sendToSession(session, message);
    }

    public void createRoom(Session session, String name) {
        Room room = new Room(getUserBySession(session), name);
        rooms.add(room);
        
        JsonObject message = createRoomListMessage();
        sendToConnectedSessions(message);
    }

    public void joinRoom(Session session, int id) {
        Room room = getRoomById(id);
        User user = getUserBySession(session);
        room.addUser(user);
        user.setRoom(room);
        Question question = room.getQuestion();

        if(question == null) {
            return;
        }
        
        JsonObject message = createNewQuestionMessage(question);

        sendToSession(session, message);
    }

    public void createQuestion(Session session, String quest, String... answers) {
        User user = getUserBySession(session);
        Room room = user.getRoom();
        
        if(room.getMaster().getId() != user.getId()) {
            return;
        }
        
        Question question = new Question(quest, answers);
        room.addQuestion(question);

        JsonObject message = createNewQuestionMessage(question);
        sendToConnectedSessions(message, room.getUsers());
    }

    public void answerQuestion(Session session, String answer) {
        Room room = getUserBySession(session).getRoom();
        room.answerQuestion(answer);

        JsonObject message = createGetVotesMessage(room.getQuestion());

        sendToConnectedSessions(message, room.getUsers());
    }

    private Room getRoomById(int id) {
        return rooms.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .get();
    }

    private Room getRoomByMaster(User user) {
        return rooms.stream()
                .filter(r -> r.getMaster().getId() == user.getId())
                .findFirst()
                .get();
    }

    private JsonObject createRoomListMessage() {
        JsonProvider provider = JsonProvider.provider();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Room room : rooms) {
            arrayBuilder.add(createRoomMessage(room));
        }

        JsonArray jsonArray = arrayBuilder.build();

        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.GETROOMLIST.toString())
                .add("list", jsonArray)
                .build();
        return message;
    }

    private JsonObject createRoomMessage(Room room) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.GETROOM.toString())
                .add("id", room.getId())
                .add("name", room.getName())
                .build();
        return message;
    }

    private JsonObject createUserListMessage() {
        JsonProvider provider = JsonProvider.provider();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (User user : users) {
            arrayBuilder.add(createUserMessage(user));
        }
        
        JsonArray jsonArray = arrayBuilder.build();

        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.GETUSERLIST.toString())
                .add("list", jsonArray)
                .build();
        return message;
    }

    private JsonObject createUserMessage(User user) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.GETUSER.toString())
                .add("id", user.getId())
                .add("name", user.getName())
                .build();
        return message;
    }

    private JsonObject createUserJoinedMessage(User user) {
        JsonProvider provider = JsonProvider.provider();
        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.USERJOINED.toString())
                .add("id", user.getId())
                .add("name", user.getName())
                .build();
        return message;
    }

    private JsonObject createNewQuestionMessage(Question question) {
        JsonProvider provider = JsonProvider.provider();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (String answer : question.getAnswers().keySet()) {
            arrayBuilder.add(provider.createObjectBuilder()
                    .add("answer", answer)
                    .build());
        }
        
        JsonArray jsonArray = arrayBuilder.build();

        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.NEWQUESTION.toString())
                .add("id", question.getId())
                .add("quest", question.getQuest())
                .add("answers", jsonArray)
                .build();
        return message;
    }

    private JsonObject createGetVotesMessage(Question question) {
        JsonProvider provider = JsonProvider.provider();

        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Entry<String, Integer> answer : question.getAnswers().entrySet()) {
            arrayBuilder.add(provider.createObjectBuilder()
                    .add("answer", answer.getKey())
                    .add("count", answer.getValue())
                    .build());
        }
        
        JsonArray jsonArray = arrayBuilder.build();

        JsonObject message = provider.createObjectBuilder()
                .add("action", ActionEnum.GETVOTES.toString())
                .add("id", question.getId())
                .add("quest", question.getQuest())
                .add("answers", jsonArray)
                .build();
        return message;
    }

    private void sendToConnectedSessions(JsonObject message) {
        sendToConnectedSessions(message, users);
    }

    private void sendToConnectedSessions(JsonObject message, Set<User> users) {
        for (User user : users) {
            sendToSession(user.getSession(), message);
        }
    }

    private void sendToSession(Session session, JsonObject message) {
        try {
            session.getBasicRemote().sendText(message.toString());
        } catch (IOException ex) {
            users.remove(getUserBySession(session));
        }
    }
}
