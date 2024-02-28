# reviewsPlayService
To run the service locally:
1) clone repository
2) go to project directory
3) run ```sbt reload```
4) create local image using ```sbt docker:publishLocal``` command.
   Don't worry if you see log entries that starts with [error] text .
   This happens because Docker prints some of its output to stderr and the internal logger renders it as an error.
6) run the container using ```docker run -it -p 9000:9000 --rm reviewsplayservice:1.0``` command

To see top domains, open http://localhost:9000/top10
