package hk.ust.comp4321.server;

public class LoadingPage {
    private LoadingPage(){
        throw new AssertionError("LoadingPage cannot be instantiated!");
    }
    private static final String COUNTER_JS =
        """
        const socket = new WebSocket("ws://localhost:8080/progress");
        const max = %s
        document.getElementById("progress").max = max;
        socket.onmessage = (evt) => {
            let data = evt.data;
            if (data === "redirect") {
                window.location.href = "http://localhost:8080/"
                return;
            }
            const prog = document.getElementById("progress");
            prog.textContent = data + " / " + max;
            prog.value = data
        };
        """;

    private static final String LOADING_HTML =
        """
        <!DOCTYPE html>
        <html>
        <head>
            <title> Group 42 Search Engine</title>
        </head>
        %s
        <body>
        <div id="loadingText">Our search engine is loading. Please wait...</div>
        <progress id="progress" value="0" max="1"> 0 %% </progress>
        <script>
        %s
        </script>
        </body>
        """;

    private static String getLoadingPageStyle() {
        return
        """
        <style>
        body {
            display: block;
            justify-content: space-around;
            align-items: center;
            background-color: #daebf1;
            font-family: 'Open Sans', sans-serif;
            font-size: 20px;
            text-align: center;
        }
        #loadingText {
            margin-top: 10em;
        }
        #progress {
            width: 30em;
            padding-top: 25px;
            margin-top: 10px;
        }
        </style>
        """;
    }

    public static String getLoadingPage(int numLoaded) {
        return LOADING_HTML.formatted(getLoadingPageStyle(), COUNTER_JS.formatted(numLoaded));
    }
}
