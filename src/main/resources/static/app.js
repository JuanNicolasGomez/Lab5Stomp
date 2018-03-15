var app = (function () {

    class Point{
        constructor(x,y){
            this.x=x;
            this.y=y;

        }        
    }
    
    var stompClient = null;
    var subscriptionPoint = null;
    var subscriptionPolygon = null;
    var number = null;

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
        publishPoint(posx,posy);
    }

    var clearCanvas = function(){
       var canvas = document.getElementById("canvas");
       var ctx = canvas.getContext("2d");
       ctx.clearRect(0,0, canvas.width, canvas.height);
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
                       subscriptionPoint = stompClient.subscribe('/topic/newpoint.' + number, function (eventbody) {
                           var point = eventbody.body;
           				var theObject=JSON.parse(eventbody.body);
           				//alert(theObject);
           				addPointToCanvas(theObject);
                       });
                       console.log('Connected: ' + frame);
                                  subscriptionPolygon = stompClient.subscribe('/topic/newpolygon.' + number, function (eventbody) {
                                  var points = JSON.parse(eventbody.body);
                                  console.log("points: " + points);
                                  var ctx = canvas.getContext('2d');
                                  ctx.fillStyle="#000000";
                                  ctx.beginPath();
                                  ctx.moveTo(points[0].x, points[0].y);
                                  ctx.lineTo(points[1].x,points[1].y);
                                  ctx.lineTo(points[2].x, points[2].y);
                                  ctx.lineTo(points[3].x, points[3].y);
                                  ctx.lineTo(points[0].x, points[0].y);
                                  ctx.closePath();
                                  ctx.fill();
                                  });
                   });

    };
    
    var unsubscribe = function(){
        if (subscriptionPoint != null && subscriptionPolygon!= null){
            subscriptionPoint.unsubscribe();
            subscriptionPolygon.unsubscribe();
            console.log("unsubscribed")
        }
    };

    var publishPoint = function(px,py){
           var pt=new Point(px,py);
           console.info("publishing point at "+pt);
           addPointToCanvas(pt);
           //publicar el evento
        //creando un objeto literal
        //stompClient.send("/topic/newpoint", {}, JSON.stringify({x:10,y:10}));
        //enviando un objeto creado a partir de una clase
        console.log(JSON.stringify(pt));
        stompClient.send("/app/newpoint." + number, {}, JSON.stringify(pt));
       };

    return {

        init: function () {
            unsubscribe();
            clearCanvas();
            number = document.getElementById("num").value;
            var can = document.getElementById("canvas");
            var pos = can.addEventListener("click",addPointByClicking);
            //websocket connection
            if(number != null){
                connectAndSubscribe();
            }
        },

        publishPoint: publishPoint,

        disconnect: function () {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        }
    };

})();