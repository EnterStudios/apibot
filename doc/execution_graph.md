# Execution Graph

The following is an example of an execution graph that
registers a user into a website and then logins.
Finally an assertion is made ensuring the login went
correctly.

(defgraph
  (vertices
    (init)
    (register/direct)
    (user/login)
    (verify-authenticated))

  (edges
    (step init registe/direct)
    (step register/direct user/login)
    (setp user/login verify-authenticated)))

