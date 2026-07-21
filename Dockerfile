FROM alpine:3.20

RUN echo "Hello from the Docker pipeline!" > /message.txt

CMD ["cat", "/message.txt"]