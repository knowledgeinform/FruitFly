To launch swarm for server-side development/testing: (launches swarm of agent
  docker containers):
$ ./build.sh
$ docker-compose up --build
  NOTE: the ip address in the Dockerfile has to match that of the host-pc for
  the agents to see/talk-to the webserver

To build agent test code for agent-side development/testing on host:
$ cd src
$ go build . -o aout
$ ./aout
