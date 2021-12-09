FROM alpine:latest

MAINTAINER Nex <nex@bits.team>

RUN apk update
RUN apk add bash mysql-client

COPY db-import.sh .
COPY db.dump .

CMD /bin/bash db-import.sh
