# docker build -t api .

FROM golang:alpine
WORKDIR /escapade
COPY go.mod .
COPY go.sum .
RUN apk add --update git
RUN apk add --update bash && rm -rf /var/cache/apk/*
RUN go mod download
ENV AWS_ACCESS_KEY_ID=AKIAI6NC7Q6LN7OJG2OA
ENV AWS_SECRET_ACCESS_KEY=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
ENV AWS_REGION=eu-west-2
COPY . .
RUN go build -o bin/ery cmd/services/ery/main.go