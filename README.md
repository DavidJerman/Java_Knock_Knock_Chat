# About
A small Java project,
which consists of a server and a client and allows communication between users on their clients.

## Custom protocol used
### Client to server
* POST / Public message sent to all users
* POST_PRIVATE to_username / Private message sent to a specified user
### Server to client
* POST from_user / Public message from a user
* POST_PRIVATE from_user / Private message from a user
