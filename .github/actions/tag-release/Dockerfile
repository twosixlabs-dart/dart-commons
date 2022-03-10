FROM alpine:latest

RUN apk add --no-cache git

COPY tag-release.sh /tag-release.sh

ENTRYPOINT ["/tag-release.sh"]