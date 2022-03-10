FROM bash:5.1

RUN apk add --no-cache git

COPY tag-release.sh /tag-release.sh

ENTRYPOINT ["/tag-release.sh"]