package endpoints;

import java.io.StringReader;
import java.util.Arrays;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author sebastian
 */
@ApplicationScoped
@ServerEndpoint("/voting")
public class VotingEndpoint {

    @Inject
    private VotingHandler sessionHandler;

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Message from " + session.getId() + ": " + message);
        try (JsonReader reader = Json.createReader(new StringReader(message))) {
            JsonObject jsonMessage = reader.readObject();

            ActionEnum action = ActionEnum.valueOf(jsonMessage.getString("action"));

            switch (action) {
                case SETNAME:
                    sessionHandler.setUserName(session, jsonMessage.getString("name"));
                    break;
                case GETROOMLIST:
                    sessionHandler.getRoomList(session);
                    break;
                case CREATEROOM:
                    sessionHandler.createRoom(session, jsonMessage.getString("name"));
                    break;
                case JOINROOM:
                    sessionHandler.joinRoom(session, jsonMessage.getInt("roomId"));
                    break;
                case NEWQUESTION:
                    Object[] oArr = jsonMessage.getJsonArray("answers").toArray();
                    String[] sArr = new String[oArr.length];
                    for(int i = 0; i < oArr.length; i++) {
                        sArr[i] = oArr[i].toString();
                    }
                    sessionHandler.createQuestion(session, jsonMessage.getString("question"), 
                            sArr);
                    break;
                case ANSWERQUESTION:
                    sessionHandler.answerQuestion(session, jsonMessage.getString("answer"));
                    break;
            }
        }
    }

    @OnOpen
    public void onOpen(Session peer) {
        System.out.println(peer.getId() + " has opened a connection");
        sessionHandler.addSession(peer);
    }

    @OnClose
    public void onClose(Session peer) {
        sessionHandler.removeSession(peer);
    }
}
