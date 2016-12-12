
define(['ojs/ojcore', 'knockout', 'jquery', 'ojs/ojknockout', 'promise', 'ojs/ojlistview', 'ojs/ojarraytabledatasource',
    'ojs/ojbutton', 'ojs/ojinputtext', 'ojs/ojbutton', 'ojs/ojchart', 'ojs/ojradioset'],
        function (oj, ko, $)
        {
            var url = "ws://localhost:8080/WebsocketVotingServer/voting";
            var websocket = new WebSocket(url);

            var self = this;

            self.newRoomName = ko.observable("new Room");
            self.RoomItems = ko.observableArray([]);
            self.selectedItems = ko.observableArray([]);
            self.dataSource = new oj.ArrayTableDataSource(self.RoomItems, {idAttribute: "id"});

            self.newQuestion = ko.observable("");
            self.answers = ko.observable("");
            var voteArr = [];

            self.votingList = ko.observableArray([]);
            self.selectedOption = ko.observableArray([]);
            self.votingDataSource = new oj.ArrayTableDataSource(self.votingList, {idAttribute: "id", answer: "answer"});

            self.threeDValue = ko.observable('off');
            self.pieSeriesValue = ko.observableArray([]);
            /* toggle buttons*/
            self.threeDOptions = [
                {id: '2D', label: '2D', value: 'off', icon: 'oj-icon demo-2d'},
                {id: '3D', label: '3D', value: 'on', icon: 'oj-icon demo-3d'}
            ];

            websocket.onopen = function (event) {
                websocket.send(JSON.stringify({action: "GETROOMLIST"}));
            }

            websocket.onmessage = function (event) {
                var obj = JSON.parse(event.data);

                if (obj.action == "GETROOMLIST") {
                    self.RoomItems(obj.list);
                }
                if (obj.action == "NEWQUESTION") {
                    console.log(obj);
                    self.newQuestion(obj.quest);
                    voteArr = [];
                    for (var i = 0; i < obj.answers.length; i++) {
                        voteArr.push({id: i, answer: obj.answers[i].answer})
                    }
                    self.votingList(voteArr);
                    self.pieSeriesValue([]);
                    document.getElementById("vote").disabled = false;
                }
                if (obj.action == "GETVOTES") {
                    console.log(obj);
                    var arr = [];
                    for (var i = 0; i < obj.answers.length; i++) {
                        arr.push({name: obj.answers[i].answer, items: [obj.answers[i].count]});
                    }
                    self.pieSeriesValue(arr);
                }
            };

            self.enter = function () {
                document.getElementById("bottom").hidden = false;
                document.getElementById("top").hidden = true;
                websocket.send(JSON.stringify({action: "JOINROOM", roomId: this.selectedItems()[0]}));

            };

            self.createRoom = function () {
                websocket.send(JSON.stringify({action: "CREATEROOM", name: this.newRoomName()}))
            };

            self.createQuestion = function () {
                websocket.send(JSON.stringify({action: "NEWQUESTION", question: this.newQuestion(), answers: this.answers().split(";")}))
            }

            self.sendVote = function () {
                websocket.send(JSON.stringify({action: "ANSWERQUESTION", answer: voteArr[self.selectedOption()].answer}))
                document.getElementById("vote").disabled = true;
            }

            self.threeDValueChange = function (event, data) {
                self.threeDValue(data.value);
                return true;
            }
        });
