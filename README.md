# reviewsPlayService
To run the service locally:
1) clone repository
2) go to project directory
3) run ```sbt reload```
4) create local image using ```sbt docker:publishLocal``` command
5) create container using ```docker run -it -p 9000:9000 --rm reviewsplayservice:1.0``` command
