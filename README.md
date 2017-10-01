Monitoring application using Apache Kafka, Kafka Streams and Redis

## Projects

- [accs-metrics-producer](https://github.com/abhirockzz/accs-ehcs-stream-processing/tree/master/accs-metrics-producer) - this is the mock (Java) producer application which pushes CPU metrics to a Kafka topic
- [accs-metrics-stream-processing](https://github.com/abhirockzz/accs-ehcs-stream-processing/tree/master/accs-metrics-stream-processing) - Kafka Streams based (Java) application which processes the data i.e. calculates the cumulative moving average (per machine) on the continuous stream of CPU metrics and pushes them to Redis
- [accs-dashboard](https://github.com/abhirockzz/accs-ehcs-stream-processing/tree/master/accs-dashboard) - Java EE (WAR) powered server side component which communicates with Redis and exposes machine leaderboard as well as individual machine metrics via SSE (Server Sent Event) channel which is leveraged by an Oracle JET based front end application to provide a real time monitoring dashboard

![](images/dashboard.jpg) 
