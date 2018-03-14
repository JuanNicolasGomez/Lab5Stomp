var app = (function () {

    class Point{
        constructor(x,y){
            this.x=x;
            this.y=y;

        }        
    }
    
    var stompClient = null;

    var addPointToCanvas = function (point) {        
        var canvas = document.getElementById("canvas");
        var ctx = canvas.getContext("2d");
        ctx.beginPath();
        ctx.arc(point.x, point.y, 3, 0, 2 * Math.PI);
        ctx.stroke();
    };

    var addPointByClicking = function(e){
        var pos = getMousePosition(e);
            posx = pos.x;
            posy = pos.y;
        var point = new Point(posx,posy);
        addPointToCanvas(point);
        stompClient.send("/topic/newpoint", {}, JSON.stringify(point));
    }
    
    
    var getMousePosition = function (evt) {
        canvas = document.getElementById("canvas");
        var rect = canvas.getBoundingClientRect();
        return {
            x: evt.clientX - rect.left,
            y: evt.clientY - rect.top
        };
    };

    var connectAndSubscribe = function () {
        console.info('Connecting to WS...');
        var socket = new SockJS('/stompendpoint');
        stompClient = Stomp.over(socket);
        
        //subscribe to /topic/TOPICXX when connections succeed
        stompClient.connect({}, function (frame) {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/newpoint', function (eventbody) {
                var point = eventbody.body;
				var theObject=JSON.parse(eventbody.body);
				//alert(theObject);
				addPointToCanvas(theObject);
            });
        });

    };
    
    

    return {

        init: function () {
            var can = document.getElementById("canvas");
            var pos = can.addEventListener("click",addPointByClicking);
            //websocket connection
            connectAndSubscribe();
        },

        publishPoint: function(px,py){
            var pt=new Point(px,py);
            console.info("publishing point at "+pt);
            addPointToCanvas(pt);
            //publicar el evento
			//creando un objeto literal
			//stompClient.send("/topic/newpoint", {}, JSON.stringify({x:10,y:10}));
			//enviando un objeto creado a partir de una clase
			stompClient.send("/topic/newpoint", {}, JSON.stringify(pt));
        },

        disconnect: function () {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        }

        //addPointByClicking: addPointByClicking
    };

})();