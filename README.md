# TeleclojureBot

A Telegram inline bot that can evaluate clojure forms and return their results.
This is still a work in progress, so, running it is very unsafe right now.

Also, it is a bit unstable since sometimes the polling to Telegram API fails.

## TODO

* Make sure it is safe to run any kind of form without exposing the token
* Better format the inline result when the form is evaluated successfully
* Better format when the form fails/returns an exception
* Better logging
