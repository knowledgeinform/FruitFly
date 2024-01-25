To launch test swarm: (launches both agent and ground-control station webserver
  as docker containers that "talk" to each other)
$ docker-compose up [--build]
  --build option re-builds the docker container with any updates

OR to re-build also the agent code and agent docker container:
$ ./build.sh

To launch the app:
$ cd src/gui
$ npm start
